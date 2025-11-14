package com.finance_control.unit.users.validation;

import com.finance_control.users.validation.UserValidation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("UserValidation Tests")
class UserValidationTest {

    @Test
    @DisplayName("validateEmail - with valid email should not throw")
    void validateEmail_WithValidEmail_ShouldNotThrow() {
        assertThatCode(() -> UserValidation.validateEmail("user@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateEmail - with null should throw")
    void validateEmail_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validateEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    @DisplayName("validateEmail - with empty string should throw")
    void validateEmail_WithEmptyString_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validateEmail(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    @DisplayName("validateEmail - with invalid format should throw")
    void validateEmail_WithInvalidFormat_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validateEmail("invalid-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid format");
    }

    @Test
    @DisplayName("validateEmail - with missing @ should throw")
    void validateEmail_WithMissingAt_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validateEmail("userexample.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid format");
    }

    @Test
    @DisplayName("validateEmail - with missing domain should throw")
    void validateEmail_WithMissingDomain_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validateEmail("user@"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid format");
    }

    @Test
    @DisplayName("validateEmail - with valid complex email should not throw")
    void validateEmail_WithValidComplexEmail_ShouldNotThrow() {
        assertThatCode(() -> UserValidation.validateEmail("user.name+tag@example.co.uk"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateEmailForUpdate - with null should not throw")
    void validateEmailForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> UserValidation.validateEmailForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateEmailForUpdate - with valid email should not throw")
    void validateEmailForUpdate_WithValidEmail_ShouldNotThrow() {
        assertThatCode(() -> UserValidation.validateEmailForUpdate("user@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validatePassword - with valid password should not throw")
    void validatePassword_WithValidPassword_ShouldNotThrow() {
        assertThatCode(() -> UserValidation.validatePassword("ValidPass123"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validatePassword - with null should throw")
    void validatePassword_WithNull_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validatePassword(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password");
    }

    @Test
    @DisplayName("validatePassword - with empty string should throw")
    void validatePassword_WithEmptyString_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validatePassword(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password");
    }

    @Test
    @DisplayName("validatePassword - with too short password should throw")
    void validatePassword_WithTooShortPassword_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validatePassword("Short1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least");
    }

    @Test
    @DisplayName("validatePassword - with password missing lowercase should throw")
    void validatePassword_WithPasswordMissingLowercase_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validatePassword("VALIDPASS123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
    }

    @Test
    @DisplayName("validatePassword - with password missing uppercase should throw")
    void validatePassword_WithPasswordMissingUppercase_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validatePassword("validpass123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uppercase");
    }

    @Test
    @DisplayName("validatePassword - with password missing digit should throw")
    void validatePassword_WithPasswordMissingDigit_ShouldThrow() {
        assertThatThrownBy(() -> UserValidation.validatePassword("ValidPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("digit");
    }

    @Test
    @DisplayName("validatePasswordForUpdate - with null should not throw")
    void validatePasswordForUpdate_WithNull_ShouldNotThrow() {
        assertThatCode(() -> UserValidation.validatePasswordForUpdate(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validatePasswordForUpdate - with valid password should not throw")
    void validatePasswordForUpdate_WithValidPassword_ShouldNotThrow() {
        assertThatCode(() -> UserValidation.validatePasswordForUpdate("ValidPass123"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateEmailUnique - with unique email should not throw")
    void validateEmailUnique_WithUniqueEmail_ShouldNotThrow() {
        Predicate<String> emailExists = email -> false;
        assertThatCode(() -> UserValidation.validateEmailUnique("newuser@example.com", emailExists))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateEmailUnique - with existing email should throw")
    void validateEmailUnique_WithExistingEmail_ShouldThrow() {
        Predicate<String> emailExists = email -> true;
        assertThatThrownBy(() -> UserValidation.validateEmailUnique("existing@example.com", emailExists))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("validateEmailUnique - with null email should throw")
    void validateEmailUnique_WithNullEmail_ShouldThrow() {
        Predicate<String> emailExists = email -> false;
        assertThatThrownBy(() -> UserValidation.validateEmailUnique(null, emailExists))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    @DisplayName("validateEmailUniqueForUpdate - with same email should not throw")
    void validateEmailUniqueForUpdate_WithSameEmail_ShouldNotThrow() {
        Predicate<String> emailExists = email -> false;
        assertThatCode(() -> UserValidation.validateEmailUniqueForUpdate("user@example.com", "user@example.com", emailExists))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateEmailUniqueForUpdate - with different unique email should not throw")
    void validateEmailUniqueForUpdate_WithDifferentUniqueEmail_ShouldNotThrow() {
        Predicate<String> emailExists = email -> false;
        assertThatCode(() -> UserValidation.validateEmailUniqueForUpdate("newuser@example.com", "olduser@example.com", emailExists))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateEmailUniqueForUpdate - with existing email should throw")
    void validateEmailUniqueForUpdate_WithExistingEmail_ShouldThrow() {
        Predicate<String> emailExists = email -> true;
        assertThatThrownBy(() -> UserValidation.validateEmailUniqueForUpdate("existing@example.com", "olduser@example.com", emailExists))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("validateEmailUniqueForUpdate - with null email should not throw")
    void validateEmailUniqueForUpdate_WithNullEmail_ShouldNotThrow() {
        Predicate<String> emailExists = email -> false;
        assertThatCode(() -> UserValidation.validateEmailUniqueForUpdate(null, "olduser@example.com", emailExists))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateEmailUniqueForUpdate - with case-insensitive same email should not throw")
    void validateEmailUniqueForUpdate_WithCaseInsensitiveSameEmail_ShouldNotThrow() {
        Predicate<String> emailExists = email -> false;
        assertThatCode(() -> UserValidation.validateEmailUniqueForUpdate("User@Example.com", "user@example.com", emailExists))
                .doesNotThrowAnyException();
    }
}
