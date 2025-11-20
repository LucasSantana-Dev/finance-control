package com.finance_control.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for category summary in reports.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDTO {

    private String name;
    private BigDecimal amount;
    private Integer count;
    private BigDecimal percentage;
}
