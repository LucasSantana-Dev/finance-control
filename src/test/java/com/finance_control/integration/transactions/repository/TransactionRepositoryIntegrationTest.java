package com.finance_control.integration.transactions.repository;

import com.finance_control.integration.BaseIntegrationTest;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionCategoryRepository categoryRepository;

    @Autowired
    private TransactionResponsiblesRepository responsibleRepository;

    private User testUser;
    private TransactionCategory category1;
    private TransactionCategory category2;
    private TransactionResponsibles responsible1;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("repostest@example.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        category1 = new TransactionCategory();
        category1.setName("Category 1");
        category1 = categoryRepository.save(category1);

        category2 = new TransactionCategory();
        category2.setName("Category 2");
        category2 = categoryRepository.save(category2);

        responsible1 = new TransactionResponsibles();
        responsible1.setName("Responsible 1");
        responsible1 = responsibleRepository.save(responsible1);
    }

    @Test
    void shouldFindDistinctCategoriesByUserId() {
        Transaction tx1 = createTransaction("TX1", BigDecimal.valueOf(100), category1);
        Transaction tx2 = createTransaction("TX2", BigDecimal.valueOf(200), category1);
        Transaction tx3 = createTransaction("TX3", BigDecimal.valueOf(300), category2);
        transactionRepository.saveAll(List.of(tx1, tx2, tx3));

        List<TransactionCategory> categories = transactionRepository.findDistinctCategoriesByUserId(testUser.getId());

        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(TransactionCategory::getName)
                .containsExactlyInAnyOrder("Category 1", "Category 2");
    }

    @Test
    void shouldFindDistinctTypes() {
        Transaction income = createTransaction("Income", BigDecimal.valueOf(100), category1);
        income.setType(TransactionType.INCOME);
        Transaction expense = createTransaction("Expense", BigDecimal.valueOf(50), category1);
        expense.setType(TransactionType.EXPENSE);
        transactionRepository.saveAll(List.of(income, expense));

        List<String> types = transactionRepository.findDistinctTypes();

        assertThat(types).containsExactlyInAnyOrder("INCOME", "EXPENSE");
    }

    @Test
    void shouldGetTotalAmountByUserId() {
        Transaction tx1 = createTransaction("TX1", BigDecimal.valueOf(100), category1);
        Transaction tx2 = createTransaction("TX2", BigDecimal.valueOf(200), category1);
        Transaction tx3 = createTransaction("TX3", BigDecimal.valueOf(300), category1);
        transactionRepository.saveAll(List.of(tx1, tx2, tx3));

        BigDecimal total = transactionRepository.getTotalAmountByUserId(testUser.getId());

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(600));
    }

    @Test
    void shouldGetTotalAmountByUserId_WhenNoTransactions_ReturnZero() {
        BigDecimal total = transactionRepository.getTotalAmountByUserId(testUser.getId());

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void shouldSumByUserAndTypeAndDateBetween() {
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        Transaction income = createTransaction("Income", BigDecimal.valueOf(500), category1);
        income.setType(TransactionType.INCOME);
        income.setDate(LocalDateTime.now());
        Transaction expense = createTransaction("Expense", BigDecimal.valueOf(200), category1);
        expense.setType(TransactionType.EXPENSE);
        expense.setDate(LocalDateTime.now());
        Transaction outsideRange = createTransaction("Outside", BigDecimal.valueOf(100), category1);
        outsideRange.setType(TransactionType.INCOME);
        outsideRange.setDate(LocalDateTime.now().minusDays(10));
        transactionRepository.saveAll(List.of(income, expense, outsideRange));

        BigDecimal sum = transactionRepository.sumByUserAndTypeAndDateBetween(
                testUser.getId(), TransactionType.INCOME, start, end);

        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void shouldFindByUserIdWithResponsibilities() {
        Transaction tx = createTransaction("TX1", BigDecimal.valueOf(100), category1);
        tx.addResponsible(responsible1, BigDecimal.valueOf(100.00), "Test");
        transactionRepository.save(tx);

        List<Transaction> transactions = transactionRepository.findByUserIdWithResponsibilities(testUser.getId());

        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getResponsibilities()).isNotEmpty();
        assertThat(transactions.get(0).getResponsibilities().get(0).getResponsible().getName())
                .isEqualTo("Responsible 1");
    }

    private Transaction createTransaction(String description, BigDecimal amount, TransactionCategory category) {
        Transaction tx = new Transaction();
        tx.setDescription(description);
        tx.setAmount(amount);
        tx.setType(TransactionType.INCOME);
        tx.setSubtype(TransactionSubtype.FIXED);
        tx.setSource(TransactionSource.CASH);
        tx.setUser(testUser);
        tx.setCategory(category);
        tx.setDate(LocalDateTime.now());
        return tx;
    }
}
