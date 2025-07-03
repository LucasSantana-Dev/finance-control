package com.finance_control.transactions.validation;

import java.util.function.Predicate;

import com.finance_control.shared.validation.BaseValidation;

/**
 * Utility class for transaction subcategory-specific validations.
 * Provides reusable validation methods for transaction subcategory operations.
 */
public final class TransactionSubcategoryValidation {
    
    private static final String NAME_FIELD = "Subcategory Name";
    private static final String CATEGORY_ID_FIELD = "Category ID";
    
    private TransactionSubcategoryValidation() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates subcategory name for create operations.
     * 
     * @param name the name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateName(String name) {
        BaseValidation.validateRequiredString(name, NAME_FIELD);
        BaseValidation.validateStringMaxLength(name, NAME_FIELD, 100);
    }
    
    /**
     * Validates subcategory name for update operations.
     * 
     * @param name the name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNameForUpdate(String name) {
        BaseValidation.validateOptionalString(name, NAME_FIELD);
        if (name != null) {
            BaseValidation.validateStringMaxLength(name, NAME_FIELD, 100);
        }
    }
    
    /**
     * Validates category ID for create operations.
     * 
     * @param categoryId the category ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateCategoryId(Long categoryId) {
        BaseValidation.validateRequiredId(categoryId, CATEGORY_ID_FIELD);
    }
    
    /**
     * Validates category ID for update operations.
     * 
     * @param categoryId the category ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateCategoryIdForUpdate(Long categoryId) {
        BaseValidation.validateOptionalId(categoryId, CATEGORY_ID_FIELD);
    }
    
    /**
     * Validates subcategory name uniqueness within a category.
     * 
     * @param name the name to validate
     * @param nameExistsFunction function to check if name already exists in the category
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNameUnique(String name, Predicate<String> nameExistsFunction) {
        validateName(name);
        if (nameExistsFunction.test(name)) {
            throw new IllegalArgumentException("Subcategory with name '" + name + "' already exists in this category");
        }
    }
} 