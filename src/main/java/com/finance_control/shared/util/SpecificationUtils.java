package com.finance_control.shared.util;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Utility class for creating common JPA Specifications.
 * Provides reusable specification builders for common filtering operations.
 */
public class SpecificationUtils {
    
    private SpecificationUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Creates a specification for exact field matching.
     * 
     * @param fieldName the name of the field to match
     * @param value the value to match against
     * @param <T> the entity type
     * @return a specification for exact matching
     */
    public static <T> Specification<T> fieldEqual(String fieldName, Object value) {
        return (root, _, criteriaBuilder) -> {
            if (value == null) return null;
            return criteriaBuilder.equal(root.get(fieldName), value);
        };
    }
    
    /**
     * Creates a specification for exact field matching with nested property.
     * 
     * @param fieldName the name of the field to match
     * @param nestedField the name of the nested field
     * @param value the value to match against
     * @param <T> the entity type
     * @return a specification for exact matching on nested field
     */
    public static <T> Specification<T> fieldEqualNested(String fieldName, String nestedField, Object value) {
        return (root, _, criteriaBuilder) -> {
            if (value == null) return null;
            return criteriaBuilder.equal(root.get(fieldName).get(nestedField), value);
        };
    }
    
    /**
     * Creates a specification for case-insensitive string matching.
     * 
     * @param fieldName the name of the field to match
     * @param value the string value to match against
     * @param <T> the entity type
     * @return a specification for case-insensitive matching
     */
    public static <T> Specification<T> likeIgnoreCase(String fieldName, String value) {
        return (root, _, criteriaBuilder) -> {
            if (value == null || value.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)), 
                "%" + value.toLowerCase() + "%"
            );
        };
    }
    
    /**
     * Creates a specification for exact string matching.
     * 
     * @param fieldName the name of the field to match
     * @param value the string value to match against
     * @param <T> the entity type
     * @return a specification for exact string matching
     */
    public static <T> Specification<T> like(String fieldName, String value) {
        return (root, _, criteriaBuilder) -> {
            if (value == null || value.trim().isEmpty()) return null;
            return criteriaBuilder.like(root.get(fieldName), "%" + value + "%");
        };
    }
    
    /**
     * Creates a specification for date range filtering.
     * 
     * @param fieldName the name of the date field
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param <T> the entity type
     * @return a specification for date range filtering
     */
    public static <T> Specification<T> dateBetween(String fieldName, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, _, criteriaBuilder) -> {
            if (startDate == null && endDate == null) return null;
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get(fieldName), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), endDate);
        };
    }
    
    /**
     * Creates a specification for numeric range filtering.
     * 
     * @param fieldName the name of the numeric field
     * @param minValue the minimum value (inclusive)
     * @param maxValue the maximum value (inclusive)
     * @param <T> the entity type
     * @return a specification for numeric range filtering
     */
    public static <T> Specification<T> numberBetween(String fieldName, Number minValue, Number maxValue) {
        return (root, _, criteriaBuilder) -> {
            if (minValue == null && maxValue == null) return null;
            if (minValue != null && maxValue != null) {
                return criteriaBuilder.between(root.get(fieldName), minValue.doubleValue(), maxValue.doubleValue());
            }
            if (minValue != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), minValue.doubleValue());
            }
            if (maxValue != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), maxValue.doubleValue());
            }
            return null;
        };
    }
    
    /**
     * Creates a specification for boolean field filtering.
     * 
     * @param fieldName the name of the boolean field
     * @param value the boolean value to match
     * @param <T> the entity type
     * @return a specification for boolean filtering
     */
    public static <T> Specification<T> isTrue(String fieldName, Boolean value) {
        return (root, _, criteriaBuilder) -> {
            if (value == null) return null;
            return value ? criteriaBuilder.isTrue(root.get(fieldName)) : criteriaBuilder.isFalse(root.get(fieldName));
        };
    }
    
    /**
     * Creates a specification for collection membership.
     * 
     * @param fieldName the name of the field to check
     * @param values the collection of values to check against
     * @param <T> the entity type
     * @return a specification for collection membership
     */
    public static <T> Specification<T> in(String fieldName, Collection<?> values) {
        return (root, _, _) -> {
            if (values == null || values.isEmpty()) return null;
            return root.get(fieldName).in(values);
        };
    }
    
    /**
     * Creates a specification for null field checking.
     * 
     * @param fieldName the name of the field to check
     * @param isNull true if field should be null, false if field should not be null
     * @param <T> the entity type
     * @return a specification for null checking
     */
    public static <T> Specification<T> isNull(String fieldName, boolean isNull) {
        return (root, _, criteriaBuilder) -> isNull ? criteriaBuilder.isNull(root.get(fieldName)) : criteriaBuilder.isNotNull(root.get(fieldName));
    }
    
    /**
     * Creates a specification for joined entity filtering.
     * 
     * @param joinField the name of the field to join
     * @param nestedField the name of the nested field in the joined entity
     * @param value the value to match against
     * @param <T> the entity type
     * @return a specification for joined entity filtering
     */
    public static <T> Specification<T> joinFieldEqual(String joinField, String nestedField, Object value) {
        return (root, _, criteriaBuilder) -> {
            if (value == null) return null;
            return criteriaBuilder.equal(root.join(joinField).get(nestedField), value);
        };
    }
    
} 