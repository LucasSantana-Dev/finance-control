package com.finance_control.shared.validation;

import com.finance_control.shared.util.ValidationUtils;

/**
 * Base validation utilities for common validation operations.
 * Provides reusable validation methods that can be used across all domains.
 */
public final class BaseValidation {
    
    private BaseValidation() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Validates that an ID is present and valid.
     * 
     * @param id the ID to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateId(Long id, String fieldName) {
        if (id == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        ValidationUtils.validateId(id);
    }
    
    /**
     * Validates that an ID is present and valid for required fields.
     * 
     * @param id the ID to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateRequiredId(Long id, String fieldName) {
        validateId(id, fieldName);
    }
    
    /**
     * Validates that an ID is valid for optional fields (can be null).
     * 
     * @param id the ID to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalId(Long id, String fieldName) {
        if (id != null) {
            ValidationUtils.validateId(id);
        }
    }
    
    /**
     * Validates that a string field is present and not empty.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateRequiredString(String value, String fieldName) {
        ValidationUtils.validateString(value, fieldName);
    }
    
    /**
     * Validates that a string field is valid when present (can be null).
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalString(String value, String fieldName) {
        if (value != null) {
            ValidationUtils.validateString(value, fieldName);
        }
    }
    
    /**
     * Validates that a string field has a maximum length.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @param maxLength the maximum allowed length
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateStringMaxLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }
    
    /**
     * Validates that a string field has a minimum length.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @param minLength the minimum required length
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateStringMinLength(String value, String fieldName, int minLength) {
        if (value != null && value.length() < minLength) {
            throw new IllegalArgumentException(fieldName + " must be at least " + minLength + " characters");
        }
    }
    
    /**
     * Validates that a string field matches a specific pattern.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @param pattern the regex pattern to match
     * @param errorMessage the error message if validation fails
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateStringPattern(String value, String fieldName, String pattern, String errorMessage) {
        if (value != null && !value.matches(pattern)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
    
    /**
     * Validates that a boolean field is not null.
     * 
     * @param value the boolean to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateRequiredBoolean(Boolean value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }
    
    /**
     * Validates that a boolean field is valid when present (can be null).
     * 
     * @param value the boolean to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalBoolean(Boolean value, String fieldName) {
        // Boolean can be null, so no validation needed
    }
    
    /**
     * Validates that an object is not null.
     * 
     * @param value the object to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateRequiredObject(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }
    
    /**
     * Validates that an object is valid when present (can be null).
     * 
     * @param value the object to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalObject(Object value, String fieldName) {
        // Object can be null, so no validation needed
    }
    
    /**
     * Validates that a collection is not null and not empty.
     * 
     * @param collection the collection to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateRequiredCollection(java.util.Collection<?> collection, String fieldName) {
        ValidationUtils.validateCollection(collection, fieldName);
    }
    
    /**
     * Validates that a collection is valid when present (can be null).
     * 
     * @param collection the collection to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalCollection(java.util.Collection<?> collection, String fieldName) {
        if (collection != null) {
            ValidationUtils.validateCollection(collection, fieldName);
        }
    }
} 