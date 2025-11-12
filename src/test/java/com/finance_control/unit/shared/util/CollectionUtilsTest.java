package com.finance_control.unit.shared.util;

import com.finance_control.shared.util.CollectionUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionUtilsTest {

    @Test
    void isEmpty_WithNullCollection_ShouldReturnTrue() {
        assertThat(CollectionUtils.isEmpty(null)).isTrue();
    }

    @Test
    void isEmpty_WithEmptyCollection_ShouldReturnTrue() {
        assertThat(CollectionUtils.isEmpty(Collections.emptyList())).isTrue();
    }

    @Test
    void isEmpty_WithNonEmptyCollection_ShouldReturnFalse() {
        assertThat(CollectionUtils.isEmpty(Arrays.asList("item1", "item2"))).isFalse();
    }

    @Test
    void isNotEmpty_WithNullCollection_ShouldReturnFalse() {
        assertThat(CollectionUtils.isNotEmpty(null)).isFalse();
    }

    @Test
    void isNotEmpty_WithEmptyCollection_ShouldReturnFalse() {
        assertThat(CollectionUtils.isNotEmpty(Collections.emptyList())).isFalse();
    }

    @Test
    void isNotEmpty_WithNonEmptyCollection_ShouldReturnTrue() {
        assertThat(CollectionUtils.isNotEmpty(Arrays.asList("item1", "item2"))).isTrue();
    }

    @Test
    void size_WithNullCollection_ShouldReturnZero() {
        assertThat(CollectionUtils.size(null)).isEqualTo(0);
    }

    @Test
    void size_WithEmptyCollection_ShouldReturnZero() {
        assertThat(CollectionUtils.size(Collections.emptyList())).isEqualTo(0);
    }

    @Test
    void size_WithNonEmptyCollection_ShouldReturnSize() {
        assertThat(CollectionUtils.size(Arrays.asList("item1", "item2", "item3"))).isEqualTo(3);
    }

    @Test
    void filter_WithNullCollection_ShouldReturnEmptyList() {
        List<String> result = CollectionUtils.<String>filter(null, s -> s.length() > 3);

        assertThat(result).isEmpty();
    }

    @Test
    void filter_WithEmptyCollection_ShouldReturnEmptyList() {
        List<String> result = CollectionUtils.<String>filter(Collections.emptyList(), s -> s.length() > 3);

        assertThat(result).isEmpty();
    }

    @Test
    void filter_WithMatchingElements_ShouldReturnFilteredList() {
        List<String> collection = Arrays.asList("one", "two", "three", "four");
        List<String> result = CollectionUtils.filter(collection, s -> s.length() > 3);

        assertThat(result).containsExactly("three", "four");
    }

    @Test
    void filter_WithNoMatchingElements_ShouldReturnEmptyList() {
        List<String> collection = Arrays.asList("one", "two");
        List<String> result = CollectionUtils.filter(collection, s -> s.length() > 10);

        assertThat(result).isEmpty();
    }

    @Test
    void filter_WithNullPredicate_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two");

        assertThatThrownBy(() -> CollectionUtils.filter(collection, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void map_WithNullCollection_ShouldReturnEmptyList() {
        List<String> result = CollectionUtils.<String, String>map(null, String::toUpperCase);

        assertThat(result).isEmpty();
    }

    @Test
    void map_WithEmptyCollection_ShouldReturnEmptyList() {
        List<String> result = CollectionUtils.<String, String>map(Collections.emptyList(), String::toUpperCase);

        assertThat(result).isEmpty();
    }

    @Test
    void map_WithValidMapper_ShouldReturnMappedList() {
        List<String> collection = Arrays.asList("one", "two", "three");
        List<String> result = CollectionUtils.map(collection, String::toUpperCase);

        assertThat(result).containsExactly("ONE", "TWO", "THREE");
    }

    @Test
    void map_WithNullMapper_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two");

        assertThatThrownBy(() -> CollectionUtils.map(collection, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void count_WithNullCollection_ShouldReturnZero() {
        long result = CollectionUtils.<String>count(null, s -> s.length() > 3);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void count_WithEmptyCollection_ShouldReturnZero() {
        long result = CollectionUtils.<String>count(Collections.emptyList(), s -> s.length() > 3);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void count_WithMatchingElements_ShouldReturnCount() {
        List<String> collection = Arrays.asList("one", "two", "three", "four");
        long result = CollectionUtils.count(collection, s -> s.length() > 3);

        assertThat(result).isEqualTo(2);
    }

    @Test
    void count_WithNoMatchingElements_ShouldReturnZero() {
        List<String> collection = Arrays.asList("one", "two");
        long result = CollectionUtils.count(collection, s -> s.length() > 10);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void count_WithNullPredicate_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two");

        assertThatThrownBy(() -> CollectionUtils.count(collection, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void groupBy_WithNullCollection_ShouldReturnEmptyMap() {
        Map<String, List<String>> result = CollectionUtils.groupBy(null, String::toUpperCase);

        assertThat(result).isEmpty();
    }

    @Test
    void groupBy_WithEmptyCollection_ShouldReturnEmptyMap() {
        Map<String, List<String>> result = CollectionUtils.groupBy(Collections.emptyList(), String::toUpperCase);

        assertThat(result).isEmpty();
    }

    @Test
    void groupBy_WithValidKeyMapper_ShouldReturnGroupedMap() {
        List<String> collection = Arrays.asList("one", "two", "three", "four");
        Map<Integer, List<String>> result = CollectionUtils.groupBy(collection, String::length);

        assertThat(result).hasSize(3);
        assertThat(result.get(3)).containsExactlyInAnyOrder("one", "two");
        assertThat(result.get(4)).containsExactlyInAnyOrder("four");
        assertThat(result.get(5)).containsExactlyInAnyOrder("three");
    }

    @Test
    void groupBy_WithNullKeyMapper_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two");

        assertThatThrownBy(() -> CollectionUtils.groupBy(collection, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void groupBy_WithKeyAndValueMapper_WithNullCollection_ShouldReturnEmptyMap() {
        Map<String, List<Integer>> result = CollectionUtils.groupBy(null, String::toUpperCase, String::length);

        assertThat(result).isEmpty();
    }

    @Test
    void groupBy_WithKeyAndValueMapper_WithEmptyCollection_ShouldReturnEmptyMap() {
        Map<String, List<Integer>> result = CollectionUtils.groupBy(Collections.emptyList(), String::toUpperCase, String::length);

        assertThat(result).isEmpty();
    }

    @Test
    void groupBy_WithKeyAndValueMapper_WithValidMappers_ShouldReturnGroupedAndMappedMap() {
        List<String> collection = Arrays.asList("one", "two", "three");
        Map<String, List<Integer>> result = CollectionUtils.groupBy(collection, String::toUpperCase, String::length);

        assertThat(result).hasSize(3);
        assertThat(result.get("ONE")).containsExactly(3);
        assertThat(result.get("TWO")).containsExactly(3);
        assertThat(result.get("THREE")).containsExactly(5);
    }

    @Test
    void groupBy_WithKeyAndValueMapper_WithNullKeyMapper_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two");

        assertThatThrownBy(() -> CollectionUtils.groupBy(collection, null, String::length))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void groupBy_WithKeyAndValueMapper_WithNullValueMapper_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two");

        assertThatThrownBy(() -> CollectionUtils.groupBy(collection, String::toUpperCase, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void findFirst_WithNullCollection_ShouldReturnNull() {
        String result = CollectionUtils.<String>findFirst(null, s -> s.length() > 3);

        assertThat(result).isNull();
    }

    @Test
    void findFirst_WithEmptyCollection_ShouldReturnNull() {
        String result = CollectionUtils.<String>findFirst(Collections.emptyList(), s -> s.length() > 3);

        assertThat(result).isNull();
    }

    @Test
    void findFirst_WithMatchingElement_ShouldReturnFirstMatch() {
        List<String> collection = Arrays.asList("one", "two", "three", "four");
        String result = CollectionUtils.findFirst(collection, s -> s.length() > 3);

        assertThat(result).isEqualTo("three");
    }

    @Test
    void findFirst_WithNoMatchingElement_ShouldReturnNull() {
        List<String> collection = Arrays.asList("one", "two");
        String result = CollectionUtils.findFirst(collection, s -> s.length() > 10);

        assertThat(result).isNull();
    }

    @Test
    void findFirst_WithNullPredicate_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two");

        assertThatThrownBy(() -> CollectionUtils.findFirst(collection, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void anyMatch_WithNullCollection_ShouldReturnFalse() {
        boolean result = CollectionUtils.<String>anyMatch(null, s -> s.length() > 3);

        assertThat(result).isFalse();
    }

    @Test
    void anyMatch_WithEmptyCollection_ShouldReturnFalse() {
        boolean result = CollectionUtils.<String>anyMatch(Collections.emptyList(), s -> s.length() > 3);

        assertThat(result).isFalse();
    }

    @Test
    void anyMatch_WithMatchingElement_ShouldReturnTrue() {
        List<String> collection = Arrays.asList("one", "two", "three");
        boolean result = CollectionUtils.anyMatch(collection, s -> s.length() > 3);

        assertThat(result).isTrue();
    }

    @Test
    void anyMatch_WithNoMatchingElement_ShouldReturnFalse() {
        List<String> collection = Arrays.asList("one", "two");
        boolean result = CollectionUtils.anyMatch(collection, s -> s.length() > 10);

        assertThat(result).isFalse();
    }

    @Test
    void anyMatch_WithNullPredicate_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two");

        assertThatThrownBy(() -> CollectionUtils.anyMatch(collection, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void allMatch_WithNullCollection_ShouldReturnTrue() {
        boolean result = CollectionUtils.<String>allMatch(null, s -> s.length() > 3);

        assertThat(result).isTrue();
    }

    @Test
    void allMatch_WithEmptyCollection_ShouldReturnTrue() {
        boolean result = CollectionUtils.<String>allMatch(Collections.emptyList(), s -> s.length() > 3);

        assertThat(result).isTrue();
    }

    @Test
    void allMatch_WithAllMatchingElements_ShouldReturnTrue() {
        List<String> collection = Arrays.asList("four", "five", "six");
        boolean result = CollectionUtils.allMatch(collection, s -> s.length() >= 3);

        assertThat(result).isTrue();
    }

    @Test
    void allMatch_WithSomeNonMatchingElements_ShouldReturnFalse() {
        List<String> collection = Arrays.asList("one", "two", "three");
        boolean result = CollectionUtils.allMatch(collection, s -> s.length() > 3);

        assertThat(result).isFalse();
    }

    @Test
    void allMatch_WithNullPredicate_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two");

        assertThatThrownBy(() -> CollectionUtils.allMatch(collection, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void noneMatch_WithNullCollection_ShouldReturnTrue() {
        boolean result = CollectionUtils.<String>noneMatch(null, s -> s.length() > 3);

        assertThat(result).isTrue();
    }

    @Test
    void noneMatch_WithEmptyCollection_ShouldReturnTrue() {
        boolean result = CollectionUtils.<String>noneMatch(Collections.emptyList(), s -> s.length() > 3);

        assertThat(result).isTrue();
    }

    @Test
    void noneMatch_WithNoMatchingElements_ShouldReturnTrue() {
        List<String> collection = Arrays.asList("one", "two");
        boolean result = CollectionUtils.noneMatch(collection, s -> s.length() > 10);

        assertThat(result).isTrue();
    }

    @Test
    void noneMatch_WithMatchingElement_ShouldReturnFalse() {
        List<String> collection = Arrays.asList("one", "two", "three");
        boolean result = CollectionUtils.noneMatch(collection, s -> s.length() > 3);

        assertThat(result).isFalse();
    }

    @Test
    void noneMatch_WithNullPredicate_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two");

        assertThatThrownBy(() -> CollectionUtils.noneMatch(collection, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toList_WithNullCollection_ShouldReturnEmptyList() {
        List<String> result = CollectionUtils.toList(null);

        assertThat(result).isEmpty();
    }

    @Test
    void toList_WithEmptyCollection_ShouldReturnEmptyList() {
        List<String> result = CollectionUtils.toList(Collections.emptyList());

        assertThat(result).isEmpty();
    }

    @Test
    void toList_WithNonEmptyCollection_ShouldReturnList() {
        List<String> collection = Arrays.asList("one", "two", "three");
        List<String> result = CollectionUtils.toList(collection);

        assertThat(result).containsExactly("one", "two", "three");
    }

    @Test
    void toList_WithLimit_WithNullCollection_ShouldReturnEmptyList() {
        List<String> result = CollectionUtils.toList(null, 5);

        assertThat(result).isEmpty();
    }

    @Test
    void toList_WithLimit_WithEmptyCollection_ShouldReturnEmptyList() {
        List<String> result = CollectionUtils.toList(Collections.emptyList(), 5);

        assertThat(result).isEmpty();
    }

    @Test
    void toList_WithLimit_WithLimitGreaterThanSize_ShouldReturnAllElements() {
        List<String> collection = Arrays.asList("one", "two", "three");
        List<String> result = CollectionUtils.toList(collection, 10);

        assertThat(result).containsExactly("one", "two", "three");
    }

    @Test
    void toList_WithLimit_WithLimitLessThanSize_ShouldReturnLimitedElements() {
        List<String> collection = Arrays.asList("one", "two", "three", "four", "five");
        List<String> result = CollectionUtils.toList(collection, 3);

        assertThat(result).containsExactly("one", "two", "three");
    }

    @Test
    void toList_WithLimit_WithZeroLimit_ShouldReturnEmptyList() {
        List<String> collection = Arrays.asList("one", "two", "three");
        List<String> result = CollectionUtils.toList(collection, 0);

        assertThat(result).isEmpty();
    }

    @Test
    void toList_WithLimit_WithNegativeLimit_ShouldThrowException() {
        List<String> collection = Arrays.asList("one", "two", "three");

        assertThatThrownBy(() -> CollectionUtils.toList(collection, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
