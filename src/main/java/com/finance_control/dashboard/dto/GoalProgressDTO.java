package com.finance_control.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for goal progress data used in dashboard charts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgressDTO {

    private Long goalId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal progressPercentage;
    private LocalDate deadline;
    private Boolean isActive;
    private String goalType;
}
