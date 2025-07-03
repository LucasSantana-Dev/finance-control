package com.finance_control.profile.validation;

import com.finance_control.shared.util.ValidationUtils;

/**
 * Validation utility class for Profile entities and DTOs.
 * Provides validation methods for profile-related operations.
 */
public final class ProfileValidation {
    
    private ProfileValidation() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates full name for create operations.
     * 
     * @param fullName the full name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateFullName(String fullName) {
        ValidationUtils.validateRequired(fullName, "Full name");
        ValidationUtils.validateLength(fullName, 2, 100, "Full name");
    }
    
    /**
     * Validates full name for update operations.
     * 
     * @param fullName the full name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateFullNameForUpdate(String fullName) {
        if (fullName != null) {
            ValidationUtils.validateLength(fullName, 2, 100, "Full name");
        }
    }
    
    /**
     * Validates bio for create operations.
     * 
     * @param bio the bio to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateBio(String bio) {
        if (bio != null) {
            ValidationUtils.validateLength(bio, 0, 500, "Bio");
        }
    }
    
    /**
     * Validates bio for update operations.
     * 
     * @param bio the bio to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateBioForUpdate(String bio) {
        if (bio != null) {
            ValidationUtils.validateLength(bio, 0, 500, "Bio");
        }
    }
    
    /**
     * Validates phone for create operations.
     * 
     * @param phone the phone to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validatePhone(String phone) {
        if (phone != null) {
            ValidationUtils.validateLength(phone, 0, 20, "Phone");
        }
    }
    
    /**
     * Validates phone for update operations.
     * 
     * @param phone the phone to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validatePhoneForUpdate(String phone) {
        if (phone != null) {
            ValidationUtils.validateLength(phone, 0, 20, "Phone");
        }
    }
    
    /**
     * Validates country for create operations.
     * 
     * @param country the country to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateCountry(String country) {
        if (country != null) {
            ValidationUtils.validateLength(country, 0, 100, "Country");
        }
    }
    
    /**
     * Validates country for update operations.
     * 
     * @param country the country to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateCountryForUpdate(String country) {
        if (country != null) {
            ValidationUtils.validateLength(country, 0, 100, "Country");
        }
    }
    
    /**
     * Validates avatar URL for create operations.
     * 
     * @param avatarUrl the avatar URL to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateAvatarUrl(String avatarUrl) {
        if (avatarUrl != null) {
            ValidationUtils.validateLength(avatarUrl, 0, 500, "Avatar URL");
            ValidationUtils.validateUrl(avatarUrl, "Avatar URL");
        }
    }
    
    /**
     * Validates avatar URL for update operations.
     * 
     * @param avatarUrl the avatar URL to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateAvatarUrlForUpdate(String avatarUrl) {
        if (avatarUrl != null) {
            ValidationUtils.validateLength(avatarUrl, 0, 500, "Avatar URL");
            ValidationUtils.validateUrl(avatarUrl, "Avatar URL");
        }
    }
} 