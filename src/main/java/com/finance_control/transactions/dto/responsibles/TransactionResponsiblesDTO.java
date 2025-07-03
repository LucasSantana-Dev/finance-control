package com.finance_control.transactions.dto.responsibles;

import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.shared.validation.BaseValidation;
import com.finance_control.transactions.validation.TransactionResponsiblesValidation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Comprehensive DTO for transaction responsible entities and their relationships.
 * This class can represent both:
 * 1. A responsible person/entity (when used for CRUD operations on responsible entities)
 * 2. A transaction-responsible relationship (when used for assigning responsibilities to transactions)
 * 
 * <p>For responsible entity operations: only name is required
 * For transaction-responsible relationships: responsibleId and percentage are required
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransactionResponsiblesDTO extends BaseDTO<Long> {
    
    private static final String RESPONSIBLE_ID_FIELD = "Responsible ID";
    
    // Responsible entity fields
    @NotBlank(message = "Responsible name is required")
    private String name;
    
    // Transaction-responsible relationship fields
    @NotNull(message = "Responsible ID is required for transaction assignments")
    private Long responsibleId;
    
    @NotNull(message = "Percentage is required for transaction assignments")
    @DecimalMin(value = "0.01", message = "Percentage must be at least 0.01%")
    @DecimalMax(value = "100.00", message = "Percentage cannot exceed 100%")
    private BigDecimal percentage;
    
    private String notes;
    
    // Relationship audit fields (populated for responses, null for create/update)
    private Long transactionId;
    private String responsibleName;
    private BigDecimal calculatedAmount;
    
    /**
     * Validates the DTO for create operations.
     * Ensures all required fields are present and valid.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateCreate() {
        TransactionResponsiblesValidation.validateName(name);
        BaseValidation.validateRequiredId(responsibleId, RESPONSIBLE_ID_FIELD);
        validatePercentage(percentage);
    }
    
    /**
     * Validates the DTO for update operations.
     * Validates only the fields that are present (not null).
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateUpdate() {
        TransactionResponsiblesValidation.validateNameForUpdate(name);
        BaseValidation.validateOptionalId(responsibleId, RESPONSIBLE_ID_FIELD);
        if (percentage != null) {
            validatePercentage(percentage);
        }
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
        TransactionResponsiblesValidation.validateName(name);
        BaseValidation.validateRequiredId(responsibleId, RESPONSIBLE_ID_FIELD);
        validatePercentage(percentage);
    }
    
    /**
     * Validates percentage value.
     * 
     * @param percentage the percentage to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("Percentage cannot be null");
        }
        if (percentage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Percentage must be positive");
        }
        if (percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage cannot exceed 100%");
        }
        if (percentage.scale() > 2) {
            throw new IllegalArgumentException("Percentage cannot have more than 2 decimal places");
        }
    }
} 