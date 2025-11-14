package com.finance_control.unit.goals.validation;

import com.finance_control.goals.validation.FinancialGoalValidation;
import com.finance_control.shared.enums.GoalType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("FinancialGoalValidation Tests")
class FinancialGoalValidationTest {

    @Test
    @DisplayName("validateName - with valid name should not throw")
    void validateName_WithValidName_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateName("Valid Goal Name"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateName - with null should throw")
    void validateName_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Goal Name");
    }

    @Test
    @DisplayName("validateName - with empty string should throw")
    void validateName_WithEmptyString_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Goal Name");
    }

    @Test
    @DisplayName("validateName - with too long name should throw")
    void validateName_WithTooLongName_ShouldThrow() {
        String longName = "a".repeat(101);
        assertThatThrownBy(() -> FinancialGoalValidation.validateName(longName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("validateNameForUpdate - with null should not throw")
    void validateNameForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateNameForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateNameForUpdate - with valid name should not throw")
    void validateNameForUpdate_WithValidName_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateNameForUpdate("Valid Goal Name"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateDescription - with null should not throw")
    void validateDescription_WithNull_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateDescription(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateDescription - with valid description should not throw")
    void validateDescription_WithValidDescription_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateDescription("Valid description"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateDescription - with too long description should throw")
    void validateDescription_WithTooLongDescription_ShouldThrow() {
        String longDescription = "a".repeat(501);
        assertThatThrownBy(() -> FinancialGoalValidation.validateDescription(longDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("validateGoalType - with valid type should not throw")
    void validateGoalType_WithValidType_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateGoalType(GoalType.SAVINGS))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateGoalType - with null should throw")
    void validateGoalType_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateGoalType(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Goal Type");
    }

    @Test
    @DisplayName("validateGoalTypeForUpdate - with null should not throw")
    void validateGoalTypeForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateGoalTypeForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateTargetAmount - with valid positive amount should not throw")
    void validateTargetAmount_WithValidPositiveAmount_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateTargetAmount(BigDecimal.valueOf(1000.50)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateTargetAmount - with null should throw")
    void validateTargetAmount_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateTargetAmount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target Amount");
    }

    @Test
    @DisplayName("validateTargetAmount - with zero should throw")
    void validateTargetAmount_WithZero_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateTargetAmount(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateTargetAmount - with negative amount should throw")
    void validateTargetAmount_WithNegativeAmount_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateTargetAmount(BigDecimal.valueOf(-100.00)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateTargetAmount - with more than 2 decimal places should throw")
    void validateTargetAmount_WithMoreThanTwoDecimalPlaces_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateTargetAmount(BigDecimal.valueOf(1000.123)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot have more than 2 decimal places");
    }

    @Test
    @DisplayName("validateTargetAmountForUpdate - with null should not throw")
    void validateTargetAmountForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateTargetAmountForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateTargetAmountForUpdate - with valid amount should not throw")
    void validateTargetAmountForUpdate_WithValidAmount_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateTargetAmountForUpdate(BigDecimal.valueOf(1000.50)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateTargetAmountForUpdate - with zero should throw")
    void validateTargetAmountForUpdate_WithZero_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateTargetAmountForUpdate(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateCurrentAmount - with null should not throw")
    void validateCurrentAmount_WithNull_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateCurrentAmount(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCurrentAmount - with valid non-negative amount should not throw")
    void validateCurrentAmount_WithValidNonNegativeAmount_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateCurrentAmount(BigDecimal.valueOf(500.00)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCurrentAmount - with zero should not throw")
    void validateCurrentAmount_WithZero_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateCurrentAmount(BigDecimal.ZERO))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCurrentAmount - with negative amount should throw")
    void validateCurrentAmount_WithNegativeAmount_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateCurrentAmount(BigDecimal.valueOf(-100.00)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    @DisplayName("validateCurrentAmount - with more than 2 decimal places should throw")
    void validateCurrentAmount_WithMoreThanTwoDecimalPlaces_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateCurrentAmount(BigDecimal.valueOf(500.123)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot have more than 2 decimal places");
    }

    @Test
    @DisplayName("validateDeadline - with null should not throw")
    void validateDeadline_WithNull_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateDeadline(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateDeadline - with valid date should not throw")
    void validateDeadline_WithValidDate_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateDeadline(LocalDateTime.now().plusDays(30)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateDeadline - with past date should throw")
    void validateDeadline_WithPastDate_ShouldThrow() {
        assertThatThrownBy(() -> FinancialGoalValidation.validateDeadline(LocalDateTime.now().minusDays(30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be in the past");
    }

    @Test
    @DisplayName("validateAccountId - with null should not throw")
    void validateAccountId_WithNull_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateAccountId(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateAccountId - with valid ID should not throw")
    void validateAccountId_WithValidId_ShouldNotThrow() {
        assertThatCode(() -> FinancialGoalValidation.validateAccountId(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateNameUnique - with unique name should not throw")
    void validateNameUnique_WithUniqueName_ShouldNotThrow() {
        Predicate<String> nameExists = name -> false;
        assertThatCode(() -> FinancialGoalValidation.validateNameUnique("Unique Goal", nameExists))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateNameUnique - with existing name should throw")
    void validateNameUnique_WithExistingName_ShouldThrow() {
        Predicate<String> nameExists = name -> true;
        assertThatThrownBy(() -> FinancialGoalValidation.validateNameUnique("Existing Goal", nameExists))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("validateNameUnique - with null name should throw")
    void validateNameUnique_WithNullName_ShouldThrow() {
        Predicate<String> nameExists = name -> false;
        assertThatThrownBy(() -> FinancialGoalValidation.validateNameUnique(null, nameExists))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Goal Name");
    }
}
