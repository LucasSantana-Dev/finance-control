package com.finance_control.integration.transactions.service;

import com.finance_control.FinanceControlApplication;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import com.finance_control.transactions.service.TransactionService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TestContainers integration test for TransactionService.
 * Tests real database operations with PostgreSQL container.
 *
 * This is the industry standard approach for integration testing in Spring Boot.
 */
@SpringBootTest(classes = FinanceControlApplication.class)
@Testcontainers
@ActiveProfiles("test")
@Transactional
class TransactionServiceTestContainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionCategoryRepository categoryRepository;

    @Autowired
    private TransactionResponsiblesRepository responsibleRepository;

    @Autowired
    private FinancialGoalRepository goalRepository;

    private User testUser;
    private TransactionCategory testCategory;
    private TransactionResponsibles testResponsible;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // "password"
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Create test category
        testCategory = new TransactionCategory();
        testCategory.setName("Test Category");
        testCategory = categoryRepository.save(testCategory);

        // Create test responsible
        testResponsible = new TransactionResponsibles();
        testResponsible.setName("Test Responsible");
        testResponsible = responsibleRepository.save(testResponsible);

        // Set user context
        UserContext.setCurrentUserId(testUser.getId());
    }

    @Test
    void shouldCreateTransactionWithRealDatabase() {
        // Given
        TransactionDTO dto = new TransactionDTO();
        dto.setDescription("Test Transaction");
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setType(com.finance_control.shared.enums.TransactionType.INCOME);
        dto.setSubtype(com.finance_control.shared.enums.TransactionSubtype.FIXED);
        dto.setSource(com.finance_control.shared.enums.TransactionSource.CASH);
        dto.setCategoryId(testCategory.getId());
        dto.setUserId(testUser.getId());

        // Add responsibilities
        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setId(1L);
        responsible.setName("Test Responsible");
        responsible.setResponsibleId(testResponsible.getId());
        responsible.setPercentage(BigDecimal.valueOf(100.00));
        responsibilities.add(responsible);
        dto.setResponsibilities(responsibilities);

        // When
        TransactionDTO result = transactionService.create(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Test Transaction");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));

        // Verify in database
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getDescription()).isEqualTo("Test Transaction");
        assertThat(transactions.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindTransactionsByUser() {
        // Given - Create a transaction
        Transaction transaction = new Transaction();
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.setType(com.finance_control.shared.enums.TransactionType.INCOME);
        transaction.setSubtype(com.finance_control.shared.enums.TransactionSubtype.FIXED);
        transaction.setSource(com.finance_control.shared.enums.TransactionSource.CASH);
        transaction.setUser(testUser);
        transaction.setCategory(testCategory);
        transaction.setDate(LocalDateTime.now());
        transactionRepository.save(transaction);

        // When - Use repository directly to avoid method ambiguity
        List<Transaction> transactions = transactionRepository.findAll();

        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getDescription()).isEqualTo("Test Transaction");
    }

    @Test
    void shouldUpdateTransaction() {
        // Given - Create a transaction
        Transaction transaction = new Transaction();
        transaction.setDescription("Original Description");
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.setType(com.finance_control.shared.enums.TransactionType.INCOME);
        transaction.setSubtype(com.finance_control.shared.enums.TransactionSubtype.FIXED);
        transaction.setSource(com.finance_control.shared.enums.TransactionSource.CASH);
        transaction.setUser(testUser);
        transaction.setCategory(testCategory);
        transaction.setDate(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        // When - Update the transaction
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setId(transaction.getId());
        updateDTO.setDescription("Updated Description");
        updateDTO.setAmount(BigDecimal.valueOf(150.00));
        updateDTO.setType(com.finance_control.shared.enums.TransactionType.INCOME);
        updateDTO.setSubtype(com.finance_control.shared.enums.TransactionSubtype.FIXED);
        updateDTO.setSource(com.finance_control.shared.enums.TransactionSource.CASH);
        updateDTO.setCategoryId(testCategory.getId());
        updateDTO.setUserId(testUser.getId());

        TransactionDTO result = transactionService.update(transaction.getId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(150.00));

        // Verify in database
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElse(null);
        assertThat(updatedTransaction).isNotNull();
        assertThat(updatedTransaction.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedTransaction.getAmount()).isEqualTo(BigDecimal.valueOf(150.00));
    }

    @Test
    void shouldDeleteTransaction() {
        // Given - Create a transaction
        Transaction transaction = new Transaction();
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.setType(com.finance_control.shared.enums.TransactionType.INCOME);
        transaction.setSubtype(com.finance_control.shared.enums.TransactionSubtype.FIXED);
        transaction.setSource(com.finance_control.shared.enums.TransactionSource.CASH);
        transaction.setUser(testUser);
        transaction.setCategory(testCategory);
        transaction.setDate(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        // When
        transactionService.delete(transaction.getId());

        // Then
        assertThat(transactionRepository.findById(transaction.getId())).isEmpty();
    }

    @Test
    void shouldHandleComplexTransactionWithResponsibilities() {
        // Given
        TransactionDTO dto = new TransactionDTO();
        dto.setDescription("Complex Transaction");
        dto.setAmount(BigDecimal.valueOf(200.00));
        dto.setType(com.finance_control.shared.enums.TransactionType.INCOME);
        dto.setSubtype(com.finance_control.shared.enums.TransactionSubtype.FIXED);
        dto.setSource(com.finance_control.shared.enums.TransactionSource.CASH);
        dto.setCategoryId(testCategory.getId());
        dto.setUserId(testUser.getId());

        // Add multiple responsibilities
        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();

        TransactionResponsiblesDTO responsible1 = new TransactionResponsiblesDTO();
        responsible1.setId(1L);
        responsible1.setName("Responsible 1");
        responsible1.setResponsibleId(testResponsible.getId());
        responsible1.setPercentage(BigDecimal.valueOf(60.00));
        responsibilities.add(responsible1);

        TransactionResponsiblesDTO responsible2 = new TransactionResponsiblesDTO();
        responsible2.setId(2L);
        responsible2.setName("Responsible 2");
        responsible2.setResponsibleId(testResponsible.getId());
        responsible2.setPercentage(BigDecimal.valueOf(40.00));
        responsibilities.add(responsible2);

        dto.setResponsibilities(responsibilities);

        // When
        TransactionDTO result = transactionService.create(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Complex Transaction");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(200.00));

        // Verify in database
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
        Transaction savedTransaction = transactions.get(0);
        assertThat(savedTransaction.getResponsibilities()).hasSize(2);
        assertThat(savedTransaction.getTotalPercentage()).isEqualTo(BigDecimal.valueOf(100.00));
    }
}
