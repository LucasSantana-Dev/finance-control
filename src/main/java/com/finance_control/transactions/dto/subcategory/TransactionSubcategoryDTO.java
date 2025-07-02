package com.finance_control.transactions.dto.subcategory;

import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.transactions.validation.TransactionSubcategoryValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for creating, updating, and representing transaction subcategories.
 * This class serves all operations, with ID being optional for creation.
 * 
 * <p>For creation: id should be null
 * For updates: id should be populated with the existing subcategory ID
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransactionSubcategoryDTO extends BaseDTO<Long> {
    
    @NotBlank(message = "Subcategory name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    private Boolean isActive;
    private String categoryName;
    
    /**
     * Validates the DTO for create operations.
     * Ensures all required fields are present and valid.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateCreate() {
        TransactionSubcategoryValidation.validateName(name);
        TransactionSubcategoryValidation.validateCategoryId(categoryId);
    }
    
    /**
     * Validates the DTO for update operations.
     * Validates only the fields that are present (not null).
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateUpdate() {
        TransactionSubcategoryValidation.validateNameForUpdate(name);
        TransactionSubcategoryValidation.validateCategoryIdForUpdate(categoryId);
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
        TransactionSubcategoryValidation.validateName(name);
        TransactionSubcategoryValidation.validateCategoryId(categoryId);
        
        if (isActive == null) {
            isActive = true; // Default to active if not set
        }
    }
} 