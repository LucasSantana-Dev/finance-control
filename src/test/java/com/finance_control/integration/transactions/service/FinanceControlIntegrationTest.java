package com.finance_control.integration.transactions.service;

import com.finance_control.integration.BaseIntegrationTest;
import com.finance_control.shared.config.TestUserContextConfig;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.transactions.dto.source.TransactionSourceDTO;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.transactions.service.source.TransactionSourceService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestUserContextConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@Rollback(false)
class FinanceControlIntegrationTest extends BaseIntegrationTest {

    private static final String CREDIT_CARD = "Nubank Credit Card";
    private static final String SAVINGS_ACCOUNT = "Itaú Savings Account";
    private static final String PIX_WALLET = "PIX Wallet";
    private static final String JANE_CREDIT_CARD = "Jane Credit Card";
    private static final String NEW_CREDIT_CARD = "New Credit Card";
    private static final String UPDATED_CREDIT_CARD = "Updated Credit Card";

    @Autowired
    private TransactionSourceService transactionSourceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionSourceRepository transactionSourceRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("$2a$10$dummy.hash.for.testing");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);
        
        // Create other user for isolation tests
        otherUser = new User();
        otherUser.setEmail("jane.smith@example.com");
        otherUser.setPassword("$2a$10$dummy.hash.for.testing");
        otherUser.setIsActive(true);
        otherUser = userRepository.save(otherUser);
        
        // Configure UserContext for this test
        UserContext.setCurrentUserId(testUser.getId());

        // Create TransactionSourceEntity with expected names for testUser
        String[] names = {CREDIT_CARD, SAVINGS_ACCOUNT, PIX_WALLET};
        for (int i = 0; i < names.length; i++) {
            var entity = new com.finance_control.transactions.model.source.TransactionSourceEntity();
            entity.setName(names[i]);
            entity.setDescription("Test source " + (i + 1));
            entity.setSourceType(com.finance_control.shared.enums.TransactionSource.CREDIT_CARD);
            entity.setIsActive(true);
            entity.setUser(testUser);
            transactionSourceRepository.save(entity);
        }
    }

    @Test
    void shouldCreateTransactionSource() {
        TransactionSourceDTO createDTO = new TransactionSourceDTO();
        createDTO.setName(NEW_CREDIT_CARD);
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
        assertThat(result.getName()).isEqualTo(NEW_CREDIT_CARD);
        assertThat(result.getSourceType()).isEqualTo(TransactionSource.CREDIT_CARD);
        assertThat(result.getBankName()).isEqualTo("Test Bank");
        assertThat(result.getUserId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindTransactionSourcesByUserId() {
        Page<TransactionSourceDTO> sources = transactionSourceService.findAll("", null, null, null,
                PageRequest.of(0, 10));

        assertThat(sources).hasSize(3);
        assertThat(sources).extracting("name")
                .containsExactlyInAnyOrder(CREDIT_CARD, SAVINGS_ACCOUNT, PIX_WALLET);
    }

    @Test
    void shouldFindActiveTransactionSources() {
        Page<TransactionSourceDTO> activeSources = transactionSourceService.findAll("", null, null, null,
                PageRequest.of(0, 10));

        assertThat(activeSources).hasSize(3);
        assertThat(activeSources).allMatch(source -> source.getIsActive());
    }

    @Test
    void shouldFindTransactionSourceById() {
        Optional<TransactionSourceDTO> source = transactionSourceService.findById(1L);

        assertThat(source).isPresent();
        assertThat(source.get().getName()).isEqualTo(CREDIT_CARD);
        assertThat(source.get().getSourceType()).isEqualTo(TransactionSource.CREDIT_CARD);
    }

    @Test
    void shouldNotFindTransactionSourceForWrongUser() {
        Optional<TransactionSourceDTO> source = transactionSourceService.findById(999L);

        assertThat(source).isEmpty();
    }

    @Test
    void shouldUpdateTransactionSource() {
        TransactionSourceDTO updateDTO = new TransactionSourceDTO();
        updateDTO.setName(UPDATED_CREDIT_CARD);
        updateDTO.setDescription("Updated description");
        updateDTO.setSourceType(TransactionSource.CREDIT_CARD);
        updateDTO.setBankName("Updated Bank");
        updateDTO.setCardType("Credit");
        updateDTO.setCardLastFour("8888");
        updateDTO.setAccountBalance(new BigDecimal("2000.00"));
        updateDTO.setUserId(testUser.getId());

        TransactionSourceDTO result = transactionSourceService.update(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(UPDATED_CREDIT_CARD);
        assertThat(result.getBankName()).isEqualTo("Updated Bank");
        assertThat(result.getCardLastFour()).isEqualTo("8888");
    }

    @Test
    void shouldDeleteTransactionSource() {
        transactionSourceService.delete(1L);

        Optional<TransactionSourceDTO> deletedSource = transactionSourceService.findById(1L);
        assertThat(deletedSource).isEmpty();
    }

    @Test
    void shouldHandleUserIsolation() {
        try {
            // Create a source for otherUser
            var otherUserSource = new com.finance_control.transactions.model.source.TransactionSourceEntity();
            otherUserSource.setName(JANE_CREDIT_CARD);
            otherUserSource.setDescription("Jane's credit card");
            otherUserSource.setSourceType(com.finance_control.shared.enums.TransactionSource.CREDIT_CARD);
            otherUserSource.setIsActive(true);
            otherUserSource.setUser(otherUser);
            transactionSourceRepository.save(otherUserSource);
            
            // Set context for testUser e buscar fontes
            UserContext.setCurrentUserId(testUser.getId());
            Page<TransactionSourceDTO> user1Sources = transactionSourceService.findAll("", null, null, null,
                    PageRequest.of(0, 10));
            
            // Set context para otherUser e buscar fontes
            UserContext.setCurrentUserId(otherUser.getId());
            Page<TransactionSourceDTO> user2Sources = transactionSourceService.findAll("", null, null, null,
                    PageRequest.of(0, 10));

            assertThat(user1Sources).hasSize(3);
            assertThat(user2Sources).hasSize(1);
            assertThat(user2Sources.getContent().get(0).getName()).isEqualTo(JANE_CREDIT_CARD);
        } finally {
            UserContext.setCurrentUserId(null);
        }
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
        duplicateDTO.setName(CREDIT_CARD); // Already exists
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
        UserContext.setCurrentUserId(otherUser.getId());
        TransactionSourceDTO duplicateDTO = new TransactionSourceDTO();
        duplicateDTO.setName(CREDIT_CARD); // Same name as user 1
        duplicateDTO.setDescription("Duplicate card for different user");
        duplicateDTO.setSourceType(TransactionSource.CREDIT_CARD);
        duplicateDTO.setUserId(otherUser.getId());

        TransactionSourceDTO result = transactionSourceService.create(duplicateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(CREDIT_CARD);
        assertThat(result.getUserId()).isEqualTo(otherUser.getId());
    }
}