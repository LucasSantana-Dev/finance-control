package com.finance_control.goals.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for financial goal completion requests.
 * Used for marking goals as completed with final data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalCompletionRequest {
    
    @NotNull(message = "Final amount is required")
    private BigDecimal finalAmount;
    
    @NotNull(message = "Completion date is required")
    private LocalDateTime completionDate;
    
    @NotNull(message = "Completed status is required")
    private Boolean completed;
    
    private String completionNotes;
    
    private String achievementNotes;
    
    private BigDecimal actualSavings;
    
    private BigDecimal actualInvestment;
} 