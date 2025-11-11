package com.finance_control.unit.shared.util;

import com.finance_control.shared.util.RangeUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RangeUtilsTest {

    @Test
    void isInRange_WithNullValue_ShouldReturnFalse() {
        assertThat(RangeUtils.isInRange(null, BigDecimal.ONE, BigDecimal.TEN)).isFalse();
    }

    @Test
    void isInRange_WithNullMinValue_ShouldSkipMinCheck() {
        assertThat(RangeUtils.isInRange(new BigDecimal("5"), null, BigDecimal.TEN)).isTrue();
    }

    @Test
    void isInRange_WithNullMaxValue_ShouldSkipMaxCheck() {
        assertThat(RangeUtils.isInRange(new BigDecimal("5"), BigDecimal.ONE, null)).isTrue();
    }

    @Test
    void isInRange_WithNullMinAndMax_ShouldReturnTrue() {
        assertThat(RangeUtils.isInRange(new BigDecimal("5"), null, null)).isTrue();
    }

    @Test
    void isInRange_WithValueWithinRange_ShouldReturnTrue() {
        assertThat(RangeUtils.isInRange(new BigDecimal("5"), BigDecimal.ONE, BigDecimal.TEN)).isTrue();
    }

    @Test
    void isInRange_WithValueAtMinBoundary_ShouldReturnTrue() {
        assertThat(RangeUtils.isInRange(new BigDecimal("1"), BigDecimal.ONE, BigDecimal.TEN)).isTrue();
    }

    @Test
    void isInRange_WithValueAtMaxBoundary_ShouldReturnTrue() {
        assertThat(RangeUtils.isInRange(new BigDecimal("10"), BigDecimal.ONE, BigDecimal.TEN)).isTrue();
    }

    @Test
    void isInRange_WithValueBelowMin_ShouldReturnFalse() {
        assertThat(RangeUtils.isInRange(new BigDecimal("0.5"), BigDecimal.ONE, BigDecimal.TEN)).isFalse();
    }

    @Test
    void isInRange_WithValueAboveMax_ShouldReturnFalse() {
        assertThat(RangeUtils.isInRange(new BigDecimal("15"), BigDecimal.ONE, BigDecimal.TEN)).isFalse();
    }

    @Test
    void meetsMinimum_WithNullValue_ShouldReturnFalse() {
        assertThat(RangeUtils.meetsMinimum(null, BigDecimal.ONE)).isFalse();
    }

    @Test
    void meetsMinimum_WithNullMinValue_ShouldReturnTrue() {
        assertThat(RangeUtils.meetsMinimum(new BigDecimal("5"), null)).isTrue();
    }

    @Test
    void meetsMinimum_WithValueGreaterThanMin_ShouldReturnTrue() {
        assertThat(RangeUtils.meetsMinimum(new BigDecimal("5"), BigDecimal.ONE)).isTrue();
    }

    @Test
    void meetsMinimum_WithValueEqualMin_ShouldReturnTrue() {
        assertThat(RangeUtils.meetsMinimum(new BigDecimal("1"), BigDecimal.ONE)).isTrue();
    }

    @Test
    void meetsMinimum_WithValueLessThanMin_ShouldReturnFalse() {
        assertThat(RangeUtils.meetsMinimum(new BigDecimal("0.5"), BigDecimal.ONE)).isFalse();
    }

    @Test
    void meetsMaximum_WithNullValue_ShouldReturnFalse() {
        assertThat(RangeUtils.meetsMaximum(null, BigDecimal.TEN)).isFalse();
    }

    @Test
    void meetsMaximum_WithNullMaxValue_ShouldReturnTrue() {
        assertThat(RangeUtils.meetsMaximum(new BigDecimal("5"), null)).isTrue();
    }

    @Test
    void meetsMaximum_WithValueLessThanMax_ShouldReturnTrue() {
        assertThat(RangeUtils.meetsMaximum(new BigDecimal("5"), BigDecimal.TEN)).isTrue();
    }

    @Test
    void meetsMaximum_WithValueEqualMax_ShouldReturnTrue() {
        assertThat(RangeUtils.meetsMaximum(new BigDecimal("10"), BigDecimal.TEN)).isTrue();
    }

    @Test
    void meetsMaximum_WithValueGreaterThanMax_ShouldReturnFalse() {
        assertThat(RangeUtils.meetsMaximum(new BigDecimal("15"), BigDecimal.TEN)).isFalse();
    }

    @Test
    void isPositive_WithPositiveValue_ShouldReturnTrue() {
        assertThat(RangeUtils.isPositive(new BigDecimal("5"))).isTrue();
    }

    @Test
    void isPositive_WithZero_ShouldReturnFalse() {
        assertThat(RangeUtils.isPositive(BigDecimal.ZERO)).isFalse();
    }

    @Test
    void isPositive_WithNegativeValue_ShouldReturnFalse() {
        assertThat(RangeUtils.isPositive(new BigDecimal("-5"))).isFalse();
    }

    @Test
    void isPositive_WithNullValue_ShouldReturnFalse() {
        assertThat(RangeUtils.isPositive(null)).isFalse();
    }

    @Test
    void isNonNegative_WithPositiveValue_ShouldReturnTrue() {
        assertThat(RangeUtils.isNonNegative(new BigDecimal("5"))).isTrue();
    }

    @Test
    void isNonNegative_WithZero_ShouldReturnTrue() {
        assertThat(RangeUtils.isNonNegative(BigDecimal.ZERO)).isTrue();
    }

    @Test
    void isNonNegative_WithNegativeValue_ShouldReturnFalse() {
        assertThat(RangeUtils.isNonNegative(new BigDecimal("-5"))).isFalse();
    }

    @Test
    void isNonNegative_WithNullValue_ShouldReturnFalse() {
        assertThat(RangeUtils.isNonNegative(null)).isFalse();
    }

    @Test
    void isPercentage_WithValidPercentage_ShouldReturnTrue() {
        assertThat(RangeUtils.isPercentage(new BigDecimal("50"))).isTrue();
    }

    @Test
    void isPercentage_WithZero_ShouldReturnTrue() {
        assertThat(RangeUtils.isPercentage(BigDecimal.ZERO)).isTrue();
    }

    @Test
    void isPercentage_WithHundred_ShouldReturnTrue() {
        assertThat(RangeUtils.isPercentage(new BigDecimal("100"))).isTrue();
    }

    @Test
    void isPercentage_WithNegative_ShouldReturnFalse() {
        assertThat(RangeUtils.isPercentage(new BigDecimal("-10"))).isFalse();
    }

    @Test
    void isPercentage_WithOverHundred_ShouldReturnFalse() {
        assertThat(RangeUtils.isPercentage(new BigDecimal("150"))).isFalse();
    }

    @Test
    void isPercentage_WithNull_ShouldReturnFalse() {
        assertThat(RangeUtils.isPercentage(null)).isFalse();
    }

    @Test
    void inRangePredicate_ShouldWorkCorrectly() {
        var predicate = RangeUtils.inRangePredicate(BigDecimal.ONE, BigDecimal.TEN);
        assertThat(predicate.test(new BigDecimal("5"))).isTrue();
        assertThat(predicate.test(new BigDecimal("0.5"))).isFalse();
        assertThat(predicate.test(new BigDecimal("15"))).isFalse();
    }

    @Test
    void meetsMinimumPredicate_ShouldWorkCorrectly() {
        var predicate = RangeUtils.meetsMinimumPredicate(BigDecimal.ONE);
        assertThat(predicate.test(new BigDecimal("5"))).isTrue();
        assertThat(predicate.test(new BigDecimal("0.5"))).isFalse();
    }

    @Test
    void meetsMaximumPredicate_ShouldWorkCorrectly() {
        var predicate = RangeUtils.meetsMaximumPredicate(BigDecimal.TEN);
        assertThat(predicate.test(new BigDecimal("5"))).isTrue();
        assertThat(predicate.test(new BigDecimal("15"))).isFalse();
    }

    @Test
    void isPositivePredicate_ShouldWorkCorrectly() {
        var predicate = RangeUtils.isPositivePredicate();
        assertThat(predicate.test(new BigDecimal("5"))).isTrue();
        assertThat(predicate.test(BigDecimal.ZERO)).isFalse();
        assertThat(predicate.test(new BigDecimal("-5"))).isFalse();
    }

    @Test
    void isNonNegativePredicate_ShouldWorkCorrectly() {
        var predicate = RangeUtils.isNonNegativePredicate();
        assertThat(predicate.test(new BigDecimal("5"))).isTrue();
        assertThat(predicate.test(BigDecimal.ZERO)).isTrue();
        assertThat(predicate.test(new BigDecimal("-5"))).isFalse();
    }

    @Test
    void isPercentagePredicate_ShouldWorkCorrectly() {
        var predicate = RangeUtils.isPercentagePredicate();
        assertThat(predicate.test(new BigDecimal("50"))).isTrue();
        assertThat(predicate.test(new BigDecimal("150"))).isFalse();
    }
}


