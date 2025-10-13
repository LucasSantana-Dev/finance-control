package com.finance_control.integration.transactions.service;

import com.finance_control.integration.BaseIntegrationTest;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.service.TransactionService;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import com.finance_control.unit.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class TransactionServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionCategoryRepository categoryRepository;

    @Autowired
    private TransactionResponsiblesRepository responsibleRepository;

    private User testUser;
    private TransactionCategory testCategory;
    private TransactionResponsibles testResponsible;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("integration@test.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        testCategory = new TransactionCategory();
        testCategory.setName("Test Category");
        testCategory = categoryRepository.save(testCategory);

        testResponsible = new TransactionResponsibles();
        testResponsible.setName("Test Responsible");
        testResponsible = responsibleRepository.save(testResponsible);

        TestUtils.setupUserContext(testUser.getId());
    }

    @AfterEach
    void tearDown() {
        TestUtils.clearUserContext();
    }

    @Test
    void shouldCreateAndRetrieveTransaction() {
        TransactionDTO dto = new TransactionDTO();
        dto.setDescription("Integration Test Transaction");
        dto.setAmount(BigDecimal.valueOf(150.00));
        dto.setDate(LocalDateTime.now());
        dto.setType(TransactionType.INCOME);
        dto.setSubtype(TransactionSubtype.FIXED);
        dto.setSource(TransactionSource.CASH);
        dto.setCategoryId(testCategory.getId());
        dto.setUserId(testUser.getId());

        // Add required responsibilities
        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setId(1L);
        responsible.setName("Test Responsible");
        responsible.setResponsibleId(testResponsible.getId());
        responsible.setPercentage(BigDecimal.valueOf(100.00));
        responsibilities.add(responsible);
        dto.setResponsibilities(responsibilities);

        TransactionDTO created = transactionService.create(dto);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Integration Test Transaction");
        assertThat(created.getAmount()).isEqualTo(BigDecimal.valueOf(150.00));
    }

    @Test
    void shouldFindTransactionsByUser() {
        TransactionDTO dto1 = new TransactionDTO();
        dto1.setDescription("Transaction 1");
        dto1.setAmount(BigDecimal.valueOf(100.00));
        dto1.setType(TransactionType.INCOME);
        dto1.setSubtype(TransactionSubtype.FIXED);
        dto1.setSource(TransactionSource.CASH);
        dto1.setCategoryId(testCategory.getId());
        dto1.setUserId(testUser.getId());

        // Add required responsibilities
        List<TransactionResponsiblesDTO> responsibilities1 = new ArrayList<>();
        TransactionResponsiblesDTO responsible1 = new TransactionResponsiblesDTO();
        responsible1.setId(1L);
        responsible1.setName("Test Responsible 1");
        responsible1.setResponsibleId(testResponsible.getId());
        responsible1.setPercentage(BigDecimal.valueOf(100.00));
        responsibilities1.add(responsible1);
        dto1.setResponsibilities(responsibilities1);

        transactionService.create(dto1);

        Page<TransactionDTO> result = transactionService.findAll(null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}
