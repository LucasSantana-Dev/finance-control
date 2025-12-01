package com.finance_control.unit.shared.controller;

import com.finance_control.shared.controller.SupabaseAuthController;
import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.dto.LoginRequest;
import com.finance_control.shared.dto.PasswordResetRequest;
import com.finance_control.shared.dto.SignupRequest;
import com.finance_control.shared.service.SupabaseAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupabaseAuthControllerTest {

    @Mock
    private SupabaseAuthService authService;

    @InjectMocks
    private SupabaseAuthController controller;

    private ObjectMapper objectMapper;
    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockAuthResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("bearer")
                .expiresIn(3600)
                .user(AuthResponse.User.builder()
                        .id("user-uuid")
                        .email("test@example.com")
                        .build())
                .build();
    }

    @Test
    void signup_WithValidRequest_ShouldReturnAuthResponse() {
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setMetadata(Map.of("key", "value"));

        when(authService.signup(any(SignupRequest.class))).thenReturn(mockAuthResponse);

        ResponseEntity<AuthResponse> response = controller.signup(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");
        assertThat(response.getBody().getUser().getEmail()).isEqualTo("test@example.com");

        verify(authService).signup(any(SignupRequest.class));
    }

    @Test
    void signin_WithValidRequest_ShouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authService.signin(any(LoginRequest.class))).thenReturn(mockAuthResponse);

        ResponseEntity<AuthResponse> response = controller.signin(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");

        verify(authService).signin(any(LoginRequest.class));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnAuthResponse() {
        SupabaseAuthController.RefreshTokenRequest request = new SupabaseAuthController.RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        when(authService.refreshToken("refresh-token")).thenReturn(mockAuthResponse);

        ResponseEntity<AuthResponse> response = controller.refreshToken(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");

        verify(authService).refreshToken("refresh-token");
    }

    @Test
    void resetPassword_WithValidEmail_ShouldReturnSuccess() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("test@example.com");

        doNothing().when(authService).resetPassword(any(PasswordResetRequest.class));

        ResponseEntity<String> response = controller.resetPassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Password reset email sent successfully");

        verify(authService).resetPassword(any(PasswordResetRequest.class));
    }

    @Test
    void updatePassword_WithValidRequest_ShouldReturnSuccess() {
        SupabaseAuthController.UpdatePasswordRequest request = new SupabaseAuthController.UpdatePasswordRequest();
        request.setNewPassword("newPassword123");

        doNothing().when(authService).updatePassword("newPassword123", "access-token");

        ResponseEntity<String> response = controller.updatePassword(request, "Bearer access-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Password updated successfully");

        verify(authService).updatePassword("newPassword123", "access-token");
    }

    @Test
    void updatePassword_WithInvalidHeader_ShouldThrowException() {
        SupabaseAuthController.UpdatePasswordRequest request = new SupabaseAuthController.UpdatePasswordRequest();
        request.setNewPassword("newPassword123");

        assertThatThrownBy(() -> controller.updatePassword(request, "InvalidHeader"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Authorization header format");
    }

    @Test
    void updateEmail_WithValidRequest_ShouldReturnSuccess() {
        SupabaseAuthController.UpdateEmailRequest request = new SupabaseAuthController.UpdateEmailRequest();
        request.setNewEmail("newemail@example.com");

        doNothing().when(authService).updateEmail("newemail@example.com", "access-token");

        ResponseEntity<String> response = controller.updateEmail(request, "Bearer access-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Email update initiated successfully");

        verify(authService).updateEmail("newemail@example.com", "access-token");
    }

    @Test
    void updateEmail_WithInvalidHeader_ShouldThrowException() {
        SupabaseAuthController.UpdateEmailRequest request = new SupabaseAuthController.UpdateEmailRequest();
        request.setNewEmail("newemail@example.com");

        assertThatThrownBy(() -> controller.updateEmail(request, "InvalidHeader"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Authorization header format");
    }

    @Test
    void signout_WithValidToken_ShouldReturnSuccess() {
        doNothing().when(authService).signout("access-token");

        ResponseEntity<String> response = controller.signout("Bearer access-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Sign out successful");

        verify(authService).signout("access-token");
    }

    @Test
    void signout_WithInvalidHeader_ShouldThrowException() {
        assertThatThrownBy(() -> controller.signout("InvalidHeader"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Authorization header format");
    }

    @Test
    void verifyEmail_WithValidToken_ShouldReturnAuthResponse() {
        when(authService.verifyEmail("token", "signup", "test@example.com"))
                .thenReturn(mockAuthResponse);

        ResponseEntity<AuthResponse> response = controller.verifyEmail("token", "signup", "test@example.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");

        verify(authService).verifyEmail("token", "signup", "test@example.com");
    }

    @Test
    void getCurrentUser_WithValidToken_ShouldReturnUserInfo() throws Exception {
        JsonNode userInfo = objectMapper.readTree("{\"id\":\"user-uuid\",\"email\":\"test@example.com\"}");

        when(authService.getUserInfo("access-token")).thenReturn(userInfo);

        ResponseEntity<Object> response = controller.getCurrentUser("Bearer access-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(authService).getUserInfo("access-token");
    }

    @Test
    void getCurrentUser_WithInvalidHeader_ShouldThrowException() {
        assertThatThrownBy(() -> controller.getCurrentUser("InvalidHeader"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Authorization header format");
    }


    @Test
    void refreshTokenRequest_ShouldSetAndGetRefreshToken() {
        SupabaseAuthController.RefreshTokenRequest request = new SupabaseAuthController.RefreshTokenRequest();
        request.setRefreshToken("refresh-token");
        assertThat(request.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void updatePasswordRequest_ShouldSetAndGetNewPassword() {
        SupabaseAuthController.UpdatePasswordRequest request = new SupabaseAuthController.UpdatePasswordRequest();
        request.setNewPassword("newPassword123");
        assertThat(request.getNewPassword()).isEqualTo("newPassword123");
    }

    @Test
    void updateEmailRequest_ShouldSetAndGetNewEmail() {
        SupabaseAuthController.UpdateEmailRequest request = new SupabaseAuthController.UpdateEmailRequest();
        request.setNewEmail("newemail@example.com");
        assertThat(request.getNewEmail()).isEqualTo("newemail@example.com");
    }
}
