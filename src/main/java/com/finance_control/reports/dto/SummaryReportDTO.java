package com.finance_control.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for summary report combining transactions and goals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryReportDTO {

    private String period;
    private TransactionReportDTO transactions;
    private GoalReportDTO goals;
    private BigDecimal netWorth;
    private BigDecimal savingsRate;
}
