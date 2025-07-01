package com.finance_control.transactions.validation;

import com.finance_control.shared.validation.BaseValidation;

/**
 * Utility class for transaction subcategory-specific validations.
 * Provides reusable validation methods for transaction subcategory operations.
 */
public final class TransactionSubcategoryValidation {
    
    private static final String FIELD_NAME = "Subcategory Name";
    private static final String FIELD_CATEGORY_ID = "Category ID";
    
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
        BaseValidation.validateRequiredString(name, FIELD_NAME);
        BaseValidation.validateStringMaxLength(name, FIELD_NAME, 100);
    }
    
    /**
     * Validates subcategory name for update operations.
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
     * Validates category ID for create operations.
     * 
     * @param categoryId the category ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateCategoryId(Long categoryId) {
        BaseValidation.validateRequiredId(categoryId, FIELD_CATEGORY_ID);
    }
    
    /**
     * Validates category ID for update operations.
     * 
     * @param categoryId the category ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateCategoryIdForUpdate(Long categoryId) {
        BaseValidation.validateOptionalId(categoryId, FIELD_CATEGORY_ID);
    }
    
    /**
     * Validates subcategory name uniqueness within a category.
     * 
     * @param name the name to validate
     * @param nameExistsFunction function to check if name already exists in the category
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNameUnique(String name, java.util.function.Function<String, Boolean> nameExistsFunction) {
        validateName(name);
        if (nameExistsFunction.apply(name)) {
            throw new IllegalArgumentException("Subcategory with name '" + name + "' already exists in this category");
        }
    }
} 