package com.finance_control.users.validation;

import java.util.function.Predicate;

import com.finance_control.shared.validation.BaseValidation;

/**
 * Validation utilities specific to User domain.
 * Provides reusable validation methods for user-related operations.
 */
public final class UserValidation {

    private static final String EMAIL_FIELD = "Email";
    private static final String FULL_NAME_FIELD = "Full name";
    private static final String PASSWORD_FIELD = "Password";

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_FULL_NAME_LENGTH = 100;

    private UserValidation() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates email format and presence.
     * 
     * @param email the email to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateEmail(String email) {
        BaseValidation.validateRequiredString(email, EMAIL_FIELD);

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email must be in a valid format");
        }
    }

    /**
     * Validates full name format and length.
     * 
     * @param fullName the full name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateFullName(String fullName) {
        BaseValidation.validateRequiredString(fullName, FULL_NAME_FIELD);
        BaseValidation.validateStringMaxLength(fullName, FULL_NAME_FIELD, MAX_FULL_NAME_LENGTH);

        if (!fullName.matches("^[a-zA-ZÀ-ÿ\\s]+$")) {
            throw new IllegalArgumentException("Full name can only contain letters and spaces");
        }
    }

    /**
     * Validates password strength and length.
     * 
     * @param password the password to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validatePassword(String password) {
        BaseValidation.validateRequiredString(password, PASSWORD_FIELD);
        BaseValidation.validateStringMinLength(password, PASSWORD_FIELD, MIN_PASSWORD_LENGTH);

        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one lowercase letter, one uppercase letter, and one digit");
        }
    }

    /**
     * Validates email for update operations (can be null).
     * 
     * @param email the email to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateEmailForUpdate(String email) {
        if (email != null) {
            validateEmail(email);
        }
    }

    /**
     * Validates full name for update operations (can be null).
     * 
     * @param fullName the full name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateFullNameForUpdate(String fullName) {
        if (fullName != null) {
            validateFullName(fullName);
        }
    }

    /**
     * Validates password for update operations (can be null).
     * 
     * @param password the password to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validatePasswordForUpdate(String password) {
        if (password != null) {
            validatePassword(password);
        }
    }

    /**
     * Validates that email is unique in the system.
     * This method should be called with a repository check.
     * 
     * @param email               the email to check
     * @param emailExistsFunction function to check if email exists
     * @throws IllegalArgumentException if email already exists
     */
    public static void validateEmailUnique(String email, Predicate<String> emailExistsFunction) {
        validateEmail(email);

        if (emailExistsFunction.test(email)) {
            throw new IllegalArgumentException("User with this email already exists");
        }
    }

    /**
     * Validates that email is unique for update operations.
     * 
     * @param email               the email to check
     * @param currentEmail        the current email of the user being updated
     * @param emailExistsFunction function to check if email exists
     * @throws IllegalArgumentException if email already exists
     */
    public static void validateEmailUniqueForUpdate(String email, String currentEmail,
            Predicate<String> emailExistsFunction) {
        if (email != null && !email.equalsIgnoreCase(currentEmail)) {
            validateEmail(email);

            if (emailExistsFunction.test(email)) {
                throw new IllegalArgumentException("User with this email already exists");
            }
        }
    }
}