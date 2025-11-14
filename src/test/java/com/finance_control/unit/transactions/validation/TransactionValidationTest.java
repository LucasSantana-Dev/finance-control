package com.finance_control.unit.transactions.validation;

import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.validation.TransactionValidation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("TransactionValidation Tests")
class TransactionValidationTest {

    @Test
    @DisplayName("validateDescription - with valid description should not throw")
    void validateDescription_WithValidDescription_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateDescription("Valid description"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateDescription - with null should throw")
    void validateDescription_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateDescription(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description");
    }

    @Test
    @DisplayName("validateDescription - with empty string should throw")
    void validateDescription_WithEmptyString_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateDescription(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description");
    }

    @Test
    @DisplayName("validateDescription - with too long description should throw")
    void validateDescription_WithTooLongDescription_ShouldThrow() {
        String longDescription = "a".repeat(501);
        assertThatThrownBy(() -> TransactionValidation.validateDescription(longDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("validateDescriptionForUpdate - with null should not throw")
    void validateDescriptionForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateDescriptionForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateDescriptionForUpdate - with valid description should not throw")
    void validateDescriptionForUpdate_WithValidDescription_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateDescriptionForUpdate("Valid description"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateAmount - with valid positive amount should not throw")
    void validateAmount_WithValidPositiveAmount_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateAmount(BigDecimal.valueOf(100.50)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateAmount - with null should throw")
    void validateAmount_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateAmount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount");
    }

    @Test
    @DisplayName("validateAmount - with zero should throw")
    void validateAmount_WithZero_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateAmount(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateAmount - with negative amount should throw")
    void validateAmount_WithNegativeAmount_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateAmount(BigDecimal.valueOf(-10.00)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("validateAmount - with more than 2 decimal places should throw")
    void validateAmount_WithMoreThanTwoDecimalPlaces_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateAmount(BigDecimal.valueOf(100.123)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot have more than 2 decimal places");
    }

    @Test
    @DisplayName("validateAmountForUpdate - with null should not throw")
    void validateAmountForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateAmountForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateAmountForUpdate - with valid amount should not throw")
    void validateAmountForUpdate_WithValidAmount_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateAmountForUpdate(BigDecimal.valueOf(100.50)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateInstallments - with null should not throw")
    void validateInstallments_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateInstallments(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateInstallments - with valid installments should not throw")
    void validateInstallments_WithValidInstallments_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateInstallments(12))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateInstallments - with zero should throw")
    void validateInstallments_WithZero_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateInstallments(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be at least");
    }

    @Test
    @DisplayName("validateInstallments - with negative should throw")
    void validateInstallments_WithNegative_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateInstallments(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be at least");
    }

    @Test
    @DisplayName("validateDate - with null should not throw")
    void validateDate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateDate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateDate - with valid date should not throw")
    void validateDate_WithValidDate_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateDate(LocalDateTime.now()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCategoryId - with valid ID should not throw")
    void validateCategoryId_WithValidId_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateCategoryId(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCategoryId - with null should throw")
    void validateCategoryId_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateCategoryId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category ID");
    }

    @Test
    @DisplayName("validateCategoryIdForUpdate - with null should not throw")
    void validateCategoryIdForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateCategoryIdForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateSubcategoryId - with null should not throw")
    void validateSubcategoryId_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateSubcategoryId(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateSubcategoryId - with valid ID should not throw")
    void validateSubcategoryId_WithValidId_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateSubcategoryId(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateSourceEntityId - with null should not throw")
    void validateSourceEntityId_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateSourceEntityId(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateSourceEntityId - with valid ID should not throw")
    void validateSourceEntityId_WithValidId_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateSourceEntityId(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUserId - with valid ID should not throw")
    void validateUserId_WithValidId_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateUserId(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUserId - with null should throw")
    void validateUserId_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateUserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID");
    }

    @Test
    @DisplayName("validateUserIdForUpdate - with null should not throw")
    void validateUserIdForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateUserIdForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponsibilities - with valid list should not throw")
    void validateResponsibilities_WithValidList_ShouldNotThrow() {
        List<TransactionResponsiblesDTO> responsibilities = List.of(new TransactionResponsiblesDTO());
        assertThatCode(() -> TransactionValidation.validateResponsibilities(responsibilities))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponsibilities - with null should throw")
    void validateResponsibilities_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateResponsibilities(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsibilities");
    }

    @Test
    @DisplayName("validateResponsibilities - with empty list should throw")
    void validateResponsibilities_WithEmptyList_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateResponsibilities(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsibilities");
    }

    @Test
    @DisplayName("validateResponsibilitiesForUpdate - with null should not throw")
    void validateResponsibilitiesForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateResponsibilitiesForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponsibilitiesForUpdate - with valid list should not throw")
    void validateResponsibilitiesForUpdate_WithValidList_ShouldNotThrow() {
        List<TransactionResponsiblesDTO> responsibilities = List.of(new TransactionResponsiblesDTO());
        assertThatCode(() -> TransactionValidation.validateResponsibilitiesForUpdate(responsibilities))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponsibilitiesForUpdate - with empty list should throw")
    void validateResponsibilitiesForUpdate_WithEmptyList_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateResponsibilitiesForUpdate(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsibilities");
    }

    @Test
    @DisplayName("validateType - with valid type should not throw")
    void validateType_WithValidType_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateType(TransactionType.INCOME))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateType - with null should throw")
    void validateType_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateType(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction Type");
    }

    @Test
    @DisplayName("validateTypeForUpdate - with null should not throw")
    void validateTypeForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateTypeForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateSubtype - with valid subtype should not throw")
    void validateSubtype_WithValidSubtype_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateSubtype(TransactionSubtype.FIXED))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateSubtype - with null should throw")
    void validateSubtype_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateSubtype(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction Subtype");
    }

    @Test
    @DisplayName("validateSubtypeForUpdate - with null should not throw")
    void validateSubtypeForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateSubtypeForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateSource - with valid source should not throw")
    void validateSource_WithValidSource_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateSource(TransactionSource.BANK_TRANSACTION))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateSource - with null should throw")
    void validateSource_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> TransactionValidation.validateSource(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction Source");
    }

    @Test
    @DisplayName("validateSourceForUpdate - with null should not throw")
    void validateSourceForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> TransactionValidation.validateSourceForUpdate(null))
                .doesNotThrowAnyException();
    }
}
