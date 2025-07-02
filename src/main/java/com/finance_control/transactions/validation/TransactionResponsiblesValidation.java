package com.finance_control.transactions.validation;

import java.util.function.Predicate;

import com.finance_control.shared.validation.BaseValidation;

/**
 * Utility class for transaction responsibles-specific validations.
 * Provides reusable validation methods for transaction responsibles operations.
 */
public final class TransactionResponsiblesValidation {
    
    private static final String FIELD_NAME = "Responsible Name";
    private static final String FIELD_USER_ID = "User ID";
    
    private TransactionResponsiblesValidation() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates responsible name for create operations.
     * 
     * @param name the name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateName(String name) {
        BaseValidation.validateRequiredString(name, FIELD_NAME);
        BaseValidation.validateStringMaxLength(name, FIELD_NAME, 100);
    }
    
    /**
     * Validates responsible name for update operations.
     * 
     * @param name the name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNameForUpdate(String name) {
        BaseValidation.validateOptionalString(name, FIELD_NAME);
        if (name != null) {
            BaseValidation.validateStringMaxLength(name, FIELD_NAME, 100);
        }
    }
    
    /**
     * Validates user ID for create operations.
     * 
     * @param userId the user ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateUserId(Long userId) {
        BaseValidation.validateRequiredId(userId, FIELD_USER_ID);
    }
    
    /**
     * Validates user ID for update operations.
     * 
     * @param userId the user ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateUserIdForUpdate(Long userId) {
        BaseValidation.validateOptionalId(userId, FIELD_USER_ID);
    }
    
    /**
     * Validates responsible name uniqueness for a user.
     * 
     * @param name the name to validate
     * @param nameExistsFunction function to check if name already exists for the user
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNameUnique(String name, Predicate<String> nameExistsFunction) {
        validateName(name);
        if (nameExistsFunction.test(name)) {
            throw new IllegalArgumentException("Responsible with name '" + name + "' already exists for this user");
        }
    }
} 