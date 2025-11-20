package com.finance_control.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for transaction report data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportDTO {

    private String period;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private Integer transactionCount;
    private List<CategorySummaryDTO> topCategories;
}
