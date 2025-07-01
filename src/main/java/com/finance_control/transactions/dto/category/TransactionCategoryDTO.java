package com.finance_control.transactions.dto.category;

import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.transactions.validation.TransactionCategoryValidation;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransactionCategoryDTO extends BaseDTO<Long> {
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    /**
     * Validates the DTO for create operations.
     * Ensures all required fields are present and valid.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validateCreate() {
        TransactionCategoryValidation.validateName(name);
    }
    
    /**
     * Validates the DTO for update operations.
     * Validates only the fields that are present (not null).
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validateUpdate() {
        TransactionCategoryValidation.validateNameForUpdate(name);
    }
    
    /**
     * Validates the DTO for response operations.
     * Ensures the DTO is properly populated for API responses.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validateResponse() {
        super.validateResponse(); // Validate common fields (ID)
        TransactionCategoryValidation.validateName(name);
    }
} 