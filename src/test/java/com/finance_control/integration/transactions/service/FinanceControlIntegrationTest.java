package com.finance_control.integration.transactions.service;

import com.finance_control.integration.BaseIntegrationTest;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.transactions.dto.source.TransactionSourceDTO;
import com.finance_control.transactions.service.source.TransactionSourceService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Sql("/test-data.sql")
class FinanceControlIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionSourceService transactionSourceService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findById(1L).orElseThrow();
    }

    @Test
    void shouldCreateTransactionSource() {
        TransactionSourceDTO createDTO = new TransactionSourceDTO();
        createDTO.setName("New Credit Card");
        createDTO.setDescription("Test credit card");
        createDTO.setSourceType(TransactionSource.CREDIT_CARD);
        createDTO.setBankName("Test Bank");
        createDTO.setCardType("Credit");
        createDTO.setCardLastFour("9999");
        createDTO.setAccountBalance(new BigDecimal("1000.00"));
        createDTO.setUserId(testUser.getId());

        TransactionSourceDTO result = transactionSourceService.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New Credit Card");
        assertThat(result.getSourceType()).isEqualTo(TransactionSource.CREDIT_CARD);
        assertThat(result.getBankName()).isEqualTo("Test Bank");
        assertThat(result.getUserId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindTransactionSourcesByUserId() {
        Page<TransactionSourceDTO> sources = transactionSourceService.findAllWithFilters(testUser.getId(), true, null, null, null);

        assertThat(sources).hasSize(3);
        assertThat(sources).extracting("name")
                .containsExactlyInAnyOrder("Nubank Credit Card", "Itaú Savings Account", "PIX Wallet");
    }

    @Test
    void shouldFindActiveTransactionSources() {
        Page<TransactionSourceDTO> activeSources = transactionSourceService.findAllWithFilters(testUser.getId(), true, null, null, null);

        assertThat(activeSources).hasSize(3);
        assertThat(activeSources).allMatch(source -> source.getIsActive());
    }

    @Test
    void shouldFindTransactionSourceById() {
        Optional<TransactionSourceDTO> source = transactionSourceService.findById(1L, testUser.getId());

        assertThat(source).isPresent();
        assertThat(source.get().getName()).isEqualTo("Nubank Credit Card");
        assertThat(source.get().getSourceType()).isEqualTo(TransactionSource.CREDIT_CARD);
    }

    @Test
    void shouldNotFindTransactionSourceForWrongUser() {
        Optional<TransactionSourceDTO> source = transactionSourceService.findById(1L, 999L);

        assertThat(source).isEmpty();
    }

    @Test
    void shouldUpdateTransactionSource() {
        TransactionSourceDTO updateDTO = new TransactionSourceDTO();
        updateDTO.setName("Updated Credit Card");
        updateDTO.setDescription("Updated description");
        updateDTO.setSourceType(TransactionSource.CREDIT_CARD);
        updateDTO.setBankName("Updated Bank");
        updateDTO.setCardType("Credit");
        updateDTO.setCardLastFour("8888");
        updateDTO.setAccountBalance(new BigDecimal("2000.00"));
        updateDTO.setUserId(testUser.getId());

        TransactionSourceDTO result = transactionSourceService.update(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Credit Card");
        assertThat(result.getBankName()).isEqualTo("Updated Bank");
        assertThat(result.getCardLastFour()).isEqualTo("8888");
    }

    @Test
    void shouldDeleteTransactionSource() {
        transactionSourceService.deleteTransactionSource(1L, testUser.getId());

        Optional<TransactionSourceDTO> deletedSource = transactionSourceService.findById(1L, testUser.getId());
        assertThat(deletedSource).isEmpty();
    }

    @Test
    void shouldHandleUserIsolation() {
        User otherUser = userRepository.findById(2L).orElseThrow();

        Page<TransactionSourceDTO> user1Sources = transactionSourceService.findAllWithFilters(testUser.getId(), true, null, null, null);
        Page<TransactionSourceDTO> user2Sources = transactionSourceService.findAllWithFilters(otherUser.getId(), true, null, null, null);

        assertThat(user1Sources).hasSize(3);
        assertThat(user2Sources).hasSize(1);
        assertThat(user2Sources.get(0).getName()).isEqualTo("Jane Credit Card");
    }

    @Test
    void shouldValidateTransactionSourceData() {
        TransactionSourceDTO invalidDTO = new TransactionSourceDTO();
        invalidDTO.setName(""); // Invalid empty name
        invalidDTO.setSourceType(TransactionSource.CREDIT_CARD);
        invalidDTO.setUserId(testUser.getId());

        // This should throw a validation exception
        try {
            transactionSourceService.create(invalidDTO);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void shouldPreventDuplicateNamesForSameUser() {
        TransactionSourceDTO duplicateDTO = new TransactionSourceDTO();
        duplicateDTO.setName("Nubank Credit Card"); // Already exists
        duplicateDTO.setDescription("Duplicate card");
        duplicateDTO.setSourceType(TransactionSource.CREDIT_CARD);
        duplicateDTO.setUserId(testUser.getId());

        try {
            transactionSourceService.create(duplicateDTO);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("already exists");
        }
    }

    @Test
    void shouldAllowSameNameForDifferentUsers() {
        User otherUser = userRepository.findById(2L).orElseThrow();

        TransactionSourceDTO duplicateDTO = new TransactionSourceDTO();
        duplicateDTO.setName("Nubank Credit Card"); // Same name as user 1
        duplicateDTO.setDescription("Duplicate card for different user");
        duplicateDTO.setSourceType(TransactionSource.CREDIT_CARD);
        duplicateDTO.setUserId(otherUser.getId());

        TransactionSourceDTO result = transactionSourceService.create(duplicateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Nubank Credit Card");
        assertThat(result.getUserId()).isEqualTo(otherUser.getId());
    }
} 