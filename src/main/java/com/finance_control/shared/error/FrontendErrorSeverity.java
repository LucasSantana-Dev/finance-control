package com.finance_control.shared.error;

/**
 * Severity levels reported by frontend clients.
 */
public enum FrontendErrorSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW;

    /**
     * Returns whether this severity is greater than or equal to the given severity.
     *
     * @param other severity to compare against
     * @return true if this severity is greater than or equal to {@code other}
     */
    public boolean isAtLeast(FrontendErrorSeverity other) {
        return this.ordinal() <= other.ordinal();
    }

    /**
     * Resolve a {@link FrontendErrorSeverity} from an arbitrary string value.
     * Defaults to {@link FrontendErrorSeverity#MEDIUM} when the value is null or unknown.
     *
     * @param value user-provided severity string
     * @return resolved severity enum
     */
    public static FrontendErrorSeverity from(String value) {
        if (value == null || value.isBlank()) {
            return MEDIUM;
        }
        try {
            return FrontendErrorSeverity.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return MEDIUM;
        }
    }
}
