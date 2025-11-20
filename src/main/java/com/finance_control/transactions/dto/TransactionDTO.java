package com.finance_control.transactions.dto;

import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.transactions.validation.TransactionValidation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for creating, updating, and representing transactions.
 * This class serves all operations, with ID being optional for creation.
 *
 * <p>For creation: id should be null
 * For updates: id should be populated with the existing transaction ID
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransactionDTO extends BaseDTO<Long> {

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Transaction subtype is required")
    private TransactionSubtype subtype;

    @NotNull(message = "Transaction source is required")
    private TransactionSource source;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private Integer installments;
    private LocalDateTime date;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long subcategoryId;

    private Long sourceEntityId;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String externalReference;

    private String bankReference;

    private Boolean reconciled;

    @NotEmpty(message = "At least one responsible is required")
    @Valid
    private List<TransactionResponsiblesDTO> responsibilities;

    /**
     * Validates common fields for transaction DTOs.
     * Used by both create and response validation.
     */
    private void validateCommonFields() {
        TransactionValidation.validateType(type);
        TransactionValidation.validateSubtype(subtype);
        TransactionValidation.validateSource(source);
        TransactionValidation.validateDescription(description);
        TransactionValidation.validateAmount(amount);
        TransactionValidation.validateInstallments(installments);
        TransactionValidation.validateDate(date);
        TransactionValidation.validateCategoryId(categoryId);
        TransactionValidation.validateSubcategoryId(subcategoryId);
        TransactionValidation.validateSourceEntityId(sourceEntityId);
        TransactionValidation.validateUserId(userId);
        TransactionValidation.validateResponsibilities(responsibilities);
    }

    /**
     * Validates the DTO for create operations.
     * Ensures all required fields are present and valid.
     *
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateCreate() {
        validateCommonFields();
    }

    /**
     * Validates the DTO for update operations.
     * Validates only the fields that are present (not null).
     *
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateUpdate() {
        TransactionValidation.validateTypeForUpdate(type);
        TransactionValidation.validateSubtypeForUpdate(subtype);
        TransactionValidation.validateSourceForUpdate(source);
        TransactionValidation.validateDescriptionForUpdate(description);
        TransactionValidation.validateAmountForUpdate(amount);
        TransactionValidation.validateInstallments(installments);
        TransactionValidation.validateDate(date);
        TransactionValidation.validateCategoryIdForUpdate(categoryId);
        TransactionValidation.validateSubcategoryId(subcategoryId);
        TransactionValidation.validateSourceEntityId(sourceEntityId);
        TransactionValidation.validateUserIdForUpdate(userId);
        TransactionValidation.validateResponsibilitiesForUpdate(responsibilities);
    }

    /**
     * Validates the DTO for response operations.
     * Ensures the DTO is properly populated for API responses.
     *
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateResponse() {
        super.validateResponse(); // Validate common fields (ID)
        validateCommonFields();
    }
}
