package com.finance_control.shared.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class for safe logging operations.
 * Provides methods to sanitize values before logging to prevent CRLF injection attacks.
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
}

