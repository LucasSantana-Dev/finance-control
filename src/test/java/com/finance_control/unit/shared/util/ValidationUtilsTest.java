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

    @Test
    void validateUrl_ShouldNotThrowException_WhenWhitespaceUrl() {
        ValidationUtils.validateUrl("   ", "Test");
    }

    @Test
    void validateUrl_ShouldThrowException_WhenInvalidUrlFormat() {
        assertThatThrownBy(() -> ValidationUtils.validateUrl("not a url", "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test must be a valid URL");
    }

    @Test
    void validateRequired_ShouldNotThrowException_WhenValidString() {
        ValidationUtils.validateRequired("valid string", "Test");
    }

    @Test
    void validateRequired_ShouldThrowException_WhenNullString() {
        assertThatThrownBy(() -> ValidationUtils.validateRequired(null, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test is required");
    }

    @Test
    void validateRequired_ShouldThrowException_WhenEmptyString() {
        assertThatThrownBy(() -> ValidationUtils.validateRequired("", "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test is required");
    }

    @Test
    void validateRequired_ShouldThrowException_WhenBlankString() {
        assertThatThrownBy(() -> ValidationUtils.validateRequired("   ", "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test is required");
    }

    @Test
    void validatePercentage_ShouldNotThrowException_WhenZeroPercentage() {
        ValidationUtils.validatePercentage(BigDecimal.ZERO);
    }

    @Test
    void validatePercentage_ShouldNotThrowException_When100Percentage() {
        ValidationUtils.validatePercentage(BigDecimal.valueOf(100));
    }

    @Test
    void validateDateRange_ShouldNotThrowException_WhenEqualDates() {
        LocalDateTime date = LocalDateTime.now();
        ValidationUtils.validateDateRange(date, date);
    }

    @Test
    void validateDateRange_ShouldThrowException_WhenNullStartDate() {
        LocalDateTime endDate = LocalDateTime.now();
        assertThatThrownBy(() -> ValidationUtils.validateDateRange(null, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date must be before or equal to end date");
    }

    @Test
    void validateDateRange_ShouldThrowException_WhenNullEndDate() {
        LocalDateTime startDate = LocalDateTime.now();
        assertThatThrownBy(() -> ValidationUtils.validateDateRange(startDate, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date must be before or equal to end date");
    }

    @Test
    void validateDateRange_ShouldThrowException_WhenBothDatesNull() {
        assertThatThrownBy(() -> ValidationUtils.validateDateRange(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date must be before or equal to end date");
    }

    @Test
    void validateLength_ShouldNotThrowException_WhenValueEqualsMinLength() {
        ValidationUtils.validateLength("ab", 2, 10, "Test");
    }

    @Test
    void validateLength_ShouldNotThrowException_WhenValueEqualsMaxLength() {
        ValidationUtils.validateLength("abcdefghij", 1, 10, "Test");
    }

    @Test
    void validateLength_ShouldNotThrowException_WhenMinLengthEqualsMaxLength() {
        ValidationUtils.validateLength("ab", 2, 2, "Test");
    }

    @Test
    void validateLength_ShouldThrowException_WhenValueTooShortForEqualMinMax() {
        assertThatThrownBy(() -> ValidationUtils.validateLength("a", 2, 2, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test must be between 2 and 2 characters");
    }

    @Test
    void validateLength_ShouldThrowException_WhenValueTooLongForEqualMinMax() {
        assertThatThrownBy(() -> ValidationUtils.validateLength("abc", 2, 2, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test must be between 2 and 2 characters");
    }

    @Test
    void isValidPercentage_ShouldReturnTrue_WhenValidPercentage() {
        assertThat(ValidationUtils.isValidPercentage(new BigDecimal("50.0"))).isTrue();
    }

    @Test
    void isValidPercentage_ShouldReturnTrue_WhenZeroPercentage() {
        assertThat(ValidationUtils.isValidPercentage(BigDecimal.ZERO)).isTrue();
    }

    @Test
    void isValidPercentage_ShouldReturnTrue_When100Percentage() {
        assertThat(ValidationUtils.isValidPercentage(BigDecimal.valueOf(100))).isTrue();
    }

    @Test
    void isValidPercentage_ShouldReturnFalse_WhenNullPercentage() {
        assertThat(ValidationUtils.isValidPercentage(null)).isFalse();
    }

    @Test
    void isValidPercentage_ShouldReturnFalse_WhenNegativePercentage() {
        assertThat(ValidationUtils.isValidPercentage(new BigDecimal("-10.0"))).isFalse();
    }

    @Test
    void isValidPercentage_ShouldReturnFalse_WhenOver100Percentage() {
        assertThat(ValidationUtils.isValidPercentage(new BigDecimal("150.0"))).isFalse();
    }

    @Test
    void isValidDateRange_ShouldReturnTrue_WhenValidDateRange() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(1);
        assertThat(ValidationUtils.isValidDateRange(startDate, endDate)).isTrue();
    }

    @Test
    void isValidDateRange_ShouldReturnTrue_WhenEqualDates() {
        LocalDateTime date = LocalDateTime.now();
        assertThat(ValidationUtils.isValidDateRange(date, date)).isTrue();
    }

    @Test
    void isValidDateRange_ShouldReturnFalse_WhenNullStartDate() {
        LocalDateTime endDate = LocalDateTime.now();
        assertThat(ValidationUtils.isValidDateRange(null, endDate)).isFalse();
    }

    @Test
    void isValidDateRange_ShouldReturnFalse_WhenNullEndDate() {
        LocalDateTime startDate = LocalDateTime.now();
        assertThat(ValidationUtils.isValidDateRange(startDate, null)).isFalse();
    }

    @Test
    void isValidDateRange_ShouldReturnFalse_WhenBothDatesNull() {
        assertThat(ValidationUtils.isValidDateRange(null, null)).isFalse();
    }

    @Test
    void isValidDateRange_ShouldReturnFalse_WhenStartDateAfterEndDate() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        assertThat(ValidationUtils.isValidDateRange(startDate, endDate)).isFalse();
    }

    @Test
    void isValidAmount_ShouldReturnTrue_WhenValidAmount() {
        assertThat(ValidationUtils.isValidAmount(new BigDecimal("100.00"))).isTrue();
    }

    @Test
    void isValidAmount_ShouldReturnFalse_WhenNullAmount() {
        assertThat(ValidationUtils.isValidAmount(null)).isFalse();
    }

    @Test
    void isValidAmount_ShouldReturnFalse_WhenZeroAmount() {
        assertThat(ValidationUtils.isValidAmount(BigDecimal.ZERO)).isFalse();
    }

    @Test
    void isValidAmount_ShouldReturnFalse_WhenNegativeAmount() {
        assertThat(ValidationUtils.isValidAmount(new BigDecimal("-100.00"))).isFalse();
    }

    @Test
    void isValidId_ShouldReturnTrue_WhenValidId() {
        assertThat(ValidationUtils.isValidId(1L)).isTrue();
    }

    @Test
    void isValidId_ShouldReturnFalse_WhenNullId() {
        assertThat(ValidationUtils.isValidId(null)).isFalse();
    }

    @Test
    void isValidId_ShouldReturnFalse_WhenNegativeId() {
        assertThat(ValidationUtils.isValidId(-1L)).isFalse();
    }

    @Test
    void isValidId_ShouldReturnFalse_WhenZeroId() {
        assertThat(ValidationUtils.isValidId(0L)).isFalse();
    }

    @Test
    void isValidString_ShouldReturnTrue_WhenValidString() {
        assertThat(ValidationUtils.isValidString("valid string")).isTrue();
    }

    @Test
    void isValidString_ShouldReturnFalse_WhenNullString() {
        assertThat(ValidationUtils.isValidString(null)).isFalse();
    }

    @Test
    void isValidString_ShouldReturnFalse_WhenEmptyString() {
        assertThat(ValidationUtils.isValidString("")).isFalse();
    }

    @Test
    void isValidString_ShouldReturnFalse_WhenBlankString() {
        assertThat(ValidationUtils.isValidString("   ")).isFalse();
    }

    @Test
    void isValidCollection_ShouldReturnTrue_WhenValidCollection() {
        assertThat(ValidationUtils.isValidCollection(Arrays.asList("item1", "item2"))).isTrue();
    }

    @Test
    void isValidCollection_ShouldReturnFalse_WhenNullCollection() {
        assertThat(ValidationUtils.isValidCollection(null)).isFalse();
    }

    @Test
    void isValidCollection_ShouldReturnFalse_WhenEmptyCollection() {
        assertThat(ValidationUtils.isValidCollection(Collections.emptyList())).isFalse();
    }
}
