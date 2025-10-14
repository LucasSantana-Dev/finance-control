package com.finance_control.shared.util;

import java.math.BigDecimal;

/**
 * Utility class for range validation and filtering operations.
 * Provides reusable methods for checking if values fall within specified ranges.
 */
public final class RangeUtils {

    private RangeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if a BigDecimal value is within the specified range (inclusive).
     *
     * @param value the value to check
     * @param minValue minimum value (can be null to skip minimum check)
     * @param maxValue maximum value (can be null to skip maximum check)
     * @return true if value is within range, false otherwise
     */
    public static boolean isInRange(BigDecimal value, BigDecimal minValue, BigDecimal maxValue) {
        if (value == null) {
            return false;
        }

        boolean meetsMin = minValue == null || value.compareTo(minValue) >= 0;
        boolean meetsMax = maxValue == null || value.compareTo(maxValue) <= 0;

        return meetsMin && meetsMax;
    }

    /**
     * Checks if a BigDecimal value is greater than or equal to the minimum value.
     *
     * @param value the value to check
     * @param minValue minimum value (can be null to skip check)
     * @return true if value meets minimum requirement, false otherwise
     */
    public static boolean meetsMinimum(BigDecimal value, BigDecimal minValue) {
        if (value == null) {
            return false;
        }
        return minValue == null || value.compareTo(minValue) >= 0;
    }

    /**
     * Checks if a BigDecimal value is less than or equal to the maximum value.
     *
     * @param value the value to check
     * @param maxValue maximum value (can be null to skip check)
     * @return true if value meets maximum requirement, false otherwise
     */
    public static boolean meetsMaximum(BigDecimal value, BigDecimal maxValue) {
        if (value == null) {
            return false;
        }
        return maxValue == null || value.compareTo(maxValue) <= 0;
    }

    /**
     * Checks if a BigDecimal value is greater than zero.
     *
     * @param value the value to check
     * @return true if value is greater than zero, false otherwise
     */
    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if a BigDecimal value is greater than or equal to zero.
     *
     * @param value the value to check
     * @return true if value is greater than or equal to zero, false otherwise
     */
    public static boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Checks if a BigDecimal value is between 0 and 100 (inclusive).
     * Useful for percentage validation.
     *
     * @param value the value to check
     * @return true if value is between 0 and 100, false otherwise
     */
    public static boolean isPercentage(BigDecimal value) {
        return isInRange(value, BigDecimal.ZERO, BigDecimal.valueOf(100));
    }

    /**
     * Creates a predicate for filtering BigDecimal values within a range.
     *
     * @param minValue minimum value (can be null)
     * @param maxValue maximum value (can be null)
     * @return predicate that tests if a value is within the range
     */
    public static java.util.function.Predicate<BigDecimal> inRangePredicate(BigDecimal minValue, BigDecimal maxValue) {
        return value -> isInRange(value, minValue, maxValue);
    }

    /**
     * Creates a predicate for filtering BigDecimal values that meet a minimum requirement.
     *
     * @param minValue minimum value (can be null)
     * @return predicate that tests if a value meets the minimum
     */
    public static java.util.function.Predicate<BigDecimal> meetsMinimumPredicate(BigDecimal minValue) {
        return value -> meetsMinimum(value, minValue);
    }

    /**
     * Creates a predicate for filtering BigDecimal values that meet a maximum requirement.
     *
     * @param maxValue maximum value (can be null)
     * @return predicate that tests if a value meets the maximum
     */
    public static java.util.function.Predicate<BigDecimal> meetsMaximumPredicate(BigDecimal maxValue) {
        return value -> meetsMaximum(value, maxValue);
    }

    /**
     * Creates a predicate for filtering positive BigDecimal values.
     *
     * @return predicate that tests if a value is positive
     */
    public static java.util.function.Predicate<BigDecimal> isPositivePredicate() {
        return RangeUtils::isPositive;
    }

    /**
     * Creates a predicate for filtering non-negative BigDecimal values.
     *
     * @return predicate that tests if a value is non-negative
     */
    public static java.util.function.Predicate<BigDecimal> isNonNegativePredicate() {
        return RangeUtils::isNonNegative;
    }

    /**
     * Creates a predicate for filtering percentage BigDecimal values (0-100).
     *
     * @return predicate that tests if a value is a valid percentage
     */
    public static java.util.function.Predicate<BigDecimal> isPercentagePredicate() {
        return RangeUtils::isPercentage;
    }
}
