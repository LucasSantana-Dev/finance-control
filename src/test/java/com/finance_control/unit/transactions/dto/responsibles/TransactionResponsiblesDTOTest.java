package com.finance_control.unit.transactions.dto.responsibles;

import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.unit.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("TransactionResponsiblesDTO Validation Tests")
class TransactionResponsiblesDTOTest extends BaseUnitTest {

    private TransactionResponsiblesDTO createValidTransactionResponsiblesDTO() {
        TransactionResponsiblesDTO dto = new TransactionResponsiblesDTO();
        dto.setName("John Doe");
        dto.setResponsibleId(1L);
        dto.setPercentage(BigDecimal.valueOf(50.00));
        return dto;
    }

    @Test
    @DisplayName("validateCreate - with valid data should not throw")
    void validateCreate_WithValidData_ShouldNotThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();

        assertThatCode(() -> dto.validateCreate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCreate - with null name should throw")
    void validateCreate_WithNullName_ShouldThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setName(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsible Name");
    }

    @Test
    @DisplayName("validateCreate - with null responsibleId should throw")
    void validateCreate_WithNullResponsibleId_ShouldThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setResponsibleId(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsible ID");
    }

    @Test
    @DisplayName("validateCreate - with null percentage should throw")
    void validateCreate_WithNullPercentage_ShouldThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setPercentage(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Percentage");
    }

    @Test
    @DisplayName("validateCreate - with zero percentage should throw")
    void validateCreate_WithZeroPercentage_ShouldThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setPercentage(BigDecimal.ZERO);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateCreate - with negative percentage should throw")
    void validateCreate_WithNegativePercentage_ShouldThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setPercentage(BigDecimal.valueOf(-10.00));

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateCreate - with percentage over 100 should throw")
    void validateCreate_WithPercentageOver100_ShouldThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setPercentage(BigDecimal.valueOf(150.00));

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed 100%");
    }

    @Test
    @DisplayName("validateCreate - with percentage having more than 2 decimal places should throw")
    void validateCreate_WithPercentageMoreThanTwoDecimalPlaces_ShouldThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setPercentage(BigDecimal.valueOf(50.123));

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot have more than 2 decimal places");
    }

    @Test
    @DisplayName("validateUpdate - with all null fields should not throw")
    void validateUpdate_WithAllNullFields_ShouldNotThrow() {
        TransactionResponsiblesDTO dto = new TransactionResponsiblesDTO();

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUpdate - with valid partial fields should not throw")
    void validateUpdate_WithValidPartialFields_ShouldNotThrow() {
        TransactionResponsiblesDTO dto = new TransactionResponsiblesDTO();
        dto.setName("Updated Name");
        dto.setPercentage(BigDecimal.valueOf(75.00));

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUpdate - with invalid percentage should throw")
    void validateUpdate_WithInvalidPercentage_ShouldThrow() {
        TransactionResponsiblesDTO dto = new TransactionResponsiblesDTO();
        dto.setPercentage(BigDecimal.valueOf(150.00));

        assertThatThrownBy(() -> dto.validateUpdate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed 100%");
    }

    @Test
    @DisplayName("validateUpdate - with null percentage should not throw")
    void validateUpdate_WithNullPercentage_ShouldNotThrow() {
        TransactionResponsiblesDTO dto = new TransactionResponsiblesDTO();
        dto.setPercentage(null);

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponse - with null id should throw")
    void validateResponse_WithNullId_ShouldThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setId(null);

        assertThatThrownBy(() -> dto.validateResponse())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID is required");
    }

    @Test
    @DisplayName("validateResponse - with valid data should not throw")
    void validateResponse_WithValidData_ShouldNotThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setId(1L);

        assertThatCode(() -> dto.validateResponse())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCreate - with exactly 100 percent should not throw")
    void validateCreate_WithExactly100Percent_ShouldNotThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setPercentage(BigDecimal.valueOf(100.00));

        assertThatCode(() -> dto.validateCreate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCreate - with exactly 0.01 percent should not throw")
    void validateCreate_WithExactly001Percent_ShouldNotThrow() {
        TransactionResponsiblesDTO dto = createValidTransactionResponsiblesDTO();
        dto.setPercentage(BigDecimal.valueOf(0.01));

        assertThatCode(() -> dto.validateCreate())
                .doesNotThrowAnyException();
    }
}
