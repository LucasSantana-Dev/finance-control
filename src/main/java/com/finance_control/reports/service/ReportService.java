package com.finance_control.reports.service;

import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.reports.dto.CategorySummaryDTO;
import com.finance_control.reports.dto.GoalReportDTO;
import com.finance_control.reports.dto.SummaryReportDTO;
import com.finance_control.reports.dto.TransactionReportDTO;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating financial reports.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final FinancialGoalRepository financialGoalRepository;

    /**
     * Generate transaction report with filters.
     */
    public TransactionReportDTO generateTransactionReport(
            LocalDate dateFrom,
            LocalDate dateTo,
            String type,
            String category) {

        Long userId = UserContext.getCurrentUserId();
        log.debug("Generating transaction report for user: {}", userId);

        // Get all transactions for the user
        List<Transaction> allTransactions = transactionRepository.findByUserIdWithResponsibilities(userId);

        // Filter transactions
        List<Transaction> filteredTransactions = filterTransactions(allTransactions, dateFrom, dateTo, type, category);

        // Calculate totals
        BigDecimal totalIncome = calculateTotalByType(filteredTransactions, TransactionType.INCOME);
        BigDecimal totalExpense = calculateTotalByType(filteredTransactions, TransactionType.EXPENSE);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        // Get top categories
        List<CategorySummaryDTO> topCategories = calculateTopCategories(filteredTransactions);

        // Format period string
        String period = formatPeriod(dateFrom, dateTo);

        return TransactionReportDTO.builder()
                .period(period)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .transactionCount(filteredTransactions.size())
                .topCategories(topCategories)
                .build();
    }

    /**
     * Generate goal report with filters.
     */
    public GoalReportDTO generateGoalReport(String status) {
        Long userId = UserContext.getCurrentUserId();
        log.debug("Generating goal report for user: {}", userId);

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        List<FinancialGoal> allGoals = financialGoalRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .getContent();

        // Filter by status if provided
        List<FinancialGoal> filteredGoals = filterGoalsByStatus(allGoals, status);

        // Calculate metrics
        int totalGoals = filteredGoals.size();
        int activeGoals = (int) filteredGoals.stream()
                .filter(FinancialGoal::getIsActive)
                .count();
        int completedGoals = totalGoals - activeGoals;

        BigDecimal totalTarget = filteredGoals.stream()
                .map(FinancialGoal::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCurrent = filteredGoals.stream()
                .map(FinancialGoal::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal completionRate = totalTarget.compareTo(BigDecimal.ZERO) > 0
                ? totalCurrent.divide(totalTarget, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal averageProgress = filteredGoals.isEmpty()
                ? BigDecimal.ZERO
                : filteredGoals.stream()
                        .map(FinancialGoal::getProgressPercentage)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(filteredGoals.size()), 2, RoundingMode.HALF_UP);

        return GoalReportDTO.builder()
                .period("All Time")
                .totalGoals(totalGoals)
                .activeGoals(activeGoals)
                .completedGoals(completedGoals)
                .totalTarget(totalTarget)
                .totalCurrent(totalCurrent)
                .completionRate(completionRate)
                .averageProgress(averageProgress)
                .build();
    }

    /**
     * Generate summary report combining transactions and goals.
     */
    public SummaryReportDTO generateSummaryReport(LocalDate dateFrom, LocalDate dateTo) {
        Long userId = UserContext.getCurrentUserId();
        log.debug("Generating summary report for user: {}", userId);

        // Generate transaction report
        TransactionReportDTO transactionReport = generateTransactionReport(dateFrom, dateTo, null, null);

        // Generate goal report
        GoalReportDTO goalReport = generateGoalReport(null);

        // Calculate net worth and savings rate
        BigDecimal netWorth = calculateNetWorth(userId, dateFrom, dateTo);
        BigDecimal savingsRate = calculateSavingsRate(transactionReport.getTotalIncome(), transactionReport.getTotalExpense());

        String period = formatPeriod(dateFrom, dateTo);

        return SummaryReportDTO.builder()
                .period(period)
                .transactions(transactionReport)
                .goals(goalReport)
                .netWorth(netWorth)
                .savingsRate(savingsRate)
                .build();
    }

    private List<Transaction> filterTransactions(
            List<Transaction> transactions,
            LocalDate dateFrom,
            LocalDate dateTo,
            String type,
            String category) {

        return transactions.stream()
                .filter(t -> {
                    // Date filter
                    if (dateFrom != null && t.getDate().toLocalDate().isBefore(dateFrom)) {
                        return false;
                    }
                    if (dateTo != null && t.getDate().toLocalDate().isAfter(dateTo)) {
                        return false;
                    }

                    // Type filter
                    if (type != null && !type.isEmpty()) {
                        TransactionType transactionType = type.equalsIgnoreCase("income")
                                ? TransactionType.INCOME
                                : TransactionType.EXPENSE;
                        if (t.getType() != transactionType) {
                            return false;
                        }
                    }

                    // Category filter
                    if (category != null && !category.isEmpty()) {
                        if (t.getCategory() == null || !t.getCategory().getName().equalsIgnoreCase(category)) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<FinancialGoal> filterGoalsByStatus(List<FinancialGoal> goals, String status) {
        if (status == null || status.isEmpty()) {
            return goals;
        }

        return goals.stream()
                .filter(g -> {
                    switch (status.toLowerCase()) {
                        case "active":
                            return g.getIsActive();
                        case "completed":
                            return !g.getIsActive() && g.getCurrentAmount().compareTo(g.getTargetAmount()) >= 0;
                        case "paused":
                            return !g.getIsActive() && g.getCurrentAmount().compareTo(g.getTargetAmount()) < 0;
                        case "cancelled":
                            return false; // Assuming cancelled goals are marked differently
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CategorySummaryDTO> calculateTopCategories(List<Transaction> transactions) {
        // Filter expense transactions only
        List<Transaction> expenseTransactions = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList());

        if (expenseTransactions.isEmpty()) {
            return new ArrayList<>();
        }

        // Group by category and sum amounts
        Map<String, BigDecimal> categoryTotals = expenseTransactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        // Calculate total expenses
        BigDecimal totalExpenses = categoryTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convert to DTOs
        List<CategorySummaryDTO> categories = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            String categoryName = entry.getKey();
            BigDecimal amount = entry.getValue();
            int count = (int) expenseTransactions.stream()
                    .filter(t -> t.getCategory() != null && t.getCategory().getName().equals(categoryName))
                    .count();

            BigDecimal percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            categories.add(CategorySummaryDTO.builder()
                    .name(categoryName)
                    .amount(amount)
                    .count(count)
                    .percentage(percentage)
                    .build());
        }

        // Sort by amount descending and limit to top 10
        return categories.stream()
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private String formatPeriod(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null && dateTo == null) {
            return "All Time";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String from = dateFrom != null ? dateFrom.format(formatter) : "Beginning";
        String to = dateTo != null ? dateTo.format(formatter) : "Now";
        return from + " to " + to;
    }

    private BigDecimal calculateNetWorth(Long userId, LocalDate dateFrom, LocalDate dateTo) {
        // Simplified calculation - in a real app, this would consider assets and liabilities
        List<Transaction> allTransactions = transactionRepository.findByUserIdWithResponsibilities(userId);
        List<Transaction> filteredTransactions = filterTransactions(allTransactions, dateFrom, dateTo, null, null);

        BigDecimal totalIncome = calculateTotalByType(filteredTransactions, TransactionType.INCOME);
        BigDecimal totalExpense = calculateTotalByType(filteredTransactions, TransactionType.EXPENSE);

        return totalIncome.subtract(totalExpense);
    }

    private BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal expenses) {
        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal savings = income.subtract(expenses);
        return savings.divide(income, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
