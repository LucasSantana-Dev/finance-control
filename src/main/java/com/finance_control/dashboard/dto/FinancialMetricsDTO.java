package com.finance_control.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for detailed financial metrics used in dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialMetricsDTO {

    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netWorth;

    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal monthlySavings;
    private BigDecimal savingsRate;

    private BigDecimal averageTransactionAmount;
    private BigDecimal largestTransaction;
    private BigDecimal smallestTransaction;

    private Integer totalTransactions;
    private Integer incomeTransactions;
    private Integer expenseTransactions;

    private LocalDate periodStart;
    private LocalDate periodEnd;
}
