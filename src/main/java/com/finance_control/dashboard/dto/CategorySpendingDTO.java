package com.finance_control.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for category spending data used in dashboard charts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySpendingDTO {

    private String categoryName;
    private String subcategoryName;
    private BigDecimal amount;
    private BigDecimal percentage;
    private Integer transactionCount;
    private String color; // For chart visualization
}
