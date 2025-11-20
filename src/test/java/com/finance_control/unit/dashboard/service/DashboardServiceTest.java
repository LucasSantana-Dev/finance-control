package com.finance_control.unit.dashboard.service;

import com.finance_control.dashboard.dto.*;
import com.finance_control.dashboard.service.DashboardService;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.enums.GoalType;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.service.SupabaseRealtimeService;

import java.time.Instant;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;

/**
 * Unit tests for DashboardService.
 * Tests dashboard summary generation and financial metrics calculation.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FinancialGoalRepository financialGoalRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private SupabaseRealtimeService realtimeService;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;
    private TransactionCategory testCategory;
    private Instant timerSample;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testCategory = new TransactionCategory();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        timerSample = Instant.now();
        lenient().when(metricsService.startDashboardGenerationTimer()).thenReturn(timerSample);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getDashboardSummary_ShouldReturnCompleteSummary() {
        // Given
        Transaction incomeTransaction = createTransaction(TransactionType.INCOME, BigDecimal.valueOf(5000.00));
        Transaction expenseTransaction = createTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(3000.00));

        // Mock repository calls - use Answer to distinguish between monthly and year-to-date calls
        // Monthly calls use start/end of month, year-to-date uses start/end of year
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenAnswer((Answer<BigDecimal>) invocation -> {
                    LocalDateTime startDate = invocation.getArgument(2);
                    // If start date is day 1 of year, it's a year-to-date call
                    if (startDate.toLocalDate().getDayOfYear() == 1) {
                        return BigDecimal.valueOf(60000.00); // Year-to-date income
                    }
                    return BigDecimal.valueOf(5000.00); // Monthly income
                });
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenAnswer((Answer<BigDecimal>) invocation -> {
                    LocalDateTime startDate = invocation.getArgument(2);
                    // If start date is day 1 of year, it's a year-to-date call
                    if (startDate.toLocalDate().getDayOfYear() == 1) {
                        return BigDecimal.valueOf(40000.00); // Year-to-date expenses
                    }
                    return BigDecimal.valueOf(3000.00); // Monthly expenses
                });

        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(List.of(incomeTransaction, expenseTransaction));

        FinancialGoal activeGoal = createFinancialGoal(true);
        FinancialGoal completedGoal = createFinancialGoal(true);
        completedGoal.setCurrentAmount(completedGoal.getTargetAmount()); // Mark as completed

        when(financialGoalRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(activeGoal));
        when(financialGoalRepository.findCompletedGoals(1L))
                .thenReturn(List.of(completedGoal));

        // When
        DashboardSummaryDTO result = dashboardService.getDashboardSummary();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(result.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(3000.00));
        assertThat(result.getMonthlyBalance()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
        assertThat(result.getSavingsRate()).isEqualByComparingTo(BigDecimal.valueOf(40.00)); // (5000-3000)/5000 * 100
        assertThat(result.getNetWorth()).isEqualByComparingTo(BigDecimal.valueOf(20000.00)); // 60000 - 40000
        assertThat(result.getActiveGoals()).isEqualTo(1);
        assertThat(result.getCompletedGoals()).isEqualTo(1);
        assertThat(result.getTotalTransactions()).isEqualTo(2);
        assertThat(result.getPendingReconciliations()).isEqualTo(2); // Both transactions unreconciled
        assertThat(result.getLastUpdated()).isNotNull();

        verify(metricsService).startDashboardGenerationTimer();
        verify(metricsService).recordDashboardGenerationTime(timerSample);
    }

    @Test
    void getDashboardSummary_WithZeroIncome_ShouldReturnZeroSavingsRate() {
        // Given
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000.00));
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(new ArrayList<>());

        when(financialGoalRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(new ArrayList<>());
        when(financialGoalRepository.findCompletedGoals(1L))
                .thenReturn(new ArrayList<>());

        // When
        DashboardSummaryDTO result = dashboardService.getDashboardSummary();

        // Then
        assertThat(result.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getFinancialMetrics_WithValidDates_ShouldReturnCompleteMetrics() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        Transaction incomeTransaction = createTransaction(TransactionType.INCOME, BigDecimal.valueOf(5000.00));
        incomeTransaction.setDate(LocalDateTime.of(2024, 1, 15, 12, 0)); // Within date range
        Transaction expenseTransaction = createTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(3000.00));
        expenseTransaction.setDate(LocalDateTime.of(2024, 1, 20, 12, 0)); // Within date range

        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.valueOf(5000.00));
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.valueOf(3000.00));
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(List.of(incomeTransaction, expenseTransaction));

        // When
        FinancialMetricsDTO result = dashboardService.getFinancialMetrics(startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMonthlyIncome()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(result.getMonthlyExpenses()).isEqualByComparingTo(BigDecimal.valueOf(3000.00));
        assertThat(result.getMonthlySavings()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
        assertThat(result.getSavingsRate()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
        assertThat(result.getAverageTransactionAmount()).isEqualByComparingTo(BigDecimal.valueOf(4000.00)); // (5000+3000)/2
        assertThat(result.getLargestTransaction()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(result.getSmallestTransaction()).isEqualByComparingTo(BigDecimal.valueOf(3000.00));
        assertThat(result.getTotalTransactions()).isEqualTo(2);
        assertThat(result.getIncomeTransactions()).isEqualTo(1);
        assertThat(result.getExpenseTransactions()).isEqualTo(1);
        assertThat(result.getPeriodStart()).isEqualTo(startDate);
        assertThat(result.getPeriodEnd()).isEqualTo(endDate);
    }

    @Test
    void getFinancialMetrics_WithNullStartDate_ShouldThrowException() {
        assertThatThrownBy(() -> dashboardService.getFinancialMetrics(null, LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date and end date cannot be null");
    }

    @Test
    void getFinancialMetrics_WithNullEndDate_ShouldThrowException() {
        assertThatThrownBy(() -> dashboardService.getFinancialMetrics(LocalDate.now(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date and end date cannot be null");
    }

    @Test
    void getFinancialMetrics_WithEmptyTransactionList_ShouldReturnZeroValues() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        lenient().when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        lenient().when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(new ArrayList<>());

        // When
        FinancialMetricsDTO result = dashboardService.getFinancialMetrics(startDate, endDate);

        // Then
        assertThat(result.getAverageTransactionAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getLargestTransaction()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getSmallestTransaction()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalTransactions()).isEqualTo(0);
        assertThat(result.getIncomeTransactions()).isEqualTo(0);
        assertThat(result.getExpenseTransactions()).isEqualTo(0);
    }

    @Test
    void getTopSpendingCategories_ShouldReturnSortedCategories() {
        // Given
        LocalDate currentMonthStart = java.time.YearMonth.now().atDay(1);

        TransactionCategory category1 = new TransactionCategory();
        category1.setId(1L);
        category1.setName("Food");

        TransactionCategory category2 = new TransactionCategory();
        category2.setId(2L);
        category2.setName("Transport");

        Transaction expense1 = createTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(1000.00));
        expense1.setCategory(category1);
        expense1.setDate(currentMonthStart.plusDays(5).atStartOfDay()); // Within current month

        Transaction expense2 = createTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(500.00));
        expense2.setCategory(category2);
        expense2.setDate(currentMonthStart.plusDays(10).atStartOfDay()); // Within current month

        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(List.of(expense1, expense2));

        // When
        List<CategorySpendingDTO> result = dashboardService.getTopSpendingCategories(1L, 5);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Food");
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(result.get(0).getPercentage()).isEqualByComparingTo(BigDecimal.valueOf(66.67)); // 1000/1500 * 100
        assertThat(result.get(1).getCategoryName()).isEqualTo("Transport");
        assertThat(result.get(1).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }

    @Test
    void getTopSpendingCategories_WithZeroExpenses_ShouldReturnZeroPercentages() {
        // Given
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(new ArrayList<>());

        // When
        List<CategorySpendingDTO> result = dashboardService.getTopSpendingCategories(1L, 5);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getMonthlyTrends_ShouldReturnLastNMonths() {
        // Given
        LocalDate currentMonthStart = java.time.YearMonth.now().atDay(1);

        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.valueOf(5000.00));
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.valueOf(3000.00));

        // Create transactions for each of the last 3 months
        Transaction t1 = createTransaction(TransactionType.INCOME, BigDecimal.valueOf(1000.00));
        t1.setDate(currentMonthStart.minusMonths(2).plusDays(5).atStartOfDay());
        Transaction t2 = createTransaction(TransactionType.INCOME, BigDecimal.valueOf(1000.00));
        t2.setDate(currentMonthStart.minusMonths(1).plusDays(5).atStartOfDay());
        Transaction t3 = createTransaction(TransactionType.INCOME, BigDecimal.valueOf(1000.00));
        t3.setDate(currentMonthStart.plusDays(5).atStartOfDay());

        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(List.of(t1, t2, t3));

        // When
        List<MonthlyTrendDTO> result = dashboardService.getMonthlyTrends(1L, 3);

        // Then
        assertThat(result).hasSize(3);
        result.forEach(trend -> {
            assertThat(trend.getIncome()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
            assertThat(trend.getExpenses()).isEqualByComparingTo(BigDecimal.valueOf(3000.00));
            assertThat(trend.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
            assertThat(trend.getTransactionCount()).isEqualTo(1);
        });
    }

    @Test
    void notifyDashboardUpdate_WithRealtimeService_ShouldSendNotification() {
        // Given - Set realtimeService on dashboardService using reflection since it's field-injected
        try {
            java.lang.reflect.Field field = DashboardService.class.getDeclaredField("realtimeService");
            field.setAccessible(true);
            field.set(dashboardService, realtimeService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set realtimeService", e);
        }

        // When
        dashboardService.notifyDashboardUpdate(1L);

        // Then
        verify(realtimeService).notifyDashboardUpdate(eq(1L), any());
    }

    @Test
    void notifyDashboardUpdate_WithoutRealtimeService_ShouldNotSendNotification() {
        // Given - Create a DashboardService without realtimeService
        DashboardService serviceWithoutRealtime = new DashboardService(
                transactionRepository,
                financialGoalRepository,
                metricsService
        );

        // When - Should not throw exception
        serviceWithoutRealtime.notifyDashboardUpdate(1L);

        // Then - No exception should be thrown (test passes if no exception is thrown)
        // Note: We can't verify the mock since it's not part of this service instance
    }

    @Test
    void notifyDashboardUpdate_WithRealtimeServiceThrowingException_ShouldLogWarning() {
        // Given - Set realtimeService on dashboardService using reflection since it's field-injected
        try {
            java.lang.reflect.Field field = DashboardService.class.getDeclaredField("realtimeService");
            field.setAccessible(true);
            field.set(dashboardService, realtimeService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set realtimeService", e);
        }

        doThrow(new RuntimeException("Realtime service error"))
                .when(realtimeService).notifyDashboardUpdate(anyLong(), any());

        // When - Should not throw exception (exception is caught and logged)
        dashboardService.notifyDashboardUpdate(1L);

        // Then - Verify the service was called (exception was thrown and caught)
        verify(realtimeService).notifyDashboardUpdate(eq(1L), any());
    }

    @Test
    void calculateSavingsRate_WithZeroIncome_ShouldReturnZero() {
        BigDecimal result = dashboardService.calculateSavingsRate(BigDecimal.ZERO, BigDecimal.valueOf(1000.00));

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateSavingsRate_WithPositiveIncome_ShouldCalculateCorrectly() {
        BigDecimal result = dashboardService.calculateSavingsRate(BigDecimal.valueOf(5000.00), BigDecimal.valueOf(3000.00));

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(40.00));
    }

    @Test
    void calculateSavingsRate_WithNegativeSavings_ShouldCalculateCorrectly() {
        BigDecimal result = dashboardService.calculateSavingsRate(BigDecimal.valueOf(3000.00), BigDecimal.valueOf(5000.00));

        // Calculation: (3000 - 5000) / 3000 * 100 = -2000 / 3000 * 100 = -66.6666...
        // With 4 decimal places and HALF_UP rounding: -66.67
        assertThat(result).isEqualByComparingTo(new BigDecimal("-66.6700"));
    }

    @Test
    void calculateSavingsRate_WithNegativeIncome_ShouldReturnZero() {
        BigDecimal result = dashboardService.calculateSavingsRate(BigDecimal.valueOf(-1000.00), BigDecimal.valueOf(500.00));

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getDashboardSummary_WithEmptyGoals_ShouldReturnZeroProgress() {
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(new ArrayList<>());

        when(financialGoalRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(new ArrayList<>());
        when(financialGoalRepository.findCompletedGoals(1L))
                .thenReturn(new ArrayList<>());

        DashboardSummaryDTO result = dashboardService.getDashboardSummary();

        assertThat(result.getTotalGoalProgress()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getActiveGoals()).isEqualTo(0);
        assertThat(result.getCompletedGoals()).isEqualTo(0);
    }

    @Test
    void getDashboardSummary_WithMultipleGoals_ShouldCalculateAverageProgress() {
        FinancialGoal goal1 = createFinancialGoal(true);
        goal1.setCurrentAmount(BigDecimal.valueOf(5000.00));
        goal1.setTargetAmount(BigDecimal.valueOf(10000.00));

        FinancialGoal goal2 = createFinancialGoal(true);
        goal2.setCurrentAmount(BigDecimal.valueOf(3000.00));
        goal2.setTargetAmount(BigDecimal.valueOf(10000.00));

        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(new ArrayList<>());

        when(financialGoalRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(goal1, goal2));
        when(financialGoalRepository.findCompletedGoals(1L))
                .thenReturn(new ArrayList<>());

        DashboardSummaryDTO result = dashboardService.getDashboardSummary();

        assertThat(result.getTotalGoalProgress()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
    }

    @Test
    void getFinancialMetrics_WithSingleTransaction_ShouldReturnCorrectValues() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        Transaction singleTransaction = createTransaction(TransactionType.INCOME, BigDecimal.valueOf(1000.00));
        singleTransaction.setDate(LocalDateTime.of(2024, 1, 15, 12, 0)); // Within date range

        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000.00));
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(List.of(singleTransaction));

        FinancialMetricsDTO result = dashboardService.getFinancialMetrics(startDate, endDate);

        assertThat(result.getAverageTransactionAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(result.getLargestTransaction()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(result.getSmallestTransaction()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }

    @Test
    void getFinancialMetrics_WithMultipleTransactions_ShouldCalculateCorrectly() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        Transaction t1 = createTransaction(TransactionType.INCOME, BigDecimal.valueOf(1000.00));
        t1.setDate(LocalDateTime.of(2024, 1, 15, 12, 0)); // Within date range
        Transaction t2 = createTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(500.00));
        t2.setDate(LocalDateTime.of(2024, 1, 20, 12, 0)); // Within date range
        Transaction t3 = createTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(200.00));
        t3.setDate(LocalDateTime.of(2024, 1, 25, 12, 0)); // Within date range

        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000.00));
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.valueOf(700.00));
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(List.of(t1, t2, t3));

        FinancialMetricsDTO result = dashboardService.getFinancialMetrics(startDate, endDate);

        assertThat(result.getAverageTransactionAmount()).isEqualByComparingTo(BigDecimal.valueOf(566.67));
        assertThat(result.getLargestTransaction()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(result.getSmallestTransaction()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
    }

    @Test
    void getTopSpendingCategories_WithLimit_ShouldEnforceLimit() {
        LocalDate currentMonthStart = java.time.YearMonth.now().atDay(1);

        TransactionCategory category1 = new TransactionCategory();
        category1.setId(1L);
        category1.setName("Food");

        TransactionCategory category2 = new TransactionCategory();
        category2.setId(2L);
        category2.setName("Transport");

        TransactionCategory category3 = new TransactionCategory();
        category3.setId(3L);
        category3.setName("Entertainment");

        Transaction expense1 = createTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(1000.00));
        expense1.setCategory(category1);
        expense1.setDate(currentMonthStart.plusDays(5).atStartOfDay()); // Within current month

        Transaction expense2 = createTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(500.00));
        expense2.setCategory(category2);
        expense2.setDate(currentMonthStart.plusDays(10).atStartOfDay()); // Within current month

        Transaction expense3 = createTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(200.00));
        expense3.setCategory(category3);
        expense3.setDate(currentMonthStart.plusDays(15).atStartOfDay()); // Within current month

        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(List.of(expense1, expense2, expense3));

        List<CategorySpendingDTO> result = dashboardService.getTopSpendingCategories(1L, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Food");
        assertThat(result.get(1).getCategoryName()).isEqualTo("Transport");
    }

    @Test
    void getTopSpendingCategories_WithEmptyExpenses_ShouldReturnEmptyList() {
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(new ArrayList<>());

        List<CategorySpendingDTO> result = dashboardService.getTopSpendingCategories(1L, 5);

        assertThat(result).isEmpty();
    }

    @Test
    void getMonthlyTrends_WithZeroMonths_ShouldReturnEmptyList() {
        // Given - When months is 0, the loop doesn't execute, so no repository calls are made

        // When
        List<MonthlyTrendDTO> result = dashboardService.getMonthlyTrends(1L, 0);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getMonthlyTrends_WithOneMonth_ShouldReturnSingleTrend() {
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.valueOf(5000.00));
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.valueOf(3000.00));
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(List.of(createTransaction(TransactionType.INCOME, BigDecimal.valueOf(1000.00))));

        List<MonthlyTrendDTO> result = dashboardService.getMonthlyTrends(1L, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIncome()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(result.get(0).getExpenses()).isEqualByComparingTo(BigDecimal.valueOf(3000.00));
        assertThat(result.get(0).getBalance()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
    }

    @Test
    void getFinancialMetrics_WithReconciledTransactions_ShouldCountCorrectly() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        Transaction reconciledTransaction = createTransaction(TransactionType.INCOME, BigDecimal.valueOf(1000.00));
        reconciledTransaction.setReconciled(true);
        reconciledTransaction.setDate(LocalDateTime.of(2024, 1, 15, 12, 0)); // Within date range

        Transaction unreconciledTransaction = createTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(500.00));
        unreconciledTransaction.setReconciled(false);
        unreconciledTransaction.setDate(LocalDateTime.of(2024, 1, 20, 12, 0)); // Within date range

        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000.00));
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.valueOf(500.00));
        when(transactionRepository.findByUserIdWithResponsibilities(1L))
                .thenReturn(List.of(reconciledTransaction, unreconciledTransaction));

        FinancialMetricsDTO result = dashboardService.getFinancialMetrics(startDate, endDate);

        assertThat(result.getTotalTransactions()).isEqualTo(2);
        assertThat(result.getIncomeTransactions()).isEqualTo(1);
        assertThat(result.getExpenseTransactions()).isEqualTo(1);
    }

    private Transaction createTransaction(TransactionType type, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(testUser);
        transaction.setCategory(testCategory);
        transaction.setReconciled(false);
        return transaction;
    }

    private FinancialGoal createFinancialGoal(boolean isActive) {
        FinancialGoal goal = new FinancialGoal();
        goal.setId(1L);
        goal.setName("Test Goal");
        goal.setTargetAmount(BigDecimal.valueOf(10000.00));
        goal.setCurrentAmount(BigDecimal.valueOf(5000.00));
        goal.setDeadline(LocalDate.now().plusMonths(6));
        goal.setIsActive(isActive);
        goal.setGoalType(GoalType.SAVINGS);
        return goal;
    }
}
