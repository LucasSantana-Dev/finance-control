package com.finance_control.transactions.validation;

import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.validation.BaseValidation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Utility class for transaction-specific validations.
 * Provides reusable validation methods for transaction operations.
 */
public final class TransactionValidation {
    
    private static final String FIELD_DESCRIPTION = "Description";
    private static final String FIELD_AMOUNT = "Amount";
    private static final String FIELD_INSTALLMENTS = "Installments";
    private static final String FIELD_DATE = "Date";
    private static final String FIELD_CATEGORY_ID = "Category ID";
    private static final String FIELD_SUBCATEGORY_ID = "Subcategory ID";
    private static final String FIELD_SOURCE_ENTITY_ID = "Source Entity ID";
    private static final String FIELD_USER_ID = "User ID";
    private static final String FIELD_RESPONSIBILITIES = "Responsibilities";
    private static final String FIELD_TYPE = "Transaction Type";
    private static final String FIELD_SUBTYPE = "Transaction Subtype";
    private static final String FIELD_SOURCE = "Transaction Source";
    
    private TransactionValidation() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates transaction description for create operations.
     * 
     * @param description the description to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateDescription(String description) {
        BaseValidation.validateRequiredString(description, FIELD_DESCRIPTION);
        BaseValidation.validateStringMaxLength(description, FIELD_DESCRIPTION, 500);
    }
    
    /**
     * Validates transaction description for update operations.
     * 
     * @param description the description to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateDescriptionForUpdate(String description) {
        BaseValidation.validateOptionalString(description, FIELD_DESCRIPTION);
        if (description != null) {
            BaseValidation.validateStringMaxLength(description, FIELD_DESCRIPTION, 500);
        }
    }
    
    /**
     * Validates transaction amount for create operations.
     * 
     * @param amount the amount to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateAmount(BigDecimal amount) {
        BaseValidation.validateRequiredObject(amount, FIELD_AMOUNT);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(FIELD_AMOUNT + " must be positive");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException(FIELD_AMOUNT + " cannot have more than 2 decimal places");
        }
    }
    
    /**
     * Validates transaction amount for update operations.
     * 
     * @param amount the amount to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateAmountForUpdate(BigDecimal amount) {
        BaseValidation.validateOptionalObject(amount, FIELD_AMOUNT);
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(FIELD_AMOUNT + " must be positive");
            }
            if (amount.scale() > 2) {
                throw new IllegalArgumentException(FIELD_AMOUNT + " cannot have more than 2 decimal places");
            }
        }
    }
    
    /**
     * Validates transaction installments.
     * 
     * @param installments the installments to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateInstallments(Integer installments) {
        BaseValidation.validateOptionalObject(installments, FIELD_INSTALLMENTS);
        if (installments != null && installments <= 0) {
            throw new IllegalArgumentException(FIELD_INSTALLMENTS + " must be positive");
        }
    }
    
    /**
     * Validates transaction date.
     * 
     * @param date the date to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateDate(LocalDateTime date) {
        BaseValidation.validateOptionalObject(date, FIELD_DATE);
        if (date != null && date.isAfter(LocalDateTime.now().plusYears(10))) {
            throw new IllegalArgumentException(FIELD_DATE + " cannot be more than 10 years in the future");
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
     * Validates subcategory ID.
     * 
     * @param subcategoryId the subcategory ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateSubcategoryId(Long subcategoryId) {
        BaseValidation.validateOptionalId(subcategoryId, FIELD_SUBCATEGORY_ID);
    }
    
    /**
     * Validates source entity ID.
     * 
     * @param sourceEntityId the source entity ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateSourceEntityId(Long sourceEntityId) {
        BaseValidation.validateOptionalId(sourceEntityId, FIELD_SOURCE_ENTITY_ID);
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
     * Validates responsibilities list for create operations.
     * 
     * @param responsibilities the responsibilities to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateResponsibilities(List<?> responsibilities) {
        BaseValidation.validateRequiredCollection(responsibilities, FIELD_RESPONSIBILITIES);
        if (responsibilities.isEmpty()) {
            throw new IllegalArgumentException(FIELD_RESPONSIBILITIES + " cannot be empty");
        }
    }
    
    /**
     * Validates responsibilities list for update operations.
     * 
     * @param responsibilities the responsibilities to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateResponsibilitiesForUpdate(List<?> responsibilities) {
        BaseValidation.validateOptionalCollection(responsibilities, FIELD_RESPONSIBILITIES);
        if (responsibilities != null && responsibilities.isEmpty()) {
            throw new IllegalArgumentException(FIELD_RESPONSIBILITIES + " cannot be empty");
        }
    }
    
    /**
     * Validates transaction type for create operations.
     * 
     * @param type the transaction type to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateType(TransactionType type) {
        BaseValidation.validateRequiredObject(type, FIELD_TYPE);
    }
    
    /**
     * Validates transaction type for update operations.
     * 
     * @param type the transaction type to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateTypeForUpdate(TransactionType type) {
        BaseValidation.validateOptionalObject(type, FIELD_TYPE);
    }
    
    /**
     * Validates transaction subtype for create operations.
     * 
     * @param subtype the transaction subtype to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateSubtype(TransactionSubtype subtype) {
        BaseValidation.validateRequiredObject(subtype, FIELD_SUBTYPE);
    }
    
    /**
     * Validates transaction subtype for update operations.
     * 
     * @param subtype the transaction subtype to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateSubtypeForUpdate(TransactionSubtype subtype) {
        BaseValidation.validateOptionalObject(subtype, FIELD_SUBTYPE);
    }
    
    /**
     * Validates transaction source for create operations.
     * 
     * @param source the transaction source to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateSource(TransactionSource source) {
        BaseValidation.validateRequiredObject(source, FIELD_SOURCE);
    }
    
    /**
     * Validates transaction source for update operations.
     * 
     * @param source the transaction source to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateSourceForUpdate(TransactionSource source) {
        BaseValidation.validateOptionalObject(source, FIELD_SOURCE);
    }
} 