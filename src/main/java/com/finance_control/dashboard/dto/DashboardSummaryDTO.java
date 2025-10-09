package com.finance_control.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for dashboard summary data containing key financial metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netWorth;
    private BigDecimal monthlyBalance;
    private BigDecimal savingsRate;

    private Integer activeGoals;
    private Integer completedGoals;
    private BigDecimal totalGoalProgress;

    private Integer totalTransactions;
    private Integer pendingReconciliations;

    private LocalDate lastUpdated;

    private List<CategorySpendingDTO> topSpendingCategories;
    private List<MonthlyTrendDTO> monthlyTrends;
    private List<GoalProgressDTO> goalProgress;
}
