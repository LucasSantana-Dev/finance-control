package com.finance_control.shared.validation;

import com.finance_control.shared.util.ValidationUtils;

import java.math.BigDecimal;

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
     * Boolean values are always valid regardless of their value, so this method intentionally does nothing.
     *
     * @param value the boolean to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails (never in this case)
     */
    public static void validateOptionalBoolean(Boolean value, String fieldName) {
        // Boolean values are inherently valid - no specific validation logic needed
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
     * For general objects, null is considered valid (optional), so this method intentionally does nothing.
     * Specific object types should use their own validation methods.
     *
     * @param value the object to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails (never in this case)
     */
    public static void validateOptionalObject(Object value, String fieldName) {
        // Generic objects have no specific validation rules - null is acceptable for optional fields
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

    /**
     * Validates an optional string field with maximum length constraint.
     *
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @param maxLength the maximum allowed length
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalStringWithMaxLength(String value, String fieldName, int maxLength) {
        validateOptionalString(value, fieldName);
        if (value != null) {
            validateStringMaxLength(value, fieldName, maxLength);
        }
    }

    /**
     * Validates an optional BigDecimal field with business constraints.
     *
     * @param value the BigDecimal to validate
     * @param fieldName the name of the field for error messages
     * @param mustBePositive whether the value must be positive
     * @param maxScale the maximum allowed decimal places
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalBigDecimalWithConstraints(BigDecimal value, String fieldName, boolean mustBePositive, int maxScale) {
        validateOptionalObject(value, fieldName);
        if (value != null) {
            if (mustBePositive && value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(fieldName + " must be positive");
            }
            if (value.scale() > maxScale) {
                throw new IllegalArgumentException(fieldName + " cannot have more than " + maxScale + " decimal places");
            }
        }
    }

    /**
     * Validates an optional BigDecimal field that must be non-negative.
     *
     * @param value the BigDecimal to validate
     * @param fieldName the name of the field for error messages
     * @param maxScale the maximum allowed decimal places
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalBigDecimalNonNegative(BigDecimal value, String fieldName, int maxScale) {
        validateOptionalObject(value, fieldName);
        if (value != null) {
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(fieldName + " cannot be negative");
            }
            if (value.scale() > maxScale) {
                throw new IllegalArgumentException(fieldName + " cannot have more than " + maxScale + " decimal places");
            }
        }
    }

    /**
     * Validates an optional LocalDateTime field with future constraint.
     *
     * @param value the LocalDateTime to validate
     * @param fieldName the name of the field for error messages
     * @param allowPast whether past dates are allowed
     * @param maxYearsInFuture maximum years in the future (0 for no limit)
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalDateTime(java.time.LocalDateTime value, String fieldName, boolean allowPast, int maxYearsInFuture) {
        validateOptionalObject(value, fieldName);
        if (value != null) {
            if (!allowPast && value.isBefore(java.time.LocalDateTime.now())) {
                throw new IllegalArgumentException(fieldName + " cannot be in the past");
            }
            if (maxYearsInFuture > 0 && value.isAfter(java.time.LocalDateTime.now().plusYears(maxYearsInFuture))) {
                throw new IllegalArgumentException(fieldName + " cannot be more than " + maxYearsInFuture + " years in the future");
            }
        }
    }

    /**
     * Validates an optional integer field with minimum value constraint.
     *
     * @param value the integer to validate
     * @param fieldName the name of the field for error messages
     * @param minValue the minimum allowed value (inclusive)
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalIntegerWithMin(Integer value, String fieldName, int minValue) {
        validateOptionalObject(value, fieldName);
        if (value != null && value < minValue) {
            throw new IllegalArgumentException(fieldName + " must be at least " + minValue);
        }
    }

    /**
     * Validates an optional collection that cannot be empty when present.
     *
     * @param collection the collection to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOptionalCollectionNotEmpty(java.util.Collection<?> collection, String fieldName) {
        validateOptionalCollection(collection, fieldName);
        if (collection != null && collection.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }
}
