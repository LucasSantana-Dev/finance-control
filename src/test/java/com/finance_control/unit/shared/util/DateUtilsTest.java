package com.finance_control.unit.shared.util;

import com.finance_control.shared.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilsTest {

    @Test
    void isInRange_LocalDateTime_WithNullDate_ShouldReturnFalse() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        boolean result = DateUtils.isInRange(null, startDate, endDate);

        assertThat(result).isFalse();
    }

    @Test
    void isInRange_LocalDateTime_WithNullStartDate_ShouldCheckOnlyEndDate() {
        LocalDateTime date = LocalDateTime.of(2024, 6, 15, 12, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        boolean result = DateUtils.isInRange(date, null, endDate);

        assertThat(result).isTrue();
    }

    @Test
    void isInRange_LocalDateTime_WithNullEndDate_ShouldCheckOnlyStartDate() {
        LocalDateTime date = LocalDateTime.of(2024, 6, 15, 12, 0);
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);

        boolean result = DateUtils.isInRange(date, startDate, null);

        assertThat(result).isTrue();
    }

    @Test
    void isInRange_LocalDateTime_WithBothNull_ShouldReturnTrue() {
        LocalDateTime date = LocalDateTime.of(2024, 6, 15, 12, 0);

        boolean result = DateUtils.isInRange(date, null, null);

        assertThat(result).isTrue();
    }

    @Test
    void isInRange_LocalDateTime_WithinRange_ShouldReturnTrue() {
        LocalDateTime date = LocalDateTime.of(2024, 6, 15, 12, 0);
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        boolean result = DateUtils.isInRange(date, startDate, endDate);

        assertThat(result).isTrue();
    }

    @Test
    void isInRange_LocalDateTime_BeforeStart_ShouldReturnFalse() {
        LocalDateTime date = LocalDateTime.of(2023, 12, 31, 23, 59);
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        boolean result = DateUtils.isInRange(date, startDate, endDate);

        assertThat(result).isFalse();
    }

    @Test
    void isInRange_LocalDateTime_AfterEnd_ShouldReturnFalse() {
        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        boolean result = DateUtils.isInRange(date, startDate, endDate);

        assertThat(result).isFalse();
    }

    @Test
    void isInRange_LocalDateTime_AtStartBoundary_ShouldReturnTrue() {
        LocalDateTime date = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        boolean result = DateUtils.isInRange(date, startDate, endDate);

        assertThat(result).isTrue();
    }

    @Test
    void isInRange_LocalDateTime_AtEndBoundary_ShouldReturnTrue() {
        LocalDateTime date = LocalDateTime.of(2024, 12, 31, 23, 59);
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        boolean result = DateUtils.isInRange(date, startDate, endDate);

        assertThat(result).isTrue();
    }

    @Test
    void isInRange_LocalDate_WithNullDate_ShouldReturnFalse() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        boolean result = DateUtils.isInRange(null, startDate, endDate);

        assertThat(result).isFalse();
    }

    @Test
    void isInRange_LocalDate_WithNullStartDate_ShouldCheckOnlyEndDate() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        boolean result = DateUtils.isInRange(date, null, endDate);

        assertThat(result).isTrue();
    }

    @Test
    void isInRange_LocalDate_WithNullEndDate_ShouldCheckOnlyStartDate() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        LocalDate startDate = LocalDate.of(2024, 1, 1);

        boolean result = DateUtils.isInRange(date, startDate, null);

        assertThat(result).isTrue();
    }

    @Test
    void isInRange_LocalDate_WithBothNull_ShouldReturnTrue() {
        LocalDate date = LocalDate.of(2024, 6, 15);

        boolean result = DateUtils.isInRange(date, null, null);

        assertThat(result).isTrue();
    }

    @Test
    void isInRange_LocalDate_WithinRange_ShouldReturnTrue() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        boolean result = DateUtils.isInRange(date, startDate, endDate);

        assertThat(result).isTrue();
    }

    @Test
    void isInRange_LocalDate_BeforeStart_ShouldReturnFalse() {
        LocalDate date = LocalDate.of(2023, 12, 31);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        boolean result = DateUtils.isInRange(date, startDate, endDate);

        assertThat(result).isFalse();
    }

    @Test
    void isInRange_LocalDate_AfterEnd_ShouldReturnFalse() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        boolean result = DateUtils.isInRange(date, startDate, endDate);

        assertThat(result).isFalse();
    }

    @Test
    void inRangePredicate_LocalDateTime_ShouldFilterCorrectly() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        var predicate = DateUtils.inRangePredicate(startDate, endDate);

        assertThat(predicate.test(LocalDateTime.of(2024, 6, 15, 12, 0))).isTrue();
        assertThat(predicate.test(LocalDateTime.of(2023, 12, 31, 23, 59))).isFalse();
        assertThat(predicate.test(null)).isFalse();
    }

    @Test
    void inRangePredicate_LocalDate_ShouldFilterCorrectly() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        var predicate = DateUtils.inRangePredicate(startDate, endDate);

        assertThat(predicate.test(LocalDate.of(2024, 6, 15))).isTrue();
        assertThat(predicate.test(LocalDate.of(2023, 12, 31))).isFalse();
        assertThat(predicate.test(null)).isFalse();
    }

    @Test
    void inRangePredicate_WithObjectExtractor_LocalDateTime_ShouldFilterCorrectly() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        Function<TestObject, LocalDateTime> extractor = TestObject::getDateTime;
        var predicate = DateUtils.inRangePredicate(extractor, startDate, endDate);

        TestObject obj1 = new TestObject(LocalDateTime.of(2024, 6, 15, 12, 0));
        TestObject obj2 = new TestObject(LocalDateTime.of(2023, 12, 31, 23, 59));
        LocalDateTime nullDateTime = null;
        TestObject obj3 = new TestObject(nullDateTime);

        assertThat(predicate.test(obj1)).isTrue();
        assertThat(predicate.test(obj2)).isFalse();
        assertThat(predicate.test(obj3)).isFalse();
    }

    @Test
    void inRangePredicate_WithObjectExtractor_LocalDate_ShouldFilterCorrectly() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        Function<TestObject, LocalDate> extractor = TestObject::getDate;
        var predicate = DateUtils.inRangePredicate(extractor, startDate, endDate);

        TestObject obj1 = new TestObject(LocalDate.of(2024, 6, 15));
        TestObject obj2 = new TestObject(LocalDate.of(2023, 12, 31));
        LocalDate nullDate = null;
        TestObject obj3 = new TestObject(nullDate);

        assertThat(predicate.test(obj1)).isTrue();
        assertThat(predicate.test(obj2)).isFalse();
        assertThat(predicate.test(obj3)).isFalse();
    }

    @Test
    void getStartOfCurrentMonth_ShouldReturnFirstDayOfMonth() {
        LocalDate result = DateUtils.getStartOfCurrentMonth();

        assertThat(result).isNotNull();
        assertThat(result.getDayOfMonth()).isEqualTo(1);
        assertThat(result).isEqualTo(YearMonth.now().atDay(1));
    }

    @Test
    void getEndOfCurrentMonth_ShouldReturnLastDayOfMonth() {
        LocalDate result = DateUtils.getEndOfCurrentMonth();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(YearMonth.now().atEndOfMonth());
    }

    @Test
    void getStartOfCurrentYear_ShouldReturnFirstDayOfYear() {
        LocalDate result = DateUtils.getStartOfCurrentYear();

        assertThat(result).isNotNull();
        assertThat(result.getDayOfYear()).isEqualTo(1);
        assertThat(result.getMonthValue()).isEqualTo(1);
        assertThat(result.getDayOfMonth()).isEqualTo(1);
    }

    @Test
    void getEndOfCurrentYear_ShouldReturnLastDayOfYear() {
        LocalDate result = DateUtils.getEndOfCurrentYear();

        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(LocalDate.now().getYear());
        assertThat(result.getDayOfYear()).isEqualTo(LocalDate.now().lengthOfYear());
    }

    @Test
    void isInCurrentMonth_WithNullDate_ShouldReturnFalse() {
        boolean result = DateUtils.isInCurrentMonth(null);

        assertThat(result).isFalse();
    }

    @Test
    void isInCurrentMonth_WithCurrentMonthDate_ShouldReturnTrue() {
        LocalDate currentDate = LocalDate.now();

        boolean result = DateUtils.isInCurrentMonth(currentDate);

        assertThat(result).isTrue();
    }

    @Test
    void isInCurrentMonth_WithPreviousMonthDate_ShouldReturnFalse() {
        LocalDate previousMonth = LocalDate.now().minusMonths(1);

        boolean result = DateUtils.isInCurrentMonth(previousMonth);

        assertThat(result).isFalse();
    }

    @Test
    void isInCurrentMonth_WithNextMonthDate_ShouldReturnFalse() {
        LocalDate nextMonth = LocalDate.now().plusMonths(1);

        boolean result = DateUtils.isInCurrentMonth(nextMonth);

        assertThat(result).isFalse();
    }

    @Test
    void isInCurrentYear_WithNullDate_ShouldReturnFalse() {
        boolean result = DateUtils.isInCurrentYear(null);

        assertThat(result).isFalse();
    }

    @Test
    void isInCurrentYear_WithCurrentYearDate_ShouldReturnTrue() {
        LocalDate currentDate = LocalDate.now();

        boolean result = DateUtils.isInCurrentYear(currentDate);

        assertThat(result).isTrue();
    }

    @Test
    void isInCurrentYear_WithPreviousYearDate_ShouldReturnFalse() {
        LocalDate previousYear = LocalDate.now().minusYears(1);

        boolean result = DateUtils.isInCurrentYear(previousYear);

        assertThat(result).isFalse();
    }

    @Test
    void isInCurrentYear_WithNextYearDate_ShouldReturnFalse() {
        LocalDate nextYear = LocalDate.now().plusYears(1);

        boolean result = DateUtils.isInCurrentYear(nextYear);

        assertThat(result).isFalse();
    }

    @Test
    void inCurrentMonthPredicate_ShouldFilterCorrectly() {
        Function<TestObject, LocalDate> extractor = TestObject::getDate;
        var predicate = DateUtils.inCurrentMonthPredicate(extractor);

        TestObject obj1 = new TestObject(LocalDate.now());
        TestObject obj2 = new TestObject(LocalDate.now().minusMonths(1));
        LocalDate nullDate2 = null;
        TestObject obj3 = new TestObject(nullDate2);

        assertThat(predicate.test(obj1)).isTrue();
        assertThat(predicate.test(obj2)).isFalse();
        assertThat(predicate.test(obj3)).isFalse();
    }

    @Test
    void inCurrentYearPredicate_ShouldFilterCorrectly() {
        Function<TestObject, LocalDate> extractor = TestObject::getDate;
        var predicate = DateUtils.inCurrentYearPredicate(extractor);

        TestObject obj1 = new TestObject(LocalDate.now());
        TestObject obj2 = new TestObject(LocalDate.now().minusYears(1));
        LocalDate nullDate3 = null;
        TestObject obj3 = new TestObject(nullDate3);

        assertThat(predicate.test(obj1)).isTrue();
        assertThat(predicate.test(obj2)).isFalse();
        assertThat(predicate.test(obj3)).isFalse();
    }

    private static class TestObject {
        private LocalDateTime dateTime;
        private LocalDate date;

        public TestObject(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            if (dateTime != null) {
                this.date = dateTime.toLocalDate();
            }
        }

        public TestObject(LocalDate date) {
            this.date = date;
            if (date != null) {
                this.dateTime = date.atStartOfDay();
            }
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public LocalDate getDate() {
            return date;
        }
    }
}
