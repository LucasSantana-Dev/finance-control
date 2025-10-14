package com.finance_control.shared.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for common collection operations.
 * Provides reusable methods for collection manipulation and transformation.
 */
public final class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if a collection is null or empty.
     *
     * @param collection the collection to check
     * @return true if collection is null or empty, false otherwise
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if a collection is not null and not empty.
     *
     * @param collection the collection to check
     * @return true if collection is not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Gets the size of a collection, returning 0 if null.
     *
     * @param collection the collection to get size of
     * @return size of collection, or 0 if null
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Filters a collection and returns the filtered list.
     *
     * @param collection the collection to filter
     * @param predicate the filter predicate
     * @param <T> the type of elements in the collection
     * @return filtered list
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return List.of();
        }
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Maps a collection to another list using the provided mapper function.
     *
     * @param collection the collection to map
     * @param mapper the mapping function
     * @param <T> the input type
     * @param <R> the output type
     * @return mapped list
     */
    public static <T, R> List<R> map(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection)) {
            return List.of();
        }
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Counts elements in a collection that match the given predicate.
     *
     * @param collection the collection to count
     * @param predicate the filter predicate
     * @param <T> the type of elements in the collection
     * @return count of matching elements
     */
    public static <T> long count(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return 0;
        }
        return collection.stream()
                .filter(predicate)
                .count();
    }

    /**
     * Groups elements by a key function.
     *
     * @param collection the collection to group
     * @param keyMapper the key mapping function
     * @param <T> the input type
     * @param <K> the key type
     * @return map of grouped elements
     */
    public static <T, K> Map<K, List<T>> groupBy(Collection<T> collection, Function<T, K> keyMapper) {
        if (isEmpty(collection)) {
            return Map.of();
        }
        return collection.stream()
                .collect(Collectors.groupingBy(keyMapper));
    }

    /**
     * Groups elements by a key function and maps values using a value mapper.
     *
     * @param collection the collection to group
     * @param keyMapper the key mapping function
     * @param valueMapper the value mapping function
     * @param <T> the input type
     * @param <K> the key type
     * @param <V> the value type
     * @return map of grouped and mapped values
     */
    public static <T, K, V> Map<K, List<V>> groupBy(Collection<T> collection,
                                                   Function<T, K> keyMapper,
                                                   Function<T, V> valueMapper) {
        if (isEmpty(collection)) {
            return Map.of();
        }
        return collection.stream()
                .collect(Collectors.groupingBy(
                        keyMapper,
                        Collectors.mapping(valueMapper, Collectors.toList())
                ));
    }

    /**
     * Finds the first element in a collection that matches the predicate.
     *
     * @param collection the collection to search
     * @param predicate the filter predicate
     * @param <T> the type of elements in the collection
     * @return first matching element, or null if none found
     */
    public static <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if any element in the collection matches the predicate.
     *
     * @param collection the collection to check
     * @param predicate the filter predicate
     * @param <T> the type of elements in the collection
     * @return true if any element matches, false otherwise
     */
    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return false;
        }
        return collection.stream()
                .anyMatch(predicate);
    }

    /**
     * Checks if all elements in the collection match the predicate.
     *
     * @param collection the collection to check
     * @param predicate the filter predicate
     * @param <T> the type of elements in the collection
     * @return true if all elements match, false otherwise
     */
    public static <T> boolean allMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return true; // vacuous truth
        }
        return collection.stream()
                .allMatch(predicate);
    }

    /**
     * Checks if no elements in the collection match the predicate.
     *
     * @param collection the collection to check
     * @param predicate the filter predicate
     * @param <T> the type of elements in the collection
     * @return true if no elements match, false otherwise
     */
    public static <T> boolean noneMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return true; // vacuous truth
        }
        return collection.stream()
                .noneMatch(predicate);
    }

    /**
     * Converts a collection to a list, handling null collections.
     *
     * @param collection the collection to convert
     * @param <T> the type of elements in the collection
     * @return list containing all elements, or empty list if collection is null
     */
    public static <T> List<T> toList(Collection<T> collection) {
        if (isEmpty(collection)) {
            return List.of();
        }
        return List.copyOf(collection);
    }

    /**
     * Creates a list from a collection with a limit on the number of elements.
     *
     * @param collection the collection to convert
     * @param limit maximum number of elements to include
     * @param <T> the type of elements in the collection
     * @return list containing up to limit elements
     */
    public static <T> List<T> toList(Collection<T> collection, int limit) {
        if (isEmpty(collection)) {
            return List.of();
        }
        return collection.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
