package com.finance_control.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for monthly trend data used in dashboard charts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDTO {

    private LocalDate month;
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal balance;
    private Integer transactionCount;
}
