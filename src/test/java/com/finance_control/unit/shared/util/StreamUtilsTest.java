package com.finance_control.unit.shared.util;

import com.finance_control.shared.util.StreamUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StreamUtilsTest {

    @Test
    void filter_WithNullList_ShouldReturnEmptyList() {
        List<String> result = StreamUtils.filter(null, s -> s.length() > 3);
        assertThat(result).isEmpty();
    }

    @Test
    void filter_WithValidList_ShouldFilterCorrectly() {
        List<String> list = List.of("one", "two", "three", "four");
        List<String> result = StreamUtils.filter(list, s -> s.length() > 3);
        assertThat(result).containsExactly("three", "four");
    }

    @Test
    void map_WithNullList_ShouldReturnEmptyList() {
        List<Integer> result = StreamUtils.map(null, String::length);
        assertThat(result).isEmpty();
    }

    @Test
    void map_WithValidList_ShouldMapCorrectly() {
        List<String> list = List.of("one", "two", "three");
        List<Integer> result = StreamUtils.map(list, String::length);
        assertThat(result).containsExactly(3, 3, 5);
    }

    @Test
    void count_WithNullList_ShouldReturnZero() {
        long result = StreamUtils.<String>count(null, s -> s.length() > 3);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void count_WithValidList_ShouldCountCorrectly() {
        List<String> list = List.of("one", "two", "three", "four");
        long result = StreamUtils.count(list, s -> s.length() > 3);
        assertThat(result).isEqualTo(2);
    }

    @Test
    void groupAndSum_WithNullList_ShouldReturnEmptyMap() {
        Map<String, BigDecimal> result = StreamUtils.groupAndSum(null, Object::toString, obj -> BigDecimal.ONE);
        assertThat(result).isEmpty();
    }

    @Test
    void groupAndSum_WithValidList_ShouldGroupAndSumCorrectly() {
        List<String> list = List.of("a", "b", "a", "b", "c");
        Map<String, BigDecimal> result = StreamUtils.groupAndSum(list, s -> s, s -> BigDecimal.ONE);
        assertThat(result.get("a")).isEqualByComparingTo(new BigDecimal("2"));
        assertThat(result.get("b")).isEqualByComparingTo(new BigDecimal("2"));
        assertThat(result.get("c")).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void sum_WithNullList_ShouldReturnZero() {
        BigDecimal result = StreamUtils.sum(null, s -> BigDecimal.ONE);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sum_WithValidList_ShouldSumCorrectly() {
        List<Integer> list = List.of(1, 2, 3, 4);
        BigDecimal result = StreamUtils.sum(list, i -> BigDecimal.valueOf(i));
        assertThat(result).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    void max_WithNullList_ShouldReturnZero() {
        BigDecimal result = StreamUtils.max(null, s -> BigDecimal.ONE);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void max_WithEmptyList_ShouldReturnZero() {
        List<Integer> list = List.of();
        BigDecimal result = StreamUtils.max(list, i -> BigDecimal.valueOf(i));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void max_WithValidList_ShouldReturnMax() {
        List<Integer> list = List.of(1, 5, 3, 2);
        BigDecimal result = StreamUtils.max(list, i -> BigDecimal.valueOf(i));
        assertThat(result).isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    void min_WithNullList_ShouldReturnZero() {
        BigDecimal result = StreamUtils.min(null, s -> BigDecimal.ONE);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void min_WithEmptyList_ShouldReturnZero() {
        List<Integer> list = List.of();
        BigDecimal result = StreamUtils.min(list, i -> BigDecimal.valueOf(i));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void min_WithValidList_ShouldReturnMin() {
        List<Integer> list = List.of(5, 1, 3, 2);
        BigDecimal result = StreamUtils.min(list, i -> BigDecimal.valueOf(i));
        assertThat(result).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void filterByDateRange_WithNullList_ShouldReturnEmptyList() {
        List<Object> result = StreamUtils.filterByDateRange(null, obj -> LocalDateTime.now(),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertThat(result).isEmpty();
    }

    @Test
    void filterByDateRange_WithNullDateInItem_ShouldExcludeItem() {
        List<TestItem> list = new ArrayList<>();
        TestItem item1 = new TestItem(LocalDateTime.now());
        TestItem item2 = new TestItem(null);
        list.add(item1);
        list.add(item2);

        List<TestItem> result = StreamUtils.filterByDateRange(list, TestItem::getDate,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(item1);
    }

    @Test
    void filterByDateRange_WithDateBeforeStart_ShouldExcludeItem() {
        List<TestItem> list = List.of(new TestItem(LocalDateTime.now().minusDays(2)));
        List<TestItem> result = StreamUtils.filterByDateRange(list, TestItem::getDate,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertThat(result).isEmpty();
    }

    @Test
    void filterByDateRange_WithDateAfterEnd_ShouldExcludeItem() {
        List<TestItem> list = List.of(new TestItem(LocalDateTime.now().plusDays(2)));
        List<TestItem> result = StreamUtils.filterByDateRange(list, TestItem::getDate,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertThat(result).isEmpty();
    }

    @Test
    void filterByDateRange_WithDateAtStartBoundary_ShouldIncludeItem() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        List<TestItem> list = List.of(new TestItem(startDate));
        List<TestItem> result = StreamUtils.filterByDateRange(list, TestItem::getDate,
                startDate, LocalDateTime.now().plusDays(1));
        assertThat(result).hasSize(1);
    }

    @Test
    void filterByDateRange_WithDateAtEndBoundary_ShouldIncludeItem() {
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        List<TestItem> list = List.of(new TestItem(endDate));
        List<TestItem> result = StreamUtils.filterByDateRange(list, TestItem::getDate,
                LocalDateTime.now().minusDays(1), endDate);
        assertThat(result).hasSize(1);
    }

    @Test
    void filterByLocalDateRange_WithNullList_ShouldReturnEmptyList() {
        List<TestLocalDateItem> result = StreamUtils.filterByLocalDateRange(null,
                TestLocalDateItem::getDate, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertThat(result).isEmpty();
    }

    @Test
    void filterByLocalDateRange_WithNullDateInItem_ShouldExcludeItem() {
        List<TestLocalDateItem> list = new ArrayList<>();
        TestLocalDateItem item1 = new TestLocalDateItem(LocalDate.now());
        TestLocalDateItem item2 = new TestLocalDateItem(null);
        list.add(item1);
        list.add(item2);

        List<TestLocalDateItem> result = StreamUtils.filterByLocalDateRange(list, TestLocalDateItem::getDate,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(item1);
    }

    @Test
    void countByDateRange_WithNullList_ShouldReturnZero() {
        long result = StreamUtils.countByDateRange(null, obj -> LocalDateTime.now(),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertThat(result).isEqualTo(0);
    }

    @Test
    void countByDateRange_WithNullDateInItem_ShouldExcludeItem() {
        List<TestItem> list = new ArrayList<>();
        TestItem item1 = new TestItem(LocalDateTime.now());
        TestItem item2 = new TestItem(null);
        list.add(item1);
        list.add(item2);

        long result = StreamUtils.countByDateRange(list, TestItem::getDate,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        assertThat(result).isEqualTo(1);
    }

    @Test
    void countByLocalDateRange_WithNullList_ShouldReturnZero() {
        long result = StreamUtils.countByLocalDateRange(null, obj -> LocalDate.now(),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertThat(result).isEqualTo(0);
    }

    @Test
    void countByLocalDateRange_WithNullDateInItem_ShouldExcludeItem() {
        List<TestLocalDateItem> list = new ArrayList<>();
        TestLocalDateItem item1 = new TestLocalDateItem(LocalDate.now());
        TestLocalDateItem item2 = new TestLocalDateItem(null);
        list.add(item1);
        list.add(item2);

        long result = StreamUtils.countByLocalDateRange(list, TestLocalDateItem::getDate,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertThat(result).isEqualTo(1);
    }

    // Helper classes for testing
    static class TestItem {
        private final LocalDateTime date;

        TestItem(LocalDateTime date) {
            this.date = date;
        }

        LocalDateTime getDate() {
            return date;
        }
    }

    static class TestLocalDateItem {
        private final LocalDate date;

        TestLocalDateItem(LocalDate date) {
            this.date = date;
        }

        LocalDate getDate() {
            return date;
        }
    }
}
