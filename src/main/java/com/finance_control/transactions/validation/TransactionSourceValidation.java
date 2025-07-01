package com.finance_control.transactions.validation;

import com.finance_control.shared.validation.BaseValidation;

/**
 * Utility class for transaction source-specific validations.
 * Provides reusable validation methods for transaction source operations.
 */
public final class TransactionSourceValidation {
    
    private static final String FIELD_NAME = "Source Name";
    private static final String FIELD_USER_ID = "User ID";
    
    private TransactionSourceValidation() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates source name for create operations.
     * 
     * @param name the name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateName(String name) {
        BaseValidation.validateRequiredString(name, FIELD_NAME);
        BaseValidation.validateStringMaxLength(name, FIELD_NAME, 100);
    }
    
    /**
     * Validates source name for update operations.
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
     * Validates source name uniqueness for a user.
     * 
     * @param name the name to validate
     * @param nameExistsFunction function to check if name already exists for the user
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNameUnique(String name, java.util.function.Function<String, Boolean> nameExistsFunction) {
        validateName(name);
        if (nameExistsFunction.apply(name)) {
            throw new IllegalArgumentException("Source with name '" + name + "' already exists for this user");
        }
    }
} 