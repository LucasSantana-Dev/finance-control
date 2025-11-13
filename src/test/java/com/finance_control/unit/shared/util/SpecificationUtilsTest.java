package com.finance_control.unit.shared.util;

import com.finance_control.shared.util.SpecificationUtils;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecificationUtilsTest {

    @Mock
    private Root<TestEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> path;

    @Mock
    private Expression<String> stringExpression;

    @Mock
    private Expression<LocalDateTime> dateExpression;

    @Mock
    private Expression<Number> numberExpression;

    @Mock
    private Expression<Boolean> booleanExpression;

    @Mock
    private Predicate predicate;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        lenient().when(criteriaBuilder.like(any(), anyString())).thenReturn(predicate);
        lenient().when(criteriaBuilder.lower(any())).thenReturn(stringExpression);
        lenient().when(criteriaBuilder.isTrue(any())).thenReturn(predicate);
        lenient().when(criteriaBuilder.isFalse(any())).thenReturn(predicate);
        lenient().when(criteriaBuilder.isNull(any())).thenReturn(predicate);
        lenient().when(criteriaBuilder.isNotNull(any())).thenReturn(predicate);
        lenient().when(path.in(anyCollection())).thenReturn(predicate);
        lenient().when(criteriaBuilder.between(any(), any(Comparable.class), any(Comparable.class))).thenReturn(predicate);
        lenient().when(criteriaBuilder.greaterThanOrEqualTo(any(), any(Comparable.class))).thenReturn(predicate);
        lenient().when(criteriaBuilder.lessThanOrEqualTo(any(), any(Comparable.class))).thenReturn(predicate);
        lenient().when(criteriaBuilder.between(any(), anyDouble(), anyDouble())).thenReturn(predicate);
        lenient().when(criteriaBuilder.greaterThanOrEqualTo(any(), anyDouble())).thenReturn(predicate);
        lenient().when(criteriaBuilder.lessThanOrEqualTo(any(), anyDouble())).thenReturn(predicate);
    }

    @Test
    void fieldEqual_WithNullValue_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.fieldEqual("fieldName", null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void fieldEqual_WithNonNullValue_ShouldCreatePredicate() {
        String value = "testValue";
        lenient().when(root.get("fieldName")).thenReturn(path);
        lenient().when(criteriaBuilder.equal(path, value)).thenReturn(predicate);
        Specification<TestEntity> spec = SpecificationUtils.fieldEqual("fieldName", value);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void fieldEqualNested_WithNullValue_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.fieldEqualNested("fieldName", "nestedField", null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void fieldEqualNested_WithNonNullValue_ShouldCreatePredicate() {
        String value = "testValue";
        @SuppressWarnings("unchecked")
        Path<Object> nestedPath = mock(Path.class);
        lenient().when(root.get("fieldName")).thenReturn(path);
        lenient().when(path.get("nestedField")).thenReturn(nestedPath);
        lenient().when(criteriaBuilder.equal(nestedPath, value)).thenReturn(predicate);
        Specification<TestEntity> spec = SpecificationUtils.fieldEqualNested("fieldName", "nestedField", value);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void likeIgnoreCase_WithNullValue_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.likeIgnoreCase("fieldName", null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void likeIgnoreCase_WithEmptyString_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.likeIgnoreCase("fieldName", "");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void likeIgnoreCase_WithWhitespaceString_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.likeIgnoreCase("fieldName", "   ");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void likeIgnoreCase_WithValidValue_ShouldCreatePredicate() {
        String value = "Test";
        lenient().when(root.get("fieldName")).thenReturn(path);
        Specification<TestEntity> spec = SpecificationUtils.likeIgnoreCase("fieldName", value);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void like_WithNullValue_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.like("fieldName", null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void like_WithEmptyString_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.like("fieldName", "");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void like_WithWhitespaceString_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.like("fieldName", "   ");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void like_WithValidValue_ShouldCreatePredicate() {
        String value = "test";
        lenient().when(root.get("fieldName")).thenReturn(path);
        Specification<TestEntity> spec = SpecificationUtils.like("fieldName", value);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void dateBetween_WithBothNull_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.dateBetween("fieldName", null, null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void dateBetween_WithBothPresent_ShouldCreatePredicate() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
        lenient().when(root.get("fieldName")).thenReturn(path);
        Specification<TestEntity> spec = SpecificationUtils.dateBetween("fieldName", startDate, endDate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void dateBetween_WithOnlyStartDate_ShouldCreatePredicate() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        lenient().when(root.get("fieldName")).thenReturn(path);
        Specification<TestEntity> spec = SpecificationUtils.dateBetween("fieldName", startDate, null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void dateBetween_WithOnlyEndDate_ShouldCreatePredicate() {
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
        lenient().when(root.get("fieldName")).thenReturn(path);
        Specification<TestEntity> spec = SpecificationUtils.dateBetween("fieldName", null, endDate);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void numberBetween_WithBothNull_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.numberBetween("fieldName", null, null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void numberBetween_WithBothPresent_ShouldCreatePredicate() {
        Integer minValue = 10;
        Integer maxValue = 20;
        lenient().when(root.get("fieldName")).thenReturn(path);
        Specification<TestEntity> spec = SpecificationUtils.numberBetween("fieldName", minValue, maxValue);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void numberBetween_WithOnlyMinValue_ShouldCreatePredicate() {
        Integer minValue = 10;
        lenient().when(root.get("fieldName")).thenReturn(path);
        Specification<TestEntity> spec = SpecificationUtils.numberBetween("fieldName", minValue, null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void numberBetween_WithOnlyMaxValue_ShouldCreatePredicate() {
        Integer maxValue = 20;
        lenient().when(root.get("fieldName")).thenReturn(path);
        Specification<TestEntity> spec = SpecificationUtils.numberBetween("fieldName", null, maxValue);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void isTrue_WithNullValue_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.isTrue("fieldName", null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void isTrue_WithTrueValue_ShouldCreatePredicate() {
        lenient().when(root.get("fieldName")).thenReturn(path);
        Specification<TestEntity> spec = SpecificationUtils.isTrue("fieldName", true);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void isTrue_WithFalseValue_ShouldCreatePredicate() {
        lenient().when(root.get("fieldName")).thenReturn(path);
        Specification<TestEntity> spec = SpecificationUtils.isTrue("fieldName", false);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void in_WithNullCollection_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.in("fieldName", null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void in_WithEmptyCollection_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.in("fieldName", Collections.emptyList());

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void in_WithNonEmptyCollection_ShouldCreatePredicate() {
        List<String> values = Arrays.asList("value1", "value2", "value3");
        Specification<TestEntity> spec = SpecificationUtils.in("fieldName", values);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void isNull_WithTrue_ShouldCreatePredicate() {
        Specification<TestEntity> spec = SpecificationUtils.isNull("fieldName", true);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void isNull_WithFalse_ShouldCreatePredicate() {
        Specification<TestEntity> spec = SpecificationUtils.isNull("fieldName", false);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    void joinFieldEqual_WithNullValue_ShouldReturnNull() {
        Specification<TestEntity> spec = SpecificationUtils.joinFieldEqual("joinField", "nestedField", null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void joinFieldEqual_WithNonNullValue_ShouldCreatePredicate() {
        String value = "testValue";
        @SuppressWarnings("unchecked")
        Join<Object, Object> join = mock(Join.class);
        @SuppressWarnings("unchecked")
        Path<Object> nestedPath = mock(Path.class);
        lenient().when(root.join("joinField")).thenReturn(join);
        lenient().when(join.get("nestedField")).thenReturn(nestedPath);
        lenient().when(criteriaBuilder.equal(nestedPath, value)).thenReturn(predicate);
        Specification<TestEntity> spec = SpecificationUtils.joinFieldEqual("joinField", "nestedField", value);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    private static class TestEntity {
        // Test entity for specification testing - no fields needed
    }
}
