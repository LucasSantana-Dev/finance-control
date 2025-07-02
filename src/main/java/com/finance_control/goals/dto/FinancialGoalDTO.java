package com.finance_control.goals.dto;

import com.finance_control.goals.validation.FinancialGoalValidation;
import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.shared.enums.GoalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating, updating, and representing financial goals.
 * This class serves all operations, with ID being optional for creation.
 * 
 * <p>
 * For creation: id should be null
 * For updates: id should be populated with the existing goal ID
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FinancialGoalDTO extends BaseDTO<Long> {

    @NotBlank(message = "Goal name is required")
    private String name;

    private String description;

    @NotNull(message = "Goal type is required")
    private GoalType goalType;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    private BigDecimal targetAmount;

    private BigDecimal currentAmount;
    private LocalDateTime targetDate;
    private Boolean isActive;

    private LocalDateTime deadline;
    private Boolean autoCalculate;

    private Long accountId;

    /**
     * Validates the DTO for create operations.
     * Ensures all required fields are present and valid.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateCreate() {
        FinancialGoalValidation.validateName(name);
        FinancialGoalValidation.validateDescription(description);
        FinancialGoalValidation.validateGoalType(goalType);
        FinancialGoalValidation.validateTargetAmount(targetAmount);
        FinancialGoalValidation.validateCurrentAmount(currentAmount);
        FinancialGoalValidation.validateTargetDate(targetDate);
        FinancialGoalValidation.validateDeadline(deadline);
        FinancialGoalValidation.validateAccountId(accountId);
    }

    /**
     * Validates the DTO for update operations.
     * Validates only the fields that are present (not null).
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateUpdate() {
        FinancialGoalValidation.validateNameForUpdate(name);
        FinancialGoalValidation.validateDescription(description);
        FinancialGoalValidation.validateGoalTypeForUpdate(goalType);
        FinancialGoalValidation.validateTargetAmountForUpdate(targetAmount);
        FinancialGoalValidation.validateCurrentAmount(currentAmount);
        FinancialGoalValidation.validateTargetDate(targetDate);
        FinancialGoalValidation.validateDeadline(deadline);
        FinancialGoalValidation.validateAccountId(accountId);
    }

    /**
     * Validates the DTO for response operations.
     * Ensures the DTO is properly populated for API responses.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateResponse() {
        super.validateResponse(); // Validate common fields (ID)

        FinancialGoalValidation.validateName(name);
        FinancialGoalValidation.validateDescription(description);
        FinancialGoalValidation.validateGoalType(goalType);
        FinancialGoalValidation.validateTargetAmount(targetAmount);
        FinancialGoalValidation.validateCurrentAmount(currentAmount);
        FinancialGoalValidation.validateTargetDate(targetDate);
        FinancialGoalValidation.validateDeadline(deadline);
        FinancialGoalValidation.validateAccountId(accountId);

        if (isActive == null) {
            isActive = true; // Default to active if not set
        }
    }
}