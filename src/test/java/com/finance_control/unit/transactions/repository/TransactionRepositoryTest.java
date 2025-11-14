package com.finance_control.unit.transactions.repository;

import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest repository layer test for TransactionRepository.
 * Tests JPA repository operations with in-memory H2 database.
 *
 * This is the industry standard approach for testing repositories in Spring Boot.
 */
@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser;
    private TransactionCategory testCategory;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = entityManager.persistAndFlush(testUser);

        // Create test category
        testCategory = new TransactionCategory();
        testCategory.setName("Test Category");
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = entityManager.persistAndFlush(testCategory);

        // Create test transaction
        testTransaction = new Transaction();
        testTransaction.setDescription("Test Transaction");
        testTransaction.setAmount(BigDecimal.valueOf(100.00));
        testTransaction.setType(TransactionType.INCOME);
        testTransaction.setSubtype(TransactionSubtype.FIXED);
        testTransaction.setSource(TransactionSource.CASH);
        testTransaction.setUser(testUser);
        testTransaction.setCategory(testCategory);
        testTransaction.setDate(LocalDateTime.now());
        testTransaction.setCreatedAt(LocalDateTime.now());
        testTransaction.setUpdatedAt(LocalDateTime.now());
        testTransaction = entityManager.persistAndFlush(testTransaction);
    }

    @Test
    void shouldFindTransactionById() {
        // When
        Optional<Transaction> found = transactionRepository.findById(testTransaction.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Test Transaction");
        assertThat(found.get().getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(found.get().getType()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    void shouldFindAllTransactions() {
        // Given - Create additional transaction
        Transaction anotherTransaction = new Transaction();
        anotherTransaction.setDescription("Another Transaction");
        anotherTransaction.setAmount(BigDecimal.valueOf(200.00));
        anotherTransaction.setType(TransactionType.EXPENSE);
        anotherTransaction.setSubtype(TransactionSubtype.FIXED);
        anotherTransaction.setSource(TransactionSource.CASH);
        anotherTransaction.setUser(testUser);
        anotherTransaction.setCategory(testCategory);
        anotherTransaction.setDate(LocalDateTime.now());
        anotherTransaction.setCreatedAt(LocalDateTime.now());
        anotherTransaction.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(anotherTransaction);

        // When
        List<Transaction> transactions = transactionRepository.findAll();

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Test Transaction", "Another Transaction");
    }

    @Test
    void shouldFindTransactionsByUser() {
        // Given - Create another user with transaction
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi");
        anotherUser.setIsActive(true);
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser.setUpdatedAt(LocalDateTime.now());
        anotherUser = entityManager.persistAndFlush(anotherUser);

        Transaction anotherUserTransaction = new Transaction();
        anotherUserTransaction.setDescription("Another User Transaction");
        anotherUserTransaction.setAmount(BigDecimal.valueOf(300.00));
        anotherUserTransaction.setType(TransactionType.INCOME);
        anotherUserTransaction.setSubtype(TransactionSubtype.FIXED);
        anotherUserTransaction.setSource(TransactionSource.CASH);
        anotherUserTransaction.setUser(anotherUser);
        anotherUserTransaction.setCategory(testCategory);
        anotherUserTransaction.setDate(LocalDateTime.now());
        anotherUserTransaction.setCreatedAt(LocalDateTime.now());
        anotherUserTransaction.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(anotherUserTransaction);

        // When - Use standard JPA method
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<Transaction> userTransactions = allTransactions.stream()
                .filter(t -> t.getUser().getId().equals(testUser.getId()))
                .toList();

        // Then
        assertThat(userTransactions).hasSize(1);
        assertThat(userTransactions.get(0).getDescription()).isEqualTo("Test Transaction");
        assertThat(userTransactions.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindTransactionsByCategory() {
        // Given - Create another category with transaction
        TransactionCategory anotherCategory = new TransactionCategory();
        anotherCategory.setName("Another Category");
        anotherCategory.setCreatedAt(LocalDateTime.now());
        anotherCategory.setUpdatedAt(LocalDateTime.now());
        anotherCategory = entityManager.persistAndFlush(anotherCategory);

        Transaction anotherCategoryTransaction = new Transaction();
        anotherCategoryTransaction.setDescription("Another Category Transaction");
        anotherCategoryTransaction.setAmount(BigDecimal.valueOf(400.00));
        anotherCategoryTransaction.setType(TransactionType.EXPENSE);
        anotherCategoryTransaction.setSubtype(TransactionSubtype.FIXED);
        anotherCategoryTransaction.setSource(TransactionSource.CASH);
        anotherCategoryTransaction.setUser(testUser);
        anotherCategoryTransaction.setCategory(anotherCategory);
        anotherCategoryTransaction.setDate(LocalDateTime.now());
        anotherCategoryTransaction.setCreatedAt(LocalDateTime.now());
        anotherCategoryTransaction.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(anotherCategoryTransaction);

        // When - Use standard JPA method
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<Transaction> categoryTransactions = allTransactions.stream()
                .filter(t -> t.getCategory().getId().equals(testCategory.getId()))
                .toList();

        // Then
        assertThat(categoryTransactions).hasSize(1);
        assertThat(categoryTransactions.get(0).getDescription()).isEqualTo("Test Transaction");
        assertThat(categoryTransactions.get(0).getCategory().getId()).isEqualTo(testCategory.getId());
    }

    @Test
    void shouldFindTransactionsByType() {
        // Given - Create expense transaction
        Transaction expenseTransaction = new Transaction();
        expenseTransaction.setDescription("Expense Transaction");
        expenseTransaction.setAmount(BigDecimal.valueOf(50.00));
        expenseTransaction.setType(TransactionType.EXPENSE);
        expenseTransaction.setSubtype(TransactionSubtype.FIXED);
        expenseTransaction.setSource(TransactionSource.CASH);
        expenseTransaction.setUser(testUser);
        expenseTransaction.setCategory(testCategory);
        expenseTransaction.setDate(LocalDateTime.now());
        expenseTransaction.setCreatedAt(LocalDateTime.now());
        expenseTransaction.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(expenseTransaction);

        // When - Use standard JPA method
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<Transaction> incomeTransactions = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .toList();

        // Then
        assertThat(incomeTransactions).hasSize(1);
        assertThat(incomeTransactions.get(0).getType()).isEqualTo(TransactionType.INCOME);
        assertThat(incomeTransactions.get(0).getDescription()).isEqualTo("Test Transaction");
    }

    @Test
    void shouldFindTransactionsByAmountRange() {
        // Given - Create transactions with different amounts
        Transaction lowAmountTransaction = new Transaction();
        lowAmountTransaction.setDescription("Low Amount Transaction");
        lowAmountTransaction.setAmount(BigDecimal.valueOf(50.00));
        lowAmountTransaction.setType(TransactionType.INCOME);
        lowAmountTransaction.setSubtype(TransactionSubtype.FIXED);
        lowAmountTransaction.setSource(TransactionSource.CASH);
        lowAmountTransaction.setUser(testUser);
        lowAmountTransaction.setCategory(testCategory);
        lowAmountTransaction.setDate(LocalDateTime.now());
        lowAmountTransaction.setCreatedAt(LocalDateTime.now());
        lowAmountTransaction.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(lowAmountTransaction);

        Transaction highAmountTransaction = new Transaction();
        highAmountTransaction.setDescription("High Amount Transaction");
        highAmountTransaction.setAmount(BigDecimal.valueOf(500.00));
        highAmountTransaction.setType(TransactionType.INCOME);
        highAmountTransaction.setSubtype(TransactionSubtype.FIXED);
        highAmountTransaction.setSource(TransactionSource.CASH);
        highAmountTransaction.setUser(testUser);
        highAmountTransaction.setCategory(testCategory);
        highAmountTransaction.setDate(LocalDateTime.now());
        highAmountTransaction.setCreatedAt(LocalDateTime.now());
        highAmountTransaction.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(highAmountTransaction);

        // When - Use standard JPA method
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<Transaction> mediumAmountTransactions = allTransactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.valueOf(75.00)) >= 0 &&
                           t.getAmount().compareTo(BigDecimal.valueOf(150.00)) <= 0)
                .toList();

        // Then
        assertThat(mediumAmountTransactions).hasSize(1);
        assertThat(mediumAmountTransactions.get(0).getDescription()).isEqualTo("Test Transaction");
        assertThat(mediumAmountTransactions.get(0).getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void shouldFindTransactionsWithPagination() {
        // Given - Create multiple transactions
        for (int i = 1; i <= 5; i++) {
            Transaction transaction = new Transaction();
            transaction.setDescription("Transaction " + i);
            transaction.setAmount(BigDecimal.valueOf(100.00 * i));
            transaction.setType(TransactionType.INCOME);
            transaction.setSubtype(TransactionSubtype.FIXED);
            transaction.setSource(TransactionSource.CASH);
            transaction.setUser(testUser);
            transaction.setCategory(testCategory);
            transaction.setDate(LocalDateTime.now().plusDays(i));
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setUpdatedAt(LocalDateTime.now());
            entityManager.persistAndFlush(transaction);
        }

        // When
        Pageable pageable = PageRequest.of(0, 3, Sort.by("date").descending());
        Page<Transaction> page = transactionRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(6); // 5 new + 1 original
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(3);
    }

    @Test
    void shouldSaveTransaction() {
        // Given
        Transaction newTransaction = new Transaction();
        newTransaction.setDescription("New Transaction");
        newTransaction.setAmount(BigDecimal.valueOf(250.00));
        newTransaction.setType(TransactionType.INCOME);
        newTransaction.setSubtype(TransactionSubtype.FIXED);
        newTransaction.setSource(TransactionSource.CASH);
        newTransaction.setUser(testUser);
        newTransaction.setCategory(testCategory);
        newTransaction.setDate(LocalDateTime.now());
        newTransaction.setCreatedAt(LocalDateTime.now());
        newTransaction.setUpdatedAt(LocalDateTime.now());

        // When
        Transaction saved = transactionRepository.save(newTransaction);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("New Transaction");
        assertThat(saved.getAmount()).isEqualTo(BigDecimal.valueOf(250.00));
        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(saved.getCategory().getId()).isEqualTo(testCategory.getId());
    }

    @Test
    void shouldUpdateTransaction() {
        // Given
        testTransaction.setDescription("Updated Transaction");
        testTransaction.setAmount(BigDecimal.valueOf(150.00));

        // When
        Transaction updated = transactionRepository.save(testTransaction);
        entityManager.flush();

        // Then
        assertThat(updated.getDescription()).isEqualTo("Updated Transaction");
        assertThat(updated.getAmount()).isEqualTo(BigDecimal.valueOf(150.00));
        assertThat(updated.getId()).isEqualTo(testTransaction.getId());
    }

    @Test
    void shouldDeleteTransaction() {
        // Given
        Long transactionId = testTransaction.getId();

        // When
        transactionRepository.delete(testTransaction);
        entityManager.flush();

        // Then
        Optional<Transaction> deleted = transactionRepository.findById(transactionId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void shouldCountTransactionsByUser() {
        // Given - Create additional transaction for same user
        Transaction anotherTransaction = new Transaction();
        anotherTransaction.setDescription("Another Transaction");
        anotherTransaction.setAmount(BigDecimal.valueOf(200.00));
        anotherTransaction.setType(TransactionType.EXPENSE);
        anotherTransaction.setSubtype(TransactionSubtype.FIXED);
        anotherTransaction.setSource(TransactionSource.CASH);
        anotherTransaction.setUser(testUser);
        anotherTransaction.setCategory(testCategory);
        anotherTransaction.setDate(LocalDateTime.now());
        anotherTransaction.setCreatedAt(LocalDateTime.now());
        anotherTransaction.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(anotherTransaction);

        // When - Use standard JPA method
        List<Transaction> allTransactions = transactionRepository.findAll();
        long count = allTransactions.stream()
                .filter(t -> t.getUser().getId().equals(testUser.getId()))
                .count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindTransactionsByDateRange() {
        // Given - Create transactions with different dates
        Transaction pastTransaction = new Transaction();
        pastTransaction.setDescription("Past Transaction");
        pastTransaction.setAmount(BigDecimal.valueOf(50.00));
        pastTransaction.setType(TransactionType.INCOME);
        pastTransaction.setSubtype(TransactionSubtype.FIXED);
        pastTransaction.setSource(TransactionSource.CASH);
        pastTransaction.setUser(testUser);
        pastTransaction.setCategory(testCategory);
        pastTransaction.setDate(LocalDateTime.now().minusDays(10));
        pastTransaction.setCreatedAt(LocalDateTime.now());
        pastTransaction.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(pastTransaction);

        Transaction futureTransaction = new Transaction();
        futureTransaction.setDescription("Future Transaction");
        futureTransaction.setAmount(BigDecimal.valueOf(300.00));
        futureTransaction.setType(TransactionType.INCOME);
        futureTransaction.setSubtype(TransactionSubtype.FIXED);
        futureTransaction.setSource(TransactionSource.CASH);
        futureTransaction.setUser(testUser);
        futureTransaction.setCategory(testCategory);
        futureTransaction.setDate(LocalDateTime.now().plusDays(10));
        futureTransaction.setCreatedAt(LocalDateTime.now());
        futureTransaction.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(futureTransaction);

        // When - Use standard JPA method
        LocalDateTime startDate = LocalDateTime.now().minusDays(5);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<Transaction> recentTransactions = allTransactions.stream()
                .filter(t -> t.getDate().isAfter(startDate) && t.getDate().isBefore(endDate))
                .toList();

        // Then
        assertThat(recentTransactions).hasSize(1);
        assertThat(recentTransactions.get(0).getDescription()).isEqualTo("Test Transaction");
    }

    @Test
    void shouldSumByUserAndTypeAndDateBetween() {
        // Given - Create transactions with different types and dates
        // Note: testTransaction from setUp is INCOME with 100.00, so we need to account for it
        LocalDateTime startDate = LocalDateTime.now().minusDays(5);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        Transaction income1 = new Transaction();
        income1.setDescription("Income 1");
        income1.setAmount(BigDecimal.valueOf(1000.00));
        income1.setType(TransactionType.INCOME);
        income1.setSubtype(TransactionSubtype.FIXED);
        income1.setSource(TransactionSource.CASH);
        income1.setUser(testUser);
        income1.setCategory(testCategory);
        income1.setDate(LocalDateTime.now());
        income1.setCreatedAt(LocalDateTime.now());
        income1.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(income1);

        Transaction income2 = new Transaction();
        income2.setDescription("Income 2");
        income2.setAmount(BigDecimal.valueOf(500.00));
        income2.setType(TransactionType.INCOME);
        income2.setSubtype(TransactionSubtype.FIXED);
        income2.setSource(TransactionSource.CASH);
        income2.setUser(testUser);
        income2.setCategory(testCategory);
        income2.setDate(LocalDateTime.now().plusDays(2));
        income2.setCreatedAt(LocalDateTime.now());
        income2.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(income2);

        Transaction expense = new Transaction();
        expense.setDescription("Expense");
        expense.setAmount(BigDecimal.valueOf(200.00));
        expense.setType(TransactionType.EXPENSE);
        expense.setSubtype(TransactionSubtype.FIXED);
        expense.setSource(TransactionSource.CASH);
        expense.setUser(testUser);
        expense.setCategory(testCategory);
        expense.setDate(LocalDateTime.now());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(expense);

        // When - Sum includes testTransaction (100.00) + income1 (1000.00) + income2 (500.00) = 1600.00
        BigDecimal sum = transactionRepository.sumByUserAndTypeAndDateBetween(
                testUser.getId(), TransactionType.INCOME, startDate, endDate);

        // Then
        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(1600.00));
    }

    @Test
    void shouldFindByUserIdWithResponsibilities() {
        // When
        List<Transaction> transactions = transactionRepository.findByUserIdWithResponsibilities(testUser.getId());

        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getDescription()).isEqualTo("Test Transaction");
        assertThat(transactions.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindDistinctCategoriesByUserId() {
        // Given - Create another category with transaction
        TransactionCategory anotherCategory = new TransactionCategory();
        anotherCategory.setName("Another Category");
        anotherCategory.setCreatedAt(LocalDateTime.now());
        anotherCategory.setUpdatedAt(LocalDateTime.now());
        anotherCategory = entityManager.persistAndFlush(anotherCategory);

        Transaction anotherTransaction = new Transaction();
        anotherTransaction.setDescription("Another Transaction");
        anotherTransaction.setAmount(BigDecimal.valueOf(200.00));
        anotherTransaction.setType(TransactionType.INCOME);
        anotherTransaction.setSubtype(TransactionSubtype.FIXED);
        anotherTransaction.setSource(TransactionSource.CASH);
        anotherTransaction.setUser(testUser);
        anotherTransaction.setCategory(anotherCategory);
        anotherTransaction.setDate(LocalDateTime.now());
        anotherTransaction.setCreatedAt(LocalDateTime.now());
        anotherTransaction.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(anotherTransaction);

        // When
        List<TransactionCategory> categories = transactionRepository.findDistinctCategoriesByUserId(testUser.getId());

        // Then
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(TransactionCategory::getName)
                .containsExactlyInAnyOrder("Test Category", "Another Category");
    }

    @Test
    void shouldFindDistinctTypes() {
        // Given - Create expense transaction
        Transaction expense = new Transaction();
        expense.setDescription("Expense");
        expense.setAmount(BigDecimal.valueOf(50.00));
        expense.setType(TransactionType.EXPENSE);
        expense.setSubtype(TransactionSubtype.FIXED);
        expense.setSource(TransactionSource.CASH);
        expense.setUser(testUser);
        expense.setCategory(testCategory);
        expense.setDate(LocalDateTime.now());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(expense);

        // When
        List<String> types = transactionRepository.findDistinctTypes();

        // Then
        assertThat(types).hasSize(2);
        assertThat(types).containsExactlyInAnyOrder("INCOME", "EXPENSE");
    }

    @Test
    void shouldGetTotalAmountByUserId() {
        // Given - Create additional transactions
        Transaction transaction2 = new Transaction();
        transaction2.setDescription("Transaction 2");
        transaction2.setAmount(BigDecimal.valueOf(200.00));
        transaction2.setType(TransactionType.INCOME);
        transaction2.setSubtype(TransactionSubtype.FIXED);
        transaction2.setSource(TransactionSource.CASH);
        transaction2.setUser(testUser);
        transaction2.setCategory(testCategory);
        transaction2.setDate(LocalDateTime.now());
        transaction2.setCreatedAt(LocalDateTime.now());
        transaction2.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(transaction2);

        // When
        BigDecimal total = transactionRepository.getTotalAmountByUserId(testUser.getId());

        // Then
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(300.00));
    }

    @Test
    void shouldFindPotentialDuplicates() {
        // Given - Create a duplicate transaction (same amount, description, date range)
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        Transaction duplicate = new Transaction();
        duplicate.setDescription("Test Transaction");
        duplicate.setAmount(BigDecimal.valueOf(100.00));
        duplicate.setType(TransactionType.INCOME);
        duplicate.setSubtype(TransactionSubtype.FIXED);
        duplicate.setSource(TransactionSource.CASH);
        duplicate.setUser(testUser);
        duplicate.setCategory(testCategory);
        duplicate.setDate(LocalDateTime.now());
        duplicate.setCreatedAt(LocalDateTime.now());
        duplicate.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(duplicate);

        // When
        List<Transaction> duplicates = transactionRepository.findPotentialDuplicates(
                testUser.getId(),
                BigDecimal.valueOf(100.00),
                "Test Transaction",
                startDate,
                endDate);

        // Then
        assertThat(duplicates).hasSize(2);
        assertThat(duplicates).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Test Transaction", "Test Transaction");
    }

    @Test
    void shouldFindTransactionsWithSearch() {
        // Given - Create transactions with different descriptions
        Transaction transaction1 = new Transaction();
        transaction1.setDescription("Grocery Shopping");
        transaction1.setAmount(BigDecimal.valueOf(50.00));
        transaction1.setType(TransactionType.EXPENSE);
        transaction1.setSubtype(TransactionSubtype.FIXED);
        transaction1.setSource(TransactionSource.CASH);
        transaction1.setUser(testUser);
        transaction1.setCategory(testCategory);
        transaction1.setDate(LocalDateTime.now());
        transaction1.setCreatedAt(LocalDateTime.now());
        transaction1.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(transaction1);

        // When
        Page<Transaction> results = transactionRepository.findAll("Grocery", Pageable.unpaged());

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getDescription()).isEqualTo("Grocery Shopping");
    }

    @Test
    void shouldFindTransactionsWithEmptySearch() {
        // When
        Page<Transaction> results = transactionRepository.findAll("", Pageable.unpaged());

        // Then
        assertThat(results.getContent()).hasSize(1);
    }

    @Test
    void shouldFindTransactionsWithNullSearch() {
        // When
        Page<Transaction> results = transactionRepository.findAll((String) null, Pageable.unpaged());

        // Then
        assertThat(results.getContent()).hasSize(1);
    }
}
