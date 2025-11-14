package com.finance_control.unit.users.dto;

import com.finance_control.users.dto.UserDTO;
import com.finance_control.unit.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("UserDTO Validation Tests")
class UserDTOTest extends BaseUnitTest {

    private UserDTO createValidUserDTO() {
        UserDTO dto = new UserDTO();
        dto.setEmail("user@example.com");
        dto.setPassword("ValidPass123");
        return dto;
    }

    @Test
    @DisplayName("validateCreate - with valid data should not throw")
    void validateCreate_WithValidData_ShouldNotThrow() {
        UserDTO dto = createValidUserDTO();

        assertThatCode(() -> dto.validateCreate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCreate - with null email should throw")
    void validateCreate_WithNullEmail_ShouldThrow() {
        UserDTO dto = createValidUserDTO();
        dto.setEmail(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    @DisplayName("validateCreate - with invalid email format should throw")
    void validateCreate_WithInvalidEmailFormat_ShouldThrow() {
        UserDTO dto = createValidUserDTO();
        dto.setEmail("invalid-email");

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid format");
    }

    @Test
    @DisplayName("validateCreate - with null password should throw")
    void validateCreate_WithNullPassword_ShouldThrow() {
        UserDTO dto = createValidUserDTO();
        dto.setPassword(null);

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password");
    }

    @Test
    @DisplayName("validateCreate - with too short password should throw")
    void validateCreate_WithTooShortPassword_ShouldThrow() {
        UserDTO dto = createValidUserDTO();
        dto.setPassword("Short1");

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least");
    }

    @Test
    @DisplayName("validateCreate - with password missing lowercase should throw")
    void validateCreate_WithPasswordMissingLowercase_ShouldThrow() {
        UserDTO dto = createValidUserDTO();
        dto.setPassword("VALIDPASS123");

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
    }

    @Test
    @DisplayName("validateCreate - with password missing uppercase should throw")
    void validateCreate_WithPasswordMissingUppercase_ShouldThrow() {
        UserDTO dto = createValidUserDTO();
        dto.setPassword("validpass123");

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uppercase");
    }

    @Test
    @DisplayName("validateCreate - with password missing digit should throw")
    void validateCreate_WithPasswordMissingDigit_ShouldThrow() {
        UserDTO dto = createValidUserDTO();
        dto.setPassword("ValidPassword");

        assertThatThrownBy(() -> dto.validateCreate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("digit");
    }

    @Test
    @DisplayName("validateUpdate - with all null fields should not throw")
    void validateUpdate_WithAllNullFields_ShouldNotThrow() {
        UserDTO dto = new UserDTO();

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUpdate - with valid email should not throw")
    void validateUpdate_WithValidEmail_ShouldNotThrow() {
        UserDTO dto = new UserDTO();
        dto.setEmail("newemail@example.com");

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUpdate - with invalid email should throw")
    void validateUpdate_WithInvalidEmail_ShouldThrow() {
        UserDTO dto = new UserDTO();
        dto.setEmail("invalid-email");

        assertThatThrownBy(() -> dto.validateUpdate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid format");
    }

    @Test
    @DisplayName("validateUpdate - with valid password should not throw")
    void validateUpdate_WithValidPassword_ShouldNotThrow() {
        UserDTO dto = new UserDTO();
        dto.setPassword("NewPass123");

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUpdate - with invalid password should throw")
    void validateUpdate_WithInvalidPassword_ShouldThrow() {
        UserDTO dto = new UserDTO();
        dto.setPassword("short");

        assertThatThrownBy(() -> dto.validateUpdate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least");
    }

    @Test
    @DisplayName("validateResponse - with null id should throw")
    void validateResponse_WithNullId_ShouldThrow() {
        UserDTO dto = createValidUserDTO();
        dto.setId(null);

        assertThatThrownBy(() -> dto.validateResponse())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID is required");
    }

    @Test
    @DisplayName("validateResponse - with valid data should not throw")
    void validateResponse_WithValidData_ShouldNotThrow() {
        UserDTO dto = createValidUserDTO();
        dto.setId(1L);

        assertThatCode(() -> dto.validateResponse())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponse - with null isActive should default to true")
    void validateResponse_WithNullIsActive_ShouldDefaultToTrue() {
        UserDTO dto = createValidUserDTO();
        dto.setId(1L);
        dto.setIsActive(null);

        assertThatCode(() -> {
            dto.validateResponse();
            assertThat(dto.getIsActive()).isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponse - with false isActive should remain false")
    void validateResponse_WithFalseIsActive_ShouldRemainFalse() {
        UserDTO dto = createValidUserDTO();
        dto.setId(1L);
        dto.setIsActive(false);

        assertThatCode(() -> {
            dto.validateResponse();
            assertThat(dto.getIsActive()).isFalse();
        }).doesNotThrowAnyException();
    }
}
