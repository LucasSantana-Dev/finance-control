package com.finance_control.shared.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.function.Function;

/**
 * Utility class for date range operations and filtering.
 * Provides reusable methods for date-based filtering and validation.
 */
public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates that the date parameter is not null.
     *
     * @param date the date to validate
     * @return true if date is not null, false otherwise
     */
    private static boolean validateDateNotNull(Object date) {
        return date != null;
    }

    /**
     * Checks if a LocalDateTime is within the specified range (inclusive).
     *
     * @param date the date to check
     * @param startDate start date (can be null to skip start check)
     * @param endDate end date (can be null to skip end check)
     * @return true if date is within range, false otherwise
     */
    public static boolean isInRange(LocalDateTime date, LocalDateTime startDate, LocalDateTime endDate) {
        if (!validateDateNotNull(date)) {
            return false;
        }

        boolean afterStart = startDate == null || !date.isBefore(startDate);
        boolean beforeEnd = endDate == null || !date.isAfter(endDate);

        return afterStart && beforeEnd;
    }

    /**
     * Checks if a LocalDate is within the specified range (inclusive).
     *
     * @param date the date to check
     * @param startDate start date (can be null to skip start check)
     * @param endDate end date (can be null to skip end check)
     * @return true if date is within range, false otherwise
     */
    public static boolean isInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (!validateDateNotNull(date)) {
            return false;
        }

        boolean afterStart = startDate == null || !date.isBefore(startDate);
        boolean beforeEnd = endDate == null || !date.isAfter(endDate);

        return afterStart && beforeEnd;
    }

    /**
     * Creates a predicate for filtering LocalDateTime values within a range.
     *
     * @param startDate start date (can be null)
     * @param endDate end date (can be null)
     * @return predicate that tests if a date is within the range
     */
    public static java.util.function.Predicate<LocalDateTime> inRangePredicate(LocalDateTime startDate, LocalDateTime endDate) {
        return date -> isInRange(date, startDate, endDate);
    }

    /**
     * Creates a predicate for filtering LocalDate values within a range.
     *
     * @param startDate start date (can be null)
     * @param endDate end date (can be null)
     * @return predicate that tests if a date is within the range
     */
    public static java.util.function.Predicate<LocalDate> inRangePredicate(LocalDate startDate, LocalDate endDate) {
        return date -> isInRange(date, startDate, endDate);
    }

    /**
     * Creates a predicate for filtering objects by LocalDateTime range.
     *
     * @param dateExtractor function to extract LocalDateTime from object
     * @param startDate start date (can be null)
     * @param endDate end date (can be null)
     * @param <T> the type of object
     * @return predicate that tests if object's date is within the range
     */
    public static <T> java.util.function.Predicate<T> inRangePredicate(Function<T, LocalDateTime> dateExtractor,
                                                                      LocalDateTime startDate,
                                                                      LocalDateTime endDate) {
        return item -> {
            LocalDateTime date = dateExtractor.apply(item);
            return isInRange(date, startDate, endDate);
        };
    }

    /**
     * Creates a predicate for filtering objects by LocalDate range.
     *
     * @param dateExtractor function to extract LocalDate from object
     * @param startDate start date (can be null)
     * @param endDate end date (can be null)
     * @param <T> the type of object
     * @return predicate that tests if object's date is within the range
     */
    public static <T> java.util.function.Predicate<T> inRangePredicate(Function<T, LocalDate> dateExtractor,
                                                                      LocalDate startDate,
                                                                      LocalDate endDate) {
        return item -> {
            LocalDate date = dateExtractor.apply(item);
            return isInRange(date, startDate, endDate);
        };
    }

    /**
     * Gets the start of the current month.
     *
     * @return LocalDate representing the first day of current month
     */
    public static LocalDate getStartOfCurrentMonth() {
        return YearMonth.now().atDay(1);
    }

    /**
     * Gets the end of the current month.
     *
     * @return LocalDate representing the last day of current month
     */
    public static LocalDate getEndOfCurrentMonth() {
        return YearMonth.now().atEndOfMonth();
    }

    /**
     * Gets the start of the current year.
     *
     * @return LocalDate representing the first day of current year
     */
    public static LocalDate getStartOfCurrentYear() {
        return LocalDate.now().withDayOfYear(1);
    }

    /**
     * Gets the end of the current year.
     *
     * @return LocalDate representing the last day of current year
     */
    public static LocalDate getEndOfCurrentYear() {
        return LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
    }

    /**
     * Checks if a date is in the current month.
     *
     * @param date the date to check
     * @return true if date is in current month, false otherwise
     */
    public static boolean isInCurrentMonth(LocalDate date) {
        if (date == null) {
            return false;
        }
        YearMonth currentMonth = YearMonth.now();
        YearMonth dateMonth = YearMonth.from(date);
        return currentMonth.equals(dateMonth);
    }

    /**
     * Checks if a date is in the current year.
     *
     * @param date the date to check
     * @return true if date is in current year, false otherwise
     */
    public static boolean isInCurrentYear(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.getYear() == LocalDate.now().getYear();
    }

    /**
     * Creates a predicate for filtering objects that are in the current month.
     *
     * @param dateExtractor function to extract LocalDate from object
     * @param <T> the type of object
     * @return predicate that tests if object's date is in current month
     */
    public static <T> java.util.function.Predicate<T> inCurrentMonthPredicate(Function<T, LocalDate> dateExtractor) {
        return item -> {
            LocalDate date = dateExtractor.apply(item);
            return isInCurrentMonth(date);
        };
    }

    /**
     * Creates a predicate for filtering objects that are in the current year.
     *
     * @param dateExtractor function to extract LocalDate from object
     * @param <T> the type of object
     * @return predicate that tests if object's date is in current year
     */
    public static <T> java.util.function.Predicate<T> inCurrentYearPredicate(Function<T, LocalDate> dateExtractor) {
        return item -> {
            LocalDate date = dateExtractor.apply(item);
            return isInCurrentYear(date);
        };
    }
}
