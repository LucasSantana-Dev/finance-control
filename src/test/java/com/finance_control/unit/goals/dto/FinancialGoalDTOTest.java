package com.finance_control.unit.goals.dto;

import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.shared.enums.GoalType;
import com.finance_control.unit.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("FinancialGoalDTO Validation Tests")
class FinancialGoalDTOTest extends BaseUnitTest {

    private FinancialGoalDTO createValidFinancialGoalDTO() {
        FinancialGoalDTO dto = new FinancialGoalDTO();
        dto.setName("Valid Goal");
        dto.setDescription("Valid Description");
        dto.setGoalType(GoalType.SAVINGS);
        dto.setTargetAmount(BigDecimal.valueOf(10000.00));
        dto.setCurrentAmount(BigDecimal.valueOf(1000.00));
        dto.setDeadline(LocalDateTime.now().plusMonths(12));
        return dto;
    }

    @Test
    @DisplayName("validateCreate - with valid data should not throw")
    void validateCreate_WithValidData_ShouldNotThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();

        assertThatCode(() -> dto.validateCreate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCreate - with null name should throw")
    void validateCreate_WithNullName_ShouldThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setName(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Goal Name");
    }

    @Test
    @DisplayName("validateCreate - with null goalType should throw")
    void validateCreate_WithNullGoalType_ShouldThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setGoalType(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Goal Type");
    }

    @Test
    @DisplayName("validateCreate - with null targetAmount should throw")
    void validateCreate_WithNullTargetAmount_ShouldThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setTargetAmount(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target Amount");
    }

    @Test
    @DisplayName("validateCreate - with zero targetAmount should throw")
    void validateCreate_WithZeroTargetAmount_ShouldThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setTargetAmount(BigDecimal.ZERO);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateCreate - with negative currentAmount should throw")
    void validateCreate_WithNegativeCurrentAmount_ShouldThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setCurrentAmount(BigDecimal.valueOf(-100.00));

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    @DisplayName("validateCreate - with past deadline should throw")
    void validateCreate_WithPastDeadline_ShouldThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setDeadline(LocalDateTime.now().minusDays(1));

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be in the past");
    }

    @Test
    @DisplayName("validateUpdate - with all null fields should not throw")
    void validateUpdate_WithAllNullFields_ShouldNotThrow() {
        FinancialGoalDTO dto = new FinancialGoalDTO();

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUpdate - with valid partial fields should not throw")
    void validateUpdate_WithValidPartialFields_ShouldNotThrow() {
        FinancialGoalDTO dto = new FinancialGoalDTO();
        dto.setName("Updated Goal");
        dto.setTargetAmount(BigDecimal.valueOf(15000.00));

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUpdate - with invalid targetAmount should throw")
    void validateUpdate_WithInvalidTargetAmount_ShouldThrow() {
        FinancialGoalDTO dto = new FinancialGoalDTO();
        dto.setTargetAmount(BigDecimal.ZERO);

        assertThatThrownBy(() -> dto.validateUpdate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateResponse - with null id should throw")
    void validateResponse_WithNullId_ShouldThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setId(null);

        assertThatThrownBy(() -> dto.validateResponse())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID is required");
    }

    @Test
    @DisplayName("validateResponse - with valid data should not throw")
    void validateResponse_WithValidData_ShouldNotThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setId(1L);

        assertThatCode(() -> dto.validateResponse())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponse - with null isActive should default to true")
    void validateResponse_WithNullIsActive_ShouldDefaultToTrue() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setId(1L);
        dto.setIsActive(null);

        assertThatCode(() -> dto.validateResponse())
                .doesNotThrowAnyException();

        // After validation, isActive should be set to true
        assertThatCode(() -> {
            dto.validateResponse();
            assertThat(dto.getIsActive()).isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCreate - with name too long should throw")
    void validateCreate_WithNameTooLong_ShouldThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setName("a".repeat(101));

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("validateCreate - with description too long should throw")
    void validateCreate_WithDescriptionTooLong_ShouldThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setDescription("a".repeat(501));

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("validateCreate - with targetAmount having more than 2 decimal places should throw")
    void validateCreate_WithTargetAmountMoreThanTwoDecimalPlaces_ShouldThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setTargetAmount(BigDecimal.valueOf(10000.123));

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot have more than 2 decimal places");
    }

    @Test
    @DisplayName("validateCreate - with null description should not throw")
    void validateCreate_WithNullDescription_ShouldNotThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setDescription(null);

        assertThatCode(() -> dto.validateCreate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCreate - with null currentAmount should not throw")
    void validateCreate_WithNullCurrentAmount_ShouldNotThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setCurrentAmount(null);

        assertThatCode(() -> dto.validateCreate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCreate - with null deadline should not throw")
    void validateCreate_WithNullDeadline_ShouldNotThrow() {
        FinancialGoalDTO dto = createValidFinancialGoalDTO();
        dto.setDeadline(null);

        assertThatCode(() -> dto.validateCreate())
                .doesNotThrowAnyException();
    }
}
