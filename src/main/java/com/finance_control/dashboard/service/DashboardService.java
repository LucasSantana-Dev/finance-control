package com.finance_control.dashboard.service;

import com.finance_control.dashboard.dto.*;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.service.SupabaseRealtimeService;
import com.finance_control.shared.util.StreamUtils;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating dashboard data and financial metrics.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final FinancialGoalRepository financialGoalRepository;
    private final MetricsService metricsService;

    @Autowired(required = false)
    private SupabaseRealtimeService realtimeService;

    /**
     * Get comprehensive dashboard summary for the current user.
     */
    @Cacheable(value = "dashboard", key = "#root.methodName + '_' + T(com.finance_control.shared.context.UserContext).getCurrentUserId()")
    public DashboardSummaryDTO getDashboardSummary() {
        var sample = metricsService.startDashboardGenerationTimer();
        try {
            Long userId = UserContext.getCurrentUserId();
            log.debug("Generating dashboard summary (user present: {})", userId != null);

        LocalDate startOfMonth = YearMonth.now().atDay(1);
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

        BigDecimal totalIncome = getTotalIncome(userId, startOfMonth, endOfMonth);
        BigDecimal totalExpenses = getTotalExpenses(userId, startOfMonth, endOfMonth);
        BigDecimal monthlyBalance = totalIncome.subtract(totalExpenses);
        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalExpenses);

        List<FinancialGoal> activeGoals = financialGoalRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
        List<FinancialGoal> completedGoals = financialGoalRepository.findCompletedGoals(userId);

        BigDecimal totalGoalProgress = calculateTotalGoalProgress(activeGoals);

        int totalTransactions = getTotalTransactionCount(userId);
        int pendingReconciliations = getPendingReconciliationsCount(userId);

        return DashboardSummaryDTO.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netWorth(calculateNetWorth(userId))
                .monthlyBalance(monthlyBalance)
                .savingsRate(savingsRate)
                .activeGoals(activeGoals.size())
                .completedGoals(completedGoals.size())
                .totalGoalProgress(totalGoalProgress)
                .totalTransactions(totalTransactions)
                .pendingReconciliations(pendingReconciliations)
                .lastUpdated(LocalDate.now())
                .topSpendingCategories(getTopSpendingCategories(userId, 5))
                .monthlyTrends(getMonthlyTrends(userId, 12))
                .goalProgress(mapGoalProgress(activeGoals))
                .build();
        } finally {
            metricsService.recordDashboardGenerationTime(sample);
        }
    }

    /**
     * Get detailed financial metrics for a specific period.
     */
    @Cacheable(value = "dashboard",
                key = "#root.methodName + '_' + T(com.finance_control.shared.context.UserContext).getCurrentUserId() + '_' + #startDate + '_' + #endDate")
    public FinancialMetricsDTO getFinancialMetrics(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }

        Long userId = UserContext.getCurrentUserId();
        log.debug("Generating financial metrics (user present: {}, dates present: {}, {})", userId != null, startDate != null, endDate != null);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        BigDecimal totalIncome = getTotalIncome(userId, startDate, endDate);
        BigDecimal totalExpenses = getTotalExpenses(userId, startDate, endDate);
        BigDecimal monthlySavings = totalIncome.subtract(totalExpenses);
        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalExpenses);

        List<Transaction> transactions = transactionRepository.findByUserIdWithResponsibilities(userId);
        List<Transaction> periodTransactions = filterTransactionsByDateRange(transactions, startDateTime, endDateTime);

        BigDecimal averageTransactionAmount = calculateAverageTransactionAmount(periodTransactions);
        BigDecimal largestTransaction = getLargestTransaction(periodTransactions);
        BigDecimal smallestTransaction = getSmallestTransaction(periodTransactions);

        int incomeTransactions = countTransactionsByType(periodTransactions, TransactionType.INCOME);
        int expenseTransactions = countTransactionsByType(periodTransactions, TransactionType.EXPENSE);

        return FinancialMetricsDTO.builder()
                .totalAssets(calculateTotalAssets(userId))
                .totalLiabilities(calculateTotalLiabilities(userId))
                .netWorth(calculateNetWorth(userId))
                .monthlyIncome(totalIncome)
                .monthlyExpenses(totalExpenses)
                .monthlySavings(monthlySavings)
                .savingsRate(savingsRate)
                .averageTransactionAmount(averageTransactionAmount)
                .largestTransaction(largestTransaction)
                .smallestTransaction(smallestTransaction)
                .totalTransactions(periodTransactions.size())
                .incomeTransactions(incomeTransactions)
                .expenseTransactions(expenseTransactions)
                .periodStart(startDate)
                .periodEnd(endDate)
                .build();
    }

    /**
     * Get top spending categories for charts.
     */
    @Cacheable(value = "dashboard", key = "#root.methodName + '_' + #userId + '_' + #limit")
    public List<CategorySpendingDTO> getTopSpendingCategories(Long userId, int limit) {
        log.debug("Getting top {} spending categories (user present: {})", limit, userId != null);

        LocalDate startOfMonth = YearMonth.now().atDay(1);
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

        List<Transaction> expenseTransactions = getExpenseTransactionsInDateRange(userId, startOfMonth, endOfMonth);
        Map<String, BigDecimal> categoryTotals = calculateCategoryTotals(expenseTransactions);
        BigDecimal totalExpenses = calculateTotalFromCategoryTotals(categoryTotals);

        List<CategorySpendingDTO> categories = new ArrayList<>();
        String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40"};

        int colorIndex = 0;
        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            BigDecimal amount = entry.getValue();
            BigDecimal percentage;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.divide(totalExpenses, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            } else {
                percentage = BigDecimal.ZERO;
            }

            int transactionCount = countTransactionsByCategory(expenseTransactions, entry.getKey());

            categories.add(CategorySpendingDTO.builder()
                    .categoryName(entry.getKey())
                    .amount(amount)
                    .percentage(percentage)
                    .transactionCount(transactionCount)
                    .color(colors[colorIndex % colors.length])
                    .build());

            colorIndex++;
        }

        List<CategorySpendingDTO> sortedCategories = sortCategoriesByAmount(categories);
        return sortedCategories.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Get monthly trends for the last N months.
     */
    @Cacheable(value = "dashboard", key = "#root.methodName + '_' + #userId + '_' + #months")
    public List<MonthlyTrendDTO> getMonthlyTrends(Long userId, int months) {
        log.debug("Getting monthly trends (user present: {}) for last {} months", userId != null, months);

        List<MonthlyTrendDTO> trends = new ArrayList<>();
        LocalDate currentMonth = YearMonth.now().atDay(1);

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = currentMonth.minusMonths(i);
            LocalDate monthEnd = YearMonth.from(monthStart).atEndOfMonth();

            BigDecimal income = getTotalIncome(userId, monthStart, monthEnd);
            BigDecimal expenses = getTotalExpenses(userId, monthStart, monthEnd);
            BigDecimal balance = income.subtract(expenses);

            int transactionCount = getTransactionCountForPeriod(userId, monthStart, monthEnd);

            trends.add(MonthlyTrendDTO.builder()
                    .month(monthStart)
                    .income(income)
                    .expenses(expenses)
                    .balance(balance)
                    .transactionCount(transactionCount)
                    .build());
        }

        return trends;
    }

    private BigDecimal getTotalIncome(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumByUserAndTypeAndDateBetween(
                userId, TransactionType.INCOME, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }

    private BigDecimal getTotalExpenses(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumByUserAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }

    public BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal expenses) {
        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal savings = income.subtract(expenses);
        return savings.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateTotalGoalProgress(List<FinancialGoal> goals) {
        if (goals.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return goals.stream()
                .map(FinancialGoal::getProgressPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(goals.size()), 2, RoundingMode.HALF_UP);
    }

    private int getTotalTransactionCount(Long userId) {
        return transactionRepository.findByUserIdWithResponsibilities(userId).size();
    }

    private int getPendingReconciliationsCount(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdWithResponsibilities(userId);
        return countUnreconciledTransactions(transactions);
    }

    private BigDecimal calculateNetWorth(Long userId) {
        // Simplified calculation - in a real app, this would consider assets and liabilities
        LocalDate startOfYear = YearMonth.now().atDay(1).withDayOfYear(1);
        LocalDate endOfYear = YearMonth.now().atEndOfMonth().withDayOfYear(365);

        BigDecimal totalIncome = getTotalIncome(userId, startOfYear, endOfYear);
        BigDecimal totalExpenses = getTotalExpenses(userId, startOfYear, endOfYear);

        return totalIncome.subtract(totalExpenses);
    }

    private BigDecimal calculateTotalAssets(Long userId) {
        // Placeholder - would integrate with asset tracking
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateTotalLiabilities(Long userId) {
        // Placeholder - would integrate with liability tracking
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateAverageTransactionAmount(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = StreamUtils.sum(transactions, Transaction::getAmount);
        return total.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getLargestTransaction(List<Transaction> transactions) {
        return StreamUtils.max(transactions, Transaction::getAmount);
    }

    private BigDecimal getSmallestTransaction(List<Transaction> transactions) {
        return StreamUtils.min(transactions, Transaction::getAmount);
    }

    private int getTransactionCountForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findByUserIdWithResponsibilities(userId);
        return countTransactionsInDateRange(transactions, startDate, endDate);
    }

    private List<GoalProgressDTO> mapGoalProgress(List<FinancialGoal> goals) {
        return goals.stream()
                .map(goal -> GoalProgressDTO.builder()
                        .goalId(goal.getId())
                        .goalName(goal.getName())
                        .targetAmount(goal.getTargetAmount())
                        .currentAmount(goal.getCurrentAmount())
                        .progressPercentage(goal.getProgressPercentage())
                        .deadline(goal.getDeadline())
                        .isActive(goal.getIsActive())
                        .goalType(goal.getGoalType().name())
                        .build())
                .collect(Collectors.toList());
    }

    // Helper methods for stream operations

    /**
     * Filters transactions by date range.
     */
    private List<Transaction> filterTransactionsByDateRange(List<Transaction> transactions, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return StreamUtils.filterByDateRange(transactions, Transaction::getDate, startDateTime, endDateTime);
    }

    /**
     * Counts transactions by type.
     */
    private int countTransactionsByType(List<Transaction> transactions, TransactionType type) {
        return (int) StreamUtils.count(transactions, t -> t.getType() == type);
    }

    /**
     * Gets expense transactions within a date range.
     */
    private List<Transaction> getExpenseTransactionsInDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> allTransactions = transactionRepository.findByUserIdWithResponsibilities(userId);
        List<Transaction> expenseTransactions = StreamUtils.filter(allTransactions, t -> t.getType() == TransactionType.EXPENSE);
        return StreamUtils.filterByLocalDateRange(expenseTransactions, t -> t.getDate().toLocalDate(), startDate, endDate);
    }

    /**
     * Calculates category totals from transactions.
     */
    private Map<String, BigDecimal> calculateCategoryTotals(List<Transaction> transactions) {
        return StreamUtils.groupAndSum(transactions, t -> t.getCategory().getName(), Transaction::getAmount);
    }

    /**
     * Calculates total from category totals map.
     */
    private BigDecimal calculateTotalFromCategoryTotals(Map<String, BigDecimal> categoryTotals) {
        return categoryTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Counts transactions by category name.
     */
    private int countTransactionsByCategory(List<Transaction> transactions, String categoryName) {
        return (int) StreamUtils.count(transactions, t -> t.getCategory().getName().equals(categoryName));
    }

    /**
     * Sorts categories by amount in descending order.
     */
    private List<CategorySpendingDTO> sortCategoriesByAmount(List<CategorySpendingDTO> categories) {
        return categories.stream()
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }

    /**
     * Counts unreconciled transactions.
     */
    private int countUnreconciledTransactions(List<Transaction> transactions) {
        return (int) StreamUtils.count(transactions, t -> !Boolean.TRUE.equals(t.getReconciled()));
    }

    /**
     * Counts transactions within a date range.
     */
    private int countTransactionsInDateRange(List<Transaction> transactions, LocalDate startDate, LocalDate endDate) {
        return (int) StreamUtils.countByLocalDateRange(transactions, t -> t.getDate().toLocalDate(), startDate, endDate);
    }

    /**
     * Notify subscribers about dashboard updates for a user.
     * This method can be called when transaction or goal data changes.
     *
     * @param userId the user ID whose dashboard was updated
     */
    public void notifyDashboardUpdate(Long userId) {
        if (realtimeService != null) {
            try {
                // Generate fresh dashboard data for the user
                // Note: This is simplified - in a real implementation, you might want to
                // generate a lightweight dashboard update notification instead of full data
                Map<String, Object> dashboardData = Map.of(
                    "userId", userId,
                    "timestamp", System.currentTimeMillis(),
                    "type", "dashboard_update"
                );

                realtimeService.notifyDashboardUpdate(userId, dashboardData);
                log.debug("Sent realtime notification for dashboard update: user {}", userId);
            } catch (Exception e) {
                log.warn("Failed to send realtime notification for dashboard update: {}", e.getMessage());
            }
        }
    }
}
