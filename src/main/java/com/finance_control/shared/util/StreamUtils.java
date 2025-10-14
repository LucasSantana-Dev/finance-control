package com.finance_control.shared.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for common stream operations.
 * Provides reusable stream processing methods to reduce code duplication.
 */
public final class StreamUtils {

    private StreamUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Filters a list by a predicate and returns the filtered list.
     *
     * @param list the list to filter
     * @param predicate the filter predicate
     * @param <T> the type of elements in the list
     * @return filtered list
     */
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Maps a list to another list using the provided mapper function.
     *
     * @param list the list to map
     * @param mapper the mapping function
     * @param <T> the input type
     * @param <R> the output type
     * @return mapped list
     */
    public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Counts elements in a list that match the given predicate.
     *
     * @param list the list to count
     * @param predicate the filter predicate
     * @param <T> the type of elements in the list
     * @return count of matching elements
     */
    public static <T> long count(List<T> list, Predicate<T> predicate) {
        if (list == null) {
            return 0;
        }
        return list.stream()
                .filter(predicate)
                .count();
    }

    /**
     * Groups elements by a key function and sums values using a value function.
     *
     * @param list the list to group
     * @param keyMapper the key mapping function
     * @param valueMapper the value mapping function
     * @param <T> the input type
     * @param <K> the key type
     * @return map of grouped and summed values
     */
    public static <T, K> Map<K, BigDecimal> groupAndSum(List<T> list,
                                                       Function<T, K> keyMapper,
                                                       Function<T, BigDecimal> valueMapper) {
        if (list == null) {
            return Map.of();
        }
        return list.stream()
                .collect(Collectors.groupingBy(
                        keyMapper,
                        Collectors.reducing(BigDecimal.ZERO, valueMapper, BigDecimal::add)
                ));
    }

    /**
     * Calculates the sum of values extracted from a list.
     *
     * @param list the list to sum
     * @param valueMapper the value mapping function
     * @param <T> the input type
     * @return sum of all values
     */
    public static <T> BigDecimal sum(List<T> list, Function<T, BigDecimal> valueMapper) {
        if (list == null) {
            return BigDecimal.ZERO;
        }
        return list.stream()
                .map(valueMapper)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Finds the maximum value in a list.
     *
     * @param list the list to search
     * @param valueMapper the value mapping function
     * @param <T> the input type
     * @return maximum value, or BigDecimal.ZERO if list is empty
     */
    public static <T> BigDecimal max(List<T> list, Function<T, BigDecimal> valueMapper) {
        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return list.stream()
                .map(valueMapper)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Finds the minimum value in a list.
     *
     * @param list the list to search
     * @param valueMapper the value mapping function
     * @param <T> the input type
     * @return minimum value, or BigDecimal.ZERO if list is empty
     */
    public static <T> BigDecimal min(List<T> list, Function<T, BigDecimal> valueMapper) {
        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return list.stream()
                .map(valueMapper)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Filters a list by date range (inclusive).
     *
     * @param list the list to filter
     * @param dateExtractor function to extract date from each element
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @param <T> the type of elements in the list
     * @return filtered list
     */
    public static <T> List<T> filterByDateRange(List<T> list,
                                               Function<T, LocalDateTime> dateExtractor,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .filter(item -> {
                    LocalDateTime date = dateExtractor.apply(item);
                    return date != null &&
                           !date.isBefore(startDate) &&
                           !date.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    /**
     * Filters a list by local date range (inclusive).
     *
     * @param list the list to filter
     * @param dateExtractor function to extract local date from each element
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @param <T> the type of elements in the list
     * @return filtered list
     */
    public static <T> List<T> filterByLocalDateRange(List<T> list,
                                                    Function<T, LocalDate> dateExtractor,
                                                    LocalDate startDate,
                                                    LocalDate endDate) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .filter(item -> {
                    LocalDate date = dateExtractor.apply(item);
                    return date != null &&
                           !date.isBefore(startDate) &&
                           !date.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    /**
     * Counts elements in a list that fall within a date range.
     *
     * @param list the list to count
     * @param dateExtractor function to extract date from each element
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @param <T> the type of elements in the list
     * @return count of elements in date range
     */
    public static <T> long countByDateRange(List<T> list,
                                           Function<T, LocalDateTime> dateExtractor,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate) {
        if (list == null) {
            return 0;
        }
        return list.stream()
                .filter(item -> {
                    LocalDateTime date = dateExtractor.apply(item);
                    return date != null &&
                           !date.isBefore(startDate) &&
                           !date.isAfter(endDate);
                })
                .count();
    }

    /**
     * Counts elements in a list that fall within a local date range.
     *
     * @param list the list to count
     * @param dateExtractor function to extract local date from each element
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @param <T> the type of elements in the list
     * @return count of elements in date range
     */
    public static <T> long countByLocalDateRange(List<T> list,
                                                Function<T, LocalDate> dateExtractor,
                                                LocalDate startDate,
                                                LocalDate endDate) {
        if (list == null) {
            return 0;
        }
        return list.stream()
                .filter(item -> {
                    LocalDate date = dateExtractor.apply(item);
                    return date != null &&
                           !date.isBefore(startDate) &&
                           !date.isAfter(endDate);
                })
                .count();
    }
}
