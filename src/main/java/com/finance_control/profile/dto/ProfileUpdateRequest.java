package com.finance_control.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for user profile updates.
 * Contains fields from the user_profiles table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
    
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;
    
    /**
     * Validates the profile update request.
     * Ensures all required fields are present and valid.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (fullName.length() < 2 || fullName.length() > 100) {
            throw new IllegalArgumentException("Full name must be between 2 and 100 characters");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email must be valid");
        }
        if (bio != null && bio.length() > 500) {
            throw new IllegalArgumentException("Bio must not exceed 500 characters");
        }
        if (phone != null && phone.length() > 20) {
            throw new IllegalArgumentException("Phone number must not exceed 20 characters");
        }
        if (country != null && country.length() > 100) {
            throw new IllegalArgumentException("Country must not exceed 100 characters");
        }
        if (avatarUrl != null && avatarUrl.length() > 500) {
            throw new IllegalArgumentException("Avatar URL must not exceed 500 characters");
        }
    }
} 