package com.finance_control.dashboard.service;

import com.finance_control.dashboard.dto.*;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.enums.GoalType;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardService.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FinancialGoalRepository financialGoalRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;
    private TransactionCategory testCategory;
    private FinancialGoal testGoal;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testCategory = new TransactionCategory();
        testCategory.setId(1L);
        testCategory.setName("Food & Dining");

        testGoal = new FinancialGoal();
        testGoal.setId(1L);
        testGoal.setName("Emergency Fund");
        testGoal.setTargetAmount(new BigDecimal("10000"));
        testGoal.setCurrentAmount(new BigDecimal("5000"));
        testGoal.setGoalType(GoalType.SAVINGS);
        testGoal.setIsActive(true);
    }

    @Test
    void getDashboardSummary_ShouldReturnCompleteSummary() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            userContextMock.when(UserContext::getCurrentUserId).thenReturn(1L);

            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            when(transactionRepository.sumByUserAndTypeAndDateBetween(
                    eq(1L), eq(TransactionType.INCOME), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("5000"));
            when(transactionRepository.sumByUserAndTypeAndDateBetween(
                    eq(1L), eq(TransactionType.EXPENSE), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("3000"));
            when(financialGoalRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(1L))
                    .thenReturn(Arrays.asList(testGoal));
            when(financialGoalRepository.findCompletedGoals(1L))
                    .thenReturn(Arrays.asList());
            when(transactionRepository.findByUserIdWithResponsibilities(1L))
                    .thenReturn(Arrays.asList());

            DashboardSummaryDTO result = dashboardService.getDashboardSummary();

            assertThat(result).isNotNull();
            assertThat(result.getTotalIncome()).isEqualTo(new BigDecimal("5000"));
            assertThat(result.getTotalExpenses()).isEqualTo(new BigDecimal("3000"));
            assertThat(result.getMonthlyBalance()).isEqualTo(new BigDecimal("2000"));
            assertThat(result.getActiveGoals()).isEqualTo(1);
            assertThat(result.getCompletedGoals()).isEqualTo(0);
            assertThat(result.getSavingsRate()).isEqualTo(new BigDecimal("40.0000"));
        }
    }

    @Test
    void getFinancialMetrics_ShouldReturnDetailedMetrics() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            userContextMock.when(UserContext::getCurrentUserId).thenReturn(1L);

            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            when(transactionRepository.sumByUserAndTypeAndDateBetween(
                    eq(1L), eq(TransactionType.INCOME), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("5000"));
            when(transactionRepository.sumByUserAndTypeAndDateBetween(
                    eq(1L), eq(TransactionType.EXPENSE), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("3000"));
            when(transactionRepository.findByUserIdWithResponsibilities(1L))
                    .thenReturn(Arrays.asList());

            FinancialMetricsDTO result = dashboardService.getFinancialMetrics(startDate, endDate);

            assertThat(result).isNotNull();
            assertThat(result.getMonthlyIncome()).isEqualTo(new BigDecimal("5000"));
            assertThat(result.getMonthlyExpenses()).isEqualTo(new BigDecimal("3000"));
            assertThat(result.getMonthlySavings()).isEqualTo(new BigDecimal("2000"));
            assertThat(result.getSavingsRate()).isEqualTo(new BigDecimal("40.0000"));
            assertThat(result.getPeriodStart()).isEqualTo(startDate);
            assertThat(result.getPeriodEnd()).isEqualTo(endDate);
        }
    }

    @Test
    void getTopSpendingCategories_ShouldReturnSortedCategories() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            userContextMock.when(UserContext::getCurrentUserId).thenReturn(1L);

            Transaction expense1 = createTestTransaction("Food", new BigDecimal("500"));
            Transaction expense2 = createTestTransaction("Transportation", new BigDecimal("300"));
            Transaction expense3 = createTestTransaction("Entertainment", new BigDecimal("200"));

            when(transactionRepository.findByUserIdWithResponsibilities(1L))
                    .thenReturn(Arrays.asList(expense1, expense2, expense3));

            List<CategorySpendingDTO> result = dashboardService.getTopSpendingCategories(1L, 3);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getCategoryName()).isEqualTo("Food");
            assertThat(result.get(0).getAmount()).isEqualTo(new BigDecimal("500"));
            assertThat(result.get(1).getCategoryName()).isEqualTo("Transportation");
            assertThat(result.get(2).getCategoryName()).isEqualTo("Entertainment");
        }
    }

    @Test
    void getMonthlyTrends_ShouldReturnTrendData() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            userContextMock.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(transactionRepository.sumByUserAndTypeAndDateBetween(
                    eq(1L), eq(TransactionType.INCOME), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("5000"));
            when(transactionRepository.sumByUserAndTypeAndDateBetween(
                    eq(1L), eq(TransactionType.EXPENSE), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("3000"));
            when(transactionRepository.findByUserIdWithResponsibilities(1L))
                    .thenReturn(Arrays.asList());

            List<MonthlyTrendDTO> result = dashboardService.getMonthlyTrends(1L, 6);

            assertThat(result).hasSize(6);
            assertThat(result.get(0).getIncome()).isEqualTo(new BigDecimal("5000"));
            assertThat(result.get(0).getExpenses()).isEqualTo(new BigDecimal("3000"));
            assertThat(result.get(0).getBalance()).isEqualTo(new BigDecimal("2000"));
        }
    }

    @Test
    void calculateSavingsRate_WithZeroIncome_ShouldReturnZero() {
        BigDecimal result = dashboardService.calculateSavingsRate(BigDecimal.ZERO, new BigDecimal("1000"));
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void calculateSavingsRate_WithValidData_ShouldReturnCorrectRate() {
        BigDecimal result = dashboardService.calculateSavingsRate(new BigDecimal("5000"), new BigDecimal("3000"));
        assertThat(result).isEqualTo(new BigDecimal("40.0000"));
    }

    private Transaction createTestTransaction(String categoryName, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.EXPENSE);
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());

        TransactionCategory category = new TransactionCategory();
        category.setName(categoryName);
        transaction.setCategory(category);

        return transaction;
    }
}
