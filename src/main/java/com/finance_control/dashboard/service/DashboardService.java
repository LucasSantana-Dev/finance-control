package com.finance_control.dashboard.service;

import com.finance_control.dashboard.dto.*;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Get comprehensive dashboard summary for the current user.
     */
    public DashboardSummaryDTO getDashboardSummary() {
        Long userId = UserContext.getCurrentUserId();
        log.debug("Generating dashboard summary for user: {}", userId);

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
    }

    /**
     * Get detailed financial metrics for a specific period.
     */
    public FinancialMetricsDTO getFinancialMetrics(LocalDate startDate, LocalDate endDate) {
        Long userId = UserContext.getCurrentUserId();
        log.debug("Generating financial metrics for user: {} from {} to {}", userId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        BigDecimal totalIncome = getTotalIncome(userId, startDate, endDate);
        BigDecimal totalExpenses = getTotalExpenses(userId, startDate, endDate);
        BigDecimal monthlySavings = totalIncome.subtract(totalExpenses);
        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalExpenses);

        List<Transaction> transactions = transactionRepository.findByUserIdWithResponsibilities(userId);
        List<Transaction> periodTransactions = transactions.stream()
                .filter(t -> !t.getDate().isBefore(startDateTime) && !t.getDate().isAfter(endDateTime))
                .collect(Collectors.toList());

        BigDecimal averageTransactionAmount = calculateAverageTransactionAmount(periodTransactions);
        BigDecimal largestTransaction = getLargestTransaction(periodTransactions);
        BigDecimal smallestTransaction = getSmallestTransaction(periodTransactions);

        int incomeTransactions = (int) periodTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .count();
        int expenseTransactions = (int) periodTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .count();

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
    public List<CategorySpendingDTO> getTopSpendingCategories(Long userId, int limit) {
        log.debug("Getting top {} spending categories for user: {}", limit, userId);

        LocalDate startOfMonth = YearMonth.now().atDay(1);
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

        List<Transaction> expenseTransactions = transactionRepository.findByUserIdWithResponsibilities(userId)
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> !t.getDate().toLocalDate().isBefore(startOfMonth) && !t.getDate().toLocalDate().isAfter(endOfMonth))
                .collect(Collectors.toList());

        Map<String, BigDecimal> categoryTotals = expenseTransactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        BigDecimal totalExpenses = categoryTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategorySpendingDTO> categories = new ArrayList<>();
        String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40"};

        int colorIndex = 0;
        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            if (categories.size() >= limit) break;

            BigDecimal amount = entry.getValue();
            BigDecimal percentage;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.divide(totalExpenses, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            } else {
                percentage = BigDecimal.ZERO;
            }

            int transactionCount = (int) expenseTransactions.stream()
                    .filter(t -> t.getCategory().getName().equals(entry.getKey()))
                    .count();

            categories.add(CategorySpendingDTO.builder()
                    .categoryName(entry.getKey())
                    .amount(amount)
                    .percentage(percentage)
                    .transactionCount(transactionCount)
                    .color(colors[colorIndex % colors.length])
                    .build());

            colorIndex++;
        }

        return categories.stream()
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }

    /**
     * Get monthly trends for the last N months.
     */
    public List<MonthlyTrendDTO> getMonthlyTrends(Long userId, int months) {
        log.debug("Getting monthly trends for user: {} for last {} months", userId, months);

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
        return (int) transactionRepository.findByUserIdWithResponsibilities(userId).stream()
                .filter(t -> !Boolean.TRUE.equals(t.getReconciled()))
                .count();
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
        BigDecimal total = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getLargestTransaction(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getSmallestTransaction(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private int getTransactionCountForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        return (int) transactionRepository.findByUserIdWithResponsibilities(userId).stream()
                .filter(t -> !t.getDate().toLocalDate().isBefore(startDate) && !t.getDate().toLocalDate().isAfter(endDate))
                .count();
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
}
