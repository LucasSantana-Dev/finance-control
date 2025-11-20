package com.finance_control.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for goal report data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalReportDTO {

    private String period;
    private Integer totalGoals;
    private Integer activeGoals;
    private Integer completedGoals;
    private BigDecimal totalTarget;
    private BigDecimal totalCurrent;
    private BigDecimal completionRate;
    private BigDecimal averageProgress;
}
