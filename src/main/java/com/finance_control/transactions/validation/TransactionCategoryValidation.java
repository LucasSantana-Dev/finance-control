package com.finance_control.transactions.validation;

import com.finance_control.shared.validation.BaseValidation;
import java.util.function.Predicate;

/**
 * Utility class for transaction category-specific validations.
 * Provides reusable validation methods for transaction category operations.
 */
public final class TransactionCategoryValidation {
    
    private static final String FIELD_NAME = "Category Name";
    
    private TransactionCategoryValidation() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates category name for create operations.
     * 
     * @param name the name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateName(String name) {
        BaseValidation.validateRequiredString(name, FIELD_NAME);
        BaseValidation.validateStringMaxLength(name, FIELD_NAME, 100);
    }
    
    /**
     * Validates category name for update operations.
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
     * Validates category name uniqueness.
     * 
     * @param name the name to validate
     * @param nameExistsFunction function to check if name already exists
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNameUnique(String name, Predicate<String> nameExistsFunction) {
        validateName(name);
        if (nameExistsFunction.test(name)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists");
        }
    }
} 