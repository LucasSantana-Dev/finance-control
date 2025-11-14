package com.finance_control.unit.transactions.dto;

import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.unit.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("TransactionDTO Validation Tests")
class TransactionDTOTest extends BaseUnitTest {

    private TransactionDTO createValidTransactionDTO() {
        TransactionDTO dto = new TransactionDTO();
        dto.setType(TransactionType.INCOME);
        dto.setSubtype(TransactionSubtype.FIXED);
        dto.setSource(TransactionSource.BANK_TRANSACTION);
        dto.setDescription("Valid Transaction");
        dto.setAmount(BigDecimal.valueOf(100.50));
        dto.setCategoryId(1L);
        dto.setUserId(1L);
        dto.setDate(LocalDateTime.now());

        TransactionResponsiblesDTO responsibility = new TransactionResponsiblesDTO();
        responsibility.setResponsibleId(1L);
        responsibility.setPercentage(BigDecimal.valueOf(100.00));
        dto.setResponsibilities(List.of(responsibility));

        return dto;
    }

    @Test
    @DisplayName("validateCreate - with valid data should not throw")
    void validateCreate_WithValidData_ShouldNotThrow() {
        TransactionDTO dto = createValidTransactionDTO();

        assertThatCode(() -> dto.validateCreate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCreate - with null type should throw")
    void validateCreate_WithNullType_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setType(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction Type");
    }

    @Test
    @DisplayName("validateCreate - with null description should throw")
    void validateCreate_WithNullDescription_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setDescription(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description");
    }

    @Test
    @DisplayName("validateCreate - with null amount should throw")
    void validateCreate_WithNullAmount_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setAmount(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount");
    }

    @Test
    @DisplayName("validateCreate - with zero amount should throw")
    void validateCreate_WithZeroAmount_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setAmount(BigDecimal.ZERO);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateCreate - with null categoryId should throw")
    void validateCreate_WithNullCategoryId_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setCategoryId(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category ID");
    }

    @Test
    @DisplayName("validateCreate - with null userId should throw")
    void validateCreate_WithNullUserId_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setUserId(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID");
    }

    @Test
    @DisplayName("validateCreate - with null responsibilities should throw")
    void validateCreate_WithNullResponsibilities_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setResponsibilities(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsibilities");
    }

    @Test
    @DisplayName("validateCreate - with empty responsibilities should throw")
    void validateCreate_WithEmptyResponsibilities_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setResponsibilities(Collections.emptyList());

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsibilities");
    }

    @Test
    @DisplayName("validateUpdate - with all null fields should not throw")
    void validateUpdate_WithAllNullFields_ShouldNotThrow() {
        TransactionDTO dto = new TransactionDTO();

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUpdate - with valid partial fields should not throw")
    void validateUpdate_WithValidPartialFields_ShouldNotThrow() {
        TransactionDTO dto = new TransactionDTO();
        dto.setDescription("Updated Description");
        dto.setAmount(BigDecimal.valueOf(200.00));

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUpdate - with invalid amount should throw")
    void validateUpdate_WithInvalidAmount_ShouldThrow() {
        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(BigDecimal.valueOf(-100.00));

        assertThatThrownBy(() -> dto.validateUpdate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateUpdate - with empty responsibilities should throw")
    void validateUpdate_WithEmptyResponsibilities_ShouldThrow() {
        TransactionDTO dto = new TransactionDTO();
        dto.setResponsibilities(Collections.emptyList());

        assertThatThrownBy(() -> dto.validateUpdate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsibilities");
    }

    @Test
    @DisplayName("validateResponse - with null id should throw")
    void validateResponse_WithNullId_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setId(null);

        assertThatThrownBy(() -> dto.validateResponse())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID is required");
    }

    @Test
    @DisplayName("validateResponse - with valid data should not throw")
    void validateResponse_WithValidData_ShouldNotThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setId(1L);

        assertThatCode(() -> dto.validateResponse())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCreate - with amount having more than 2 decimal places should throw")
    void validateCreate_WithAmountMoreThanTwoDecimalPlaces_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setAmount(BigDecimal.valueOf(100.123));

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot have more than 2 decimal places");
    }

    @Test
    @DisplayName("validateCreate - with description too long should throw")
    void validateCreate_WithDescriptionTooLong_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setDescription("a".repeat(501));

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("validateCreate - with invalid installments should throw")
    void validateCreate_WithInvalidInstallments_ShouldThrow() {
        TransactionDTO dto = createValidTransactionDTO();
        dto.setInstallments(0);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be at least");
    }

    @Test
    @DisplayName("validateUpdate - with null responsibilities should not throw")
    void validateUpdate_WithNullResponsibilities_ShouldNotThrow() {
        TransactionDTO dto = new TransactionDTO();
        dto.setResponsibilities(null);

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }
}
