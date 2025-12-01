package com.finance_control.shared.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class for safe logging operations.
 * Provides methods to sanitize values before logging to prevent:
 * - CRLF injection attacks
 * - Sensitive data exposure (passwords, secrets, tokens, keys)
 * - PII (Personally Identifiable Information) exposure
 */
@UtilityClass
public class LoggingUtils {

    /**
     * Sanitizes a value for safe logging by replacing CRLF characters with underscores.
     * This prevents log injection attacks where user input could manipulate log entries.
     *
     * @param value the value to sanitize
     * @return the sanitized value, or null if input was null
     */
    public static String sanitizeForLogging(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString().replaceAll("[\\r\\n]", "_");
    }

    /**
     * Masks sensitive values (passwords, secrets, tokens, keys) in log messages.
     * Shows only first 4 characters followed by asterisks.
     *
     * @param value the sensitive value to mask
     * @return masked value (e.g., "pass***" for "password123")
     */
    public static String maskSensitiveValue(Object value) {
        if (value == null) {
            return null;
        }
        String str = value.toString();
        if (str.length() <= 4) {
            return "***";
        }
        return str.substring(0, 4) + "***";
    }

    /**
     * Masks email addresses to protect PII while still allowing identification.
     * Shows first 3 characters and domain (e.g., "use***@example.com").
     *
     * @param email the email address to mask
     * @return masked email address, or null if input was null or invalid
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() <= 3) {
            return "***" + domain;
        }
        return localPart.substring(0, 3) + "***" + domain;
    }

    /**
     * Checks if a field name indicates sensitive data that should be masked.
     *
     * @param fieldName the field name to check
     * @return true if the field contains sensitive data indicators
     */
    public static boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String lower = fieldName.toLowerCase();
        return lower.contains("password") ||
               lower.contains("secret") ||
               lower.contains("token") ||
               lower.contains("key") ||
               lower.contains("api_key") ||
               lower.contains("jwt") ||
               lower.contains("credential");
    }
}
