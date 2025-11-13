package com.finance_control.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed projection for a single month.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastedMonthDTO {

    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth month;

    private BigDecimal projectedIncome;

    private BigDecimal projectedExpenses;

    private BigDecimal projectedNet;

    private String notes;
}
