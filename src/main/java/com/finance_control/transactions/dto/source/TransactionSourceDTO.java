package com.finance_control.transactions.dto.source;

import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.transactions.validation.TransactionSourceValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating, updating, and representing transaction source entities.
 * This class serves all operations, with ID being optional for creation.
 * 
 * <p>For creation: id should be null
 * For updates: id should be populated with the existing source entity ID
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransactionSourceDTO extends BaseDTO<Long> {
    
    @NotBlank(message = "Source name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Source type is required")
    private TransactionSource sourceType;
    
    private String bankName;
    private String accountNumber;
    private String cardType;
    private String cardLastFour;
    private BigDecimal accountBalance;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private Boolean isActive;
    
    /**
     * Validates the DTO for create operations.
     * Ensures all required fields are present and valid.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validateCreate() {
        TransactionSourceValidation.validateName(name);
        TransactionSourceValidation.validateUserId(userId);
    }
    
    /**
     * Validates the DTO for update operations.
     * Validates only the fields that are present (not null).
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validateUpdate() {
        TransactionSourceValidation.validateNameForUpdate(name);
        TransactionSourceValidation.validateUserIdForUpdate(userId);
    }
    
    /**
     * Validates the DTO for response operations.
     * Ensures the DTO is properly populated for API responses.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validateResponse() {
        super.validateResponse(); // Validate common fields (ID)
        TransactionSourceValidation.validateName(name);
        TransactionSourceValidation.validateUserId(userId);
        
        if (isActive == null) {
            isActive = true; // Default to active if not set
        }
    }
} 