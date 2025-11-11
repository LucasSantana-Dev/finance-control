package com.finance_control.goals.validation;

import com.finance_control.shared.enums.GoalType;
import com.finance_control.shared.validation.BaseValidation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Utility class for financial goal-specific validations.
 * Provides reusable validation methods for financial goal operations.
 */
public final class FinancialGoalValidation {

    private static final String FIELD_NAME = "Goal Name";
    private static final String FIELD_DESCRIPTION = "Description";
    private static final String FIELD_GOAL_TYPE = "Goal Type";
    private static final String TARGET_AMOUNT_FIELD = "Target Amount";
    private static final String CURRENT_AMOUNT_FIELD = "Current Amount";
    private static final String FIELD_DEADLINE = "Deadline";
    private static final String FIELD_ACCOUNT_ID = "Account ID";

    private FinancialGoalValidation() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates goal name for create operations.
     *
     * @param name the name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateName(String name) {
        BaseValidation.validateRequiredString(name, FIELD_NAME);
        BaseValidation.validateStringMaxLength(name, FIELD_NAME, 100);
    }

    /**
     * Validates goal name for update operations.
     *
     * @param name the name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNameForUpdate(String name) {
        BaseValidation.validateOptionalStringWithMaxLength(name, FIELD_NAME, 100);
    }

    /**
     * Validates goal description.
     *
     * @param description the description to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateDescription(String description) {
        BaseValidation.validateOptionalStringWithMaxLength(description, FIELD_DESCRIPTION, 500);
    }

    /**
     * Validates goal type for create operations.
     *
     * @param goalType the goal type to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateGoalType(GoalType goalType) {
        BaseValidation.validateRequiredObject(goalType, FIELD_GOAL_TYPE);
    }

    /**
     * Validates goal type for update operations.
     *
     * @param goalType the goal type to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateGoalTypeForUpdate(GoalType goalType) {
        BaseValidation.validateOptionalObject(goalType, FIELD_GOAL_TYPE);
    }

    /**
     * Validates target amount for create operations.
     *
     * @param targetAmount the target amount to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateTargetAmount(BigDecimal targetAmount) {
        BaseValidation.validateRequiredObject(targetAmount, TARGET_AMOUNT_FIELD);
        if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(TARGET_AMOUNT_FIELD + " must be positive");
        }
        if (targetAmount.scale() > 2) {
            throw new IllegalArgumentException(TARGET_AMOUNT_FIELD + " cannot have more than 2 decimal places");
        }
    }

    /**
     * Validates target amount for update operations.
     *
     * @param targetAmount the target amount to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateTargetAmountForUpdate(BigDecimal targetAmount) {
        BaseValidation.validateOptionalBigDecimalWithConstraints(targetAmount, TARGET_AMOUNT_FIELD, true, 2);
    }

    /**
     * Validates current amount.
     *
     * @param currentAmount the current amount to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateCurrentAmount(BigDecimal currentAmount) {
        BaseValidation.validateOptionalBigDecimalNonNegative(currentAmount, CURRENT_AMOUNT_FIELD, 2);
    }

    /**
     * Validates deadline.
     *
     * @param deadline the deadline to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateDeadline(LocalDateTime deadline) {
        BaseValidation.validateOptionalDateTime(deadline, FIELD_DEADLINE, false, 0);
    }

    /**
     * Validates account ID.
     *
     * @param accountId the account ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateAccountId(Long accountId) {
        BaseValidation.validateOptionalId(accountId, FIELD_ACCOUNT_ID);
    }

    /**
     * Validates goal name uniqueness for a user.
     *
     * @param name the name to validate
     * @param nameExistsPred predicate to check if name already exists for the user
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNameUnique(String name, java.util.function.Predicate<String> nameExistsPred) {
        validateName(name);
        if (nameExistsPred.test(name)) {
            throw new IllegalArgumentException("Goal with name '" + name + "' already exists for this user");
        }
    }
}
