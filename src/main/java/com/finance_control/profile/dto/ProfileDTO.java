package com.finance_control.profile.dto;

import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.profile.validation.ProfileValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProfileDTO extends BaseDTO<Long> {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;
    
    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;
    
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;
    
    @Size(max = 500, message = "Avatar URL cannot exceed 500 characters")
    private String avatarUrl;
    
    // Computed fields (not stored in database)
    private String currency;
    private String timezone;
    
    /**
     * Validates the DTO for create operations.
     * Ensures all required fields are present and valid.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateCreate() {
        ProfileValidation.validateFullName(fullName);
        ProfileValidation.validateBio(bio);
        ProfileValidation.validatePhone(phone);
        ProfileValidation.validateCountry(country);
        ProfileValidation.validateAvatarUrl(avatarUrl);
    }
    
    /**
     * Validates the DTO for update operations.
     * Validates only the fields that are present (not null).
     * 
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validateUpdate() {
        ProfileValidation.validateFullNameForUpdate(fullName);
        ProfileValidation.validateBioForUpdate(bio);
        ProfileValidation.validatePhoneForUpdate(phone);
        ProfileValidation.validateCountryForUpdate(country);
        ProfileValidation.validateAvatarUrlForUpdate(avatarUrl);
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
        
        ProfileValidation.validateFullName(fullName);
        ProfileValidation.validateBio(bio);
        ProfileValidation.validatePhone(phone);
        ProfileValidation.validateCountry(country);
        ProfileValidation.validateAvatarUrl(avatarUrl);
    }
} 