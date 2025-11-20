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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

        when(authService.signup(any(SignupRequest.class))).thenReturn(Mono.just(mockAuthResponse));

        StepVerifier.create(controller.signup(request))
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");
                    assertThat(response.getBody().getUser().getEmail()).isEqualTo("test@example.com");
                    return true;
                })
                .verifyComplete();

        verify(authService).signup(any(SignupRequest.class));
    }

    @Test
    void signin_WithValidRequest_ShouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authService.signin(any(LoginRequest.class))).thenReturn(Mono.just(mockAuthResponse));

        StepVerifier.create(controller.signin(request))
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");
                    return true;
                })
                .verifyComplete();

        verify(authService).signin(any(LoginRequest.class));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnAuthResponse() {
        SupabaseAuthController.RefreshTokenRequest request = new SupabaseAuthController.RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        when(authService.refreshToken("refresh-token")).thenReturn(Mono.just(mockAuthResponse));

        StepVerifier.create(controller.refreshToken(request))
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");
                    return true;
                })
                .verifyComplete();

        verify(authService).refreshToken("refresh-token");
    }

    @Test
    void resetPassword_WithValidEmail_ShouldReturnSuccess() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("test@example.com");

        when(authService.resetPassword(any(PasswordResetRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(controller.resetPassword(request))
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).contains("Password reset email sent successfully");
                    return true;
                })
                .verifyComplete();

        verify(authService).resetPassword(any(PasswordResetRequest.class));
    }

    @Test
    void updatePassword_WithValidRequest_ShouldReturnSuccess() {
        SupabaseAuthController.UpdatePasswordRequest request = new SupabaseAuthController.UpdatePasswordRequest();
        request.setNewPassword("newPassword123");

        when(authService.updatePassword("newPassword123", "access-token"))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.updatePassword(request, "Bearer access-token"))
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).contains("Password updated successfully");
                    return true;
                })
                .verifyComplete();

        verify(authService).updatePassword("newPassword123", "access-token");
    }

    @Test
    void updatePassword_WithInvalidHeader_ShouldThrowException() {
        SupabaseAuthController.UpdatePasswordRequest request = new SupabaseAuthController.UpdatePasswordRequest();
        request.setNewPassword("newPassword123");

        assertThatThrownBy(() -> controller.updatePassword(request, "InvalidHeader").block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Authorization header format");
    }

    @Test
    void updateEmail_WithValidRequest_ShouldReturnSuccess() {
        SupabaseAuthController.UpdateEmailRequest request = new SupabaseAuthController.UpdateEmailRequest();
        request.setNewEmail("newemail@example.com");

        when(authService.updateEmail("newemail@example.com", "access-token"))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.updateEmail(request, "Bearer access-token"))
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).contains("Email update initiated successfully");
                    return true;
                })
                .verifyComplete();

        verify(authService).updateEmail("newemail@example.com", "access-token");
    }

    @Test
    void updateEmail_WithInvalidHeader_ShouldThrowException() {
        SupabaseAuthController.UpdateEmailRequest request = new SupabaseAuthController.UpdateEmailRequest();
        request.setNewEmail("newemail@example.com");

        assertThatThrownBy(() -> controller.updateEmail(request, "InvalidHeader").block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Authorization header format");
    }

    @Test
    void signout_WithValidToken_ShouldReturnSuccess() {
        when(authService.signout("access-token")).thenReturn(Mono.empty());

        StepVerifier.create(controller.signout("Bearer access-token"))
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).contains("Sign out successful");
                    return true;
                })
                .verifyComplete();

        verify(authService).signout("access-token");
    }

    @Test
    void signout_WithInvalidHeader_ShouldThrowException() {
        assertThatThrownBy(() -> controller.signout("InvalidHeader").block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Authorization header format");
    }

    @Test
    void verifyEmail_WithValidToken_ShouldReturnAuthResponse() {
        when(authService.verifyEmail("token", "signup", "test@example.com"))
                .thenReturn(Mono.just(mockAuthResponse));

        StepVerifier.create(controller.verifyEmail("token", "signup", "test@example.com"))
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");
                    return true;
                })
                .verifyComplete();

        verify(authService).verifyEmail("token", "signup", "test@example.com");
    }

    @Test
    void getCurrentUser_WithValidToken_ShouldReturnUserInfo() throws Exception {
        JsonNode userInfo = objectMapper.readTree("{\"id\":\"user-uuid\",\"email\":\"test@example.com\"}");

        when(authService.getUserInfo("access-token")).thenReturn(Mono.just(userInfo));

        StepVerifier.create(controller.getCurrentUser("Bearer access-token"))
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isNotNull();
                    return true;
                })
                .verifyComplete();

        verify(authService).getUserInfo("access-token");
    }

    @Test
    void getCurrentUser_WithInvalidHeader_ShouldThrowException() {
        assertThatThrownBy(() -> controller.getCurrentUser("InvalidHeader").block())
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
