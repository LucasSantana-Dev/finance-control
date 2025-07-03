package com.finance_control.unit.shared.util;

import com.finance_control.shared.util.ValidationUtils;
import com.finance_control.unit.BaseUnitTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilsTest extends BaseUnitTest {

    private static final String TEST_CANNOT_BE_NULL_OR_EMPTY = "Test cannot be null or empty";
    private static final String AMOUNT_MUST_BE_GREATER_THAN_ZERO = "Amount must be greater than zero";
    private static final String ID_MUST_BE_POSITIVE_NUMBER = "ID must be a positive number";
    private static final String PERCENTAGE_MUST_BE_BETWEEN_0_AND_100 = "Percentage must be between 0 and 100";

    @Test
    void validateString_ShouldNotThrowException_WhenValidString() {
        ValidationUtils.validateString("valid string", "Test");
    }

    @Test
    void validateString_ShouldThrowException_WhenNullString() {
        assertThatThrownBy(() -> ValidationUtils.validateString(null, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(TEST_CANNOT_BE_NULL_OR_EMPTY);
    }

    @Test
    void validateString_ShouldThrowException_WhenEmptyString() {
        assertThatThrownBy(() -> ValidationUtils.validateString("", "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(TEST_CANNOT_BE_NULL_OR_EMPTY);
    }

    @Test
    void validateString_ShouldThrowException_WhenBlankString() {
        assertThatThrownBy(() -> ValidationUtils.validateString("   ", "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(TEST_CANNOT_BE_NULL_OR_EMPTY);
    }

    @Test
    void validateAmount_ShouldNotThrowException_WhenValidAmount() {
        ValidationUtils.validateAmount(new BigDecimal("100.00"));
    }

    @Test
    void validateAmount_ShouldThrowException_WhenNullAmount() {
        assertThatThrownBy(() -> ValidationUtils.validateAmount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(AMOUNT_MUST_BE_GREATER_THAN_ZERO);
    }

    @Test
    void validateAmount_ShouldThrowException_WhenZeroAmount() {
        assertThatThrownBy(() -> ValidationUtils.validateAmount(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(AMOUNT_MUST_BE_GREATER_THAN_ZERO);
    }

    @Test
    void validateAmount_ShouldThrowException_WhenNegativeAmount() {
        assertThatThrownBy(() -> ValidationUtils.validateAmount(new BigDecimal("-100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(AMOUNT_MUST_BE_GREATER_THAN_ZERO);
    }

    @Test
    void validateId_ShouldNotThrowException_WhenValidId() {
        ValidationUtils.validateId(1L);
    }

    @Test
    void validateId_ShouldThrowException_WhenNullId() {
        assertThatThrownBy(() -> ValidationUtils.validateId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ID_MUST_BE_POSITIVE_NUMBER);
    }

    @Test
    void validateId_ShouldThrowException_WhenNegativeId() {
        assertThatThrownBy(() -> ValidationUtils.validateId(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ID_MUST_BE_POSITIVE_NUMBER);
    }

    @Test
    void validateId_ShouldThrowException_WhenZeroId() {
        assertThatThrownBy(() -> ValidationUtils.validateId(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ID_MUST_BE_POSITIVE_NUMBER);
    }

    @Test
    void validatePercentage_ShouldNotThrowException_WhenValidPercentage() {
        ValidationUtils.validatePercentage(new BigDecimal("50.0"));
    }

    @Test
    void validatePercentage_ShouldThrowException_WhenNullPercentage() {
        assertThatThrownBy(() -> ValidationUtils.validatePercentage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PERCENTAGE_MUST_BE_BETWEEN_0_AND_100);
    }

    @Test
    void validatePercentage_ShouldThrowException_WhenNegativePercentage() {
        assertThatThrownBy(() -> ValidationUtils.validatePercentage(new BigDecimal("-10.0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PERCENTAGE_MUST_BE_BETWEEN_0_AND_100);
    }

    @Test
    void validatePercentage_ShouldThrowException_WhenOver100Percentage() {
        assertThatThrownBy(() -> ValidationUtils.validatePercentage(new BigDecimal("150.0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PERCENTAGE_MUST_BE_BETWEEN_0_AND_100);
    }

    @Test
    void validateDateRange_ShouldNotThrowException_WhenValidDateRange() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(1);
        ValidationUtils.validateDateRange(startDate, endDate);
    }

    @Test
    void validateDateRange_ShouldThrowException_WhenStartDateAfterEndDate() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        
        assertThatThrownBy(() -> ValidationUtils.validateDateRange(startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date must be before or equal to end date");
    }

    @Test
    void validateCollection_ShouldNotThrowException_WhenValidCollection() {
        ValidationUtils.validateCollection(Arrays.asList("item1", "item2"), "Test");
    }

    @Test
    void validateCollection_ShouldThrowException_WhenNullCollection() {
        assertThatThrownBy(() -> ValidationUtils.validateCollection(null, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(TEST_CANNOT_BE_NULL_OR_EMPTY);
    }

    @Test
    void validateCollection_ShouldThrowException_WhenEmptyCollection() {
        assertThatThrownBy(() -> ValidationUtils.validateCollection(Collections.emptyList(), "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(TEST_CANNOT_BE_NULL_OR_EMPTY);
    }

    @Test
    void validateLength_ShouldNotThrowException_WhenValidLength() {
        ValidationUtils.validateLength("test", 1, 10, "Test");
    }

    @Test
    void validateLength_ShouldNotThrowException_WhenNullValue() {
        ValidationUtils.validateLength(null, 1, 10, "Test");
    }

    @Test
    void validateLength_ShouldThrowException_WhenTooShort() {
        assertThatThrownBy(() -> ValidationUtils.validateLength("a", 2, 10, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test must be between 2 and 10 characters");
    }

    @Test
    void validateLength_ShouldThrowException_WhenTooLong() {
        assertThatThrownBy(() -> ValidationUtils.validateLength("very long string", 1, 5, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test must be between 1 and 5 characters");
    }

    @Test
    void validateUrl_ShouldNotThrowException_WhenValidUrl() {
        ValidationUtils.validateUrl("https://example.com", "Test");
    }

    @Test
    void validateUrl_ShouldNotThrowException_WhenNullUrl() {
        ValidationUtils.validateUrl(null, "Test");
    }

    @Test
    void validateUrl_ShouldNotThrowException_WhenEmptyUrl() {
        ValidationUtils.validateUrl("", "Test");
    }

    @Test
    void validateUrl_ShouldThrowException_WhenInvalidUrl() {
        assertThatThrownBy(() -> ValidationUtils.validateUrl("invalid-url", "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test must be a valid URL");
    }
} 