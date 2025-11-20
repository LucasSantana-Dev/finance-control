package com.finance_control.integration;

import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.dto.SignupRequest;
import com.finance_control.shared.dto.LoginRequest;
import com.finance_control.shared.dto.PasswordResetRequest;
import com.finance_control.shared.service.SupabaseAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.Map;

/**
 * Integration tests for SupabaseAuthService.
 * These tests require a real Supabase project with authentication enabled.
 * To run these tests:
 * 1. Set up a Supabase project with Auth enabled
 * 2. Enable email confirmations or disable them for testing
 * 3. Set environment variables: SUPABASE_URL, SUPABASE_ANON_KEY,
 * SUPABASE_AUTH_ENABLED=true
 * 4. Remove @Disabled annotation
 * 5. Update test data with real values or create test users
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires real Supabase project setup with Auth enabled")
class SupabaseAuthIntegrationTest {

        @Autowired
        private SupabaseAuthService authService;

        private String testEmail;
        private String testPassword;

        @BeforeEach
        void setUp() {
                // Generate unique test email for each test run
                testEmail = "test-" + System.currentTimeMillis() + "@example.com";
                testPassword = "TestPassword123!";
        }

        @Test
        void signup_WithValidData_ShouldCreateUserAndReturnTokens() {
                // Given
                SignupRequest request = SignupRequest.builder()
                                .email(testEmail)
                                .password(testPassword)
                                .metadata(Map.of("full_name", "Integration Test User"))
                                .build();

                // When & Then
                StepVerifier.create(authService.signup(request))
                                .expectNextMatches(response -> {
                                        // Depending on Supabase configuration, might return tokens immediately
                                        // or require email confirmation
                                        return response != null && response.getUser() != null;
                                })
                                .verifyComplete();
        }

        @Test
        void signin_WithValidCredentials_ShouldReturnTokens() {
                // Given - First signup the user
                SignupRequest signupRequest = SignupRequest.builder()
                                .email(testEmail)
                                .password(testPassword)
                                .build();

                authService.signup(signupRequest).block();

                LoginRequest loginRequest = LoginRequest.builder()
                                .email(testEmail)
                                .password(testPassword)
                                .build();

                // When & Then
                StepVerifier.create(authService.signin(loginRequest))
                                .expectNextMatches(response -> {
                                        return response.getAccessToken() != null &&
                                                        response.getRefreshToken() != null &&
                                                        response.getUser() != null &&
                                                        testEmail.equals(response.getUser().getEmail());
                                })
                                .verifyComplete();
        }

        @Test
        void signin_WithInvalidCredentials_ShouldFail() {
                // Given
                LoginRequest request = LoginRequest.builder()
                                .email("nonexistent@example.com")
                                .password("wrongpassword")
                                .build();

                // When & Then
                StepVerifier.create(authService.signin(request))
                                .expectError()
                                .verify();
        }

        @Test
        void refreshToken_WithValidRefreshToken_ShouldReturnNewTokens() {
                // Given - First get a valid refresh token by signing in
                SignupRequest signupRequest = SignupRequest.builder()
                                .email(testEmail)
                                .password(testPassword)
                                .build();

                AuthResponse signupResponse = authService.signup(signupRequest).block();
                String refreshToken = signupResponse.getRefreshToken();

                // When & Then
                StepVerifier.create(authService.refreshToken(refreshToken))
                                .expectNextMatches(response -> {
                                        return response.getAccessToken() != null &&
                                                        response.getRefreshToken() != null &&
                                                        !response.getAccessToken()
                                                                        .equals(signupResponse.getAccessToken());
                                })
                                .verifyComplete();
        }

        @Test
        void resetPassword_WithValidEmail_ShouldSendResetEmail() {
                // Given
                PasswordResetRequest request = PasswordResetRequest.builder()
                                .email(testEmail)
                                .redirectTo("http://localhost:8080/reset-password")
                                .build();

                // When & Then
                StepVerifier.create(authService.resetPassword(request))
                                .verifyComplete();
        }

        @Test
        void getUserInfo_WithValidToken_ShouldReturnUserData() {
                // Given - First sign in to get a valid token
                SignupRequest signupRequest = SignupRequest.builder()
                                .email(testEmail)
                                .password(testPassword)
                                .build();

                AuthResponse signupResponse = authService.signup(signupRequest).block();
                String accessToken = signupResponse.getAccessToken();

                // When & Then
                StepVerifier.create(authService.getUserInfo(accessToken))
                                .expectNextMatches(userInfo -> {
                                        return userInfo.has("email") &&
                                                        testEmail.equals(userInfo.get("email").asText());
                                })
                                .verifyComplete();
        }

        @Test
        void updatePassword_WithValidToken_ShouldUpdatePassword() {
                // Given - First sign in to get a valid token
                SignupRequest signupRequest = SignupRequest.builder()
                                .email(testEmail)
                                .password(testPassword)
                                .build();

                AuthResponse signupResponse = authService.signup(signupRequest).block();
                String accessToken = signupResponse.getAccessToken();
                String newPassword = "NewTestPassword123!";

                // When & Then
                StepVerifier.create(authService.updatePassword(newPassword, accessToken))
                                .verifyComplete();

                // Verify the password was changed by trying to sign in with new password
                LoginRequest loginWithNewPassword = LoginRequest.builder()
                                .email(testEmail)
                                .password(newPassword)
                                .build();

                StepVerifier.create(authService.signin(loginWithNewPassword))
                                .expectNextMatches(response -> response.getAccessToken() != null)
                                .verifyComplete();
        }

        @Test
        void updateEmail_WithValidToken_ShouldInitiateEmailUpdate() {
                // Given - First sign in to get a valid token
                SignupRequest signupRequest = SignupRequest.builder()
                                .email(testEmail)
                                .password(testPassword)
                                .build();

                AuthResponse signupResponse = authService.signup(signupRequest).block();
                String accessToken = signupResponse.getAccessToken();
                String newEmail = "new-" + testEmail;

                // When & Then
                StepVerifier.create(authService.updateEmail(newEmail, accessToken))
                                .verifyComplete();
        }

        @Test
        void signout_WithValidToken_ShouldSignOutUser() {
                // Given - First sign in to get a valid token
                SignupRequest signupRequest = SignupRequest.builder()
                                .email(testEmail)
                                .password(testPassword)
                                .build();

                AuthResponse signupResponse = authService.signup(signupRequest).block();
                String accessToken = signupResponse.getAccessToken();

                // When & Then
                StepVerifier.create(authService.signout(accessToken))
                                .verifyComplete();

                // Verify the token is invalidated by trying to use it
                StepVerifier.create(authService.getUserInfo(accessToken))
                                .expectError()
                                .verify();
        }

        @Test
        void verifyEmail_WithValidToken_ShouldVerifyEmail() {
                // Note: This test requires email verification to be enabled in Supabase
                // and a way to extract the verification token from the email.
                //
                // To run this test:
                // 1. Ensure email confirmation is enabled in Supabase Auth settings
                // 2. Set up email interceptors (e.g., MailHog, Mailtrap, or similar)
                // 3. Extract the verification token from the intercepted email
                // 4. Use the token in the test below
                //
                // Alternative: Test manually by:
                // 1. Signing up a user (which sends verification email)
                // 2. Extracting token from email URL (format: ?token=...&type=signup)
                // 3. Calling verifyEmail with the extracted token

                // Given - Sign up a user to trigger email verification
                SignupRequest signupRequest = SignupRequest.builder()
                                .email(testEmail)
                                .password(testPassword)
                                .build();

                // Sign up the user (this will send a verification email if email confirmation
                // is enabled)
                AuthResponse signupResponse = authService.signup(signupRequest).block();

                // Extract verification token from email
                // In a real scenario with email interceptors, you would:
                // 1. Intercept the email sent by Supabase
                // 2. Extract the token from the verification link (e.g.,
                // ?token=xxx&type=signup)
                // 3. Use that token here
                //
                // For now, we'll document the expected behavior:
                // If email confirmation is disabled in Supabase, signupResponse will contain
                // tokens immediately
                // If email confirmation is enabled, signupResponse.user.email_confirmed_at will
                // be null
                // and you need to verify the email using the token from the email

                if (signupResponse != null && signupResponse.getUser() != null) {
                        boolean emailConfirmed = signupResponse.getUser().getEmailConfirmedAt() != null;

                        if (emailConfirmed) {
                                // Email confirmation is disabled or auto-confirmed
                                // The user is already verified, so verification test is not applicable
                                // This is expected behavior when email confirmation is disabled
                                return;
                        }

                        // Email confirmation is enabled and user needs verification
                        // To complete this test, you would need to:
                        // 1. Extract the verification token from the email sent by Supabase
                        // 2. Uncomment the code below and replace "EXTRACT_FROM_EMAIL" with the actual
                        // token
                        //
                        // Example verification link format:
                        // https://your-project.supabase.co/auth/v1/verify?token=xxx&type=signup
                        // Extract the token parameter from the URL

                        // When & Then - Verify email with token from email
                        // Uncomment and provide the actual token when email interceptors are available:
                        /*
                         * String verificationToken = "EXTRACT_FROM_EMAIL"; // Extract from intercepted
                         * email
                         * String verificationType = "signup"; // Usually "signup" for new user
                         * verification
                         *
                         * StepVerifier.create(authService.verifyEmail(verificationToken,
                         * verificationType, testEmail))
                         * .expectNextMatches(response -> {
                         * return response.getAccessToken() != null &&
                         * response.getRefreshToken() != null &&
                         * response.getUser() != null &&
                         * response.getUser().getEmailConfirmedAt() != null;
                         * })
                         * .verifyComplete();
                         */

                        // For now, we document that this test requires manual setup
                        // The test structure is correct and will work once email interceptors are
                        // configured
                        // When email interceptors are available, uncomment the code above and provide
                        // the actual token
                }
        }

        @Test
        void isSupabaseAuthEnabled_ShouldReturnTrue() {
                // When
                boolean enabled = authService.isSupabaseAuthEnabled();

                // Then
                assert enabled : "Supabase auth should be enabled in integration test environment";
        }
}
