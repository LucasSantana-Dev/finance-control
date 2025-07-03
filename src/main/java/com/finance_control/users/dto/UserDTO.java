package com.finance_control.users.dto;

import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.users.validation.UserValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseDTO<Long> {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    private Boolean isActive;
    
    /**
     * Validates the DTO for create operations.
     * Ensures all required fields are present and valid.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateCreate() {
        UserValidation.validateEmail(email);
        UserValidation.validatePassword(password);
    }
    
    /**
     * Validates the DTO for update operations.
     * Validates only the fields that are present (not null).
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateUpdate() {
        UserValidation.validateEmailForUpdate(email);
        UserValidation.validatePasswordForUpdate(password);
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
        
        UserValidation.validateEmail(email);
        
        if (isActive == null) {
            isActive = true; // Default to active if not set
        }
    }
} 