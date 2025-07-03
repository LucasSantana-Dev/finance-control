package com.finance_control.shared.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

public final class ValidationUtils {

    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isValidPercentage(BigDecimal percentage) {
        return percentage != null &&
                percentage.compareTo(BigDecimal.ZERO) >= 0 &&
                percentage.compareTo(BigDecimal.valueOf(100)) <= 0;
    }

    public static boolean isValidDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    public static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidCollection(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static void validatePercentage(BigDecimal percentage) {
        if (!isValidPercentage(percentage)) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
    }

    public static void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (!isValidDateRange(startDate, endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }

    public static void validateAmount(BigDecimal amount) {
        if (!isValidAmount(amount)) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    public static void validateId(Long id) {
        if (!isValidId(id)) {
            throw new IllegalArgumentException("ID must be a positive number");
        }
    }

    public static void validateString(String value, String fieldName) {
        if (!isValidString(value)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    public static void validateCollection(final Collection<?> collection, String fieldName) {
        if (!isValidCollection(collection)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    public static void validateRequired(String value, String fieldName) {
        if (!isValidString(value)) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    public static void validateLength(String value, int minLength, int maxLength, String fieldName) {
        if (value != null && (value.length() < minLength || value.length() > maxLength)) {
            throw new IllegalArgumentException(fieldName + " must be between " + minLength + 
                    " and " + maxLength + " characters");
        }
    }

    public static void validateUrl(String url, String fieldName) {
        if (url != null && !url.trim().isEmpty()) {
            try {
                new java.net.URL(url);
            } catch (Exception e) {
                throw new IllegalArgumentException(fieldName + " must be a valid URL");
            }
        }
    }
}