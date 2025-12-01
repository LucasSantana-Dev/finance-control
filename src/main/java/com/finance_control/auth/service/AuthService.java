package com.finance_control.auth.service;

import com.finance_control.auth.exception.AuthenticationException;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.monitoring.SentryService;
import com.finance_control.shared.service.EncryptionService;
import com.finance_control.shared.service.SupabaseAuthService;
import com.finance_control.shared.service.UserMappingService;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.dto.LoginRequest;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MetricsService metricsService;
    private final SentryService sentryService;
    private final EncryptionService encryptionService;

    // Optional Supabase authentication provider (now primary)
    @Autowired(required = false)
    private SupabaseAuthService supabaseAuthService;

    @Autowired(required = false)
    private UserMappingService userMappingService;

    /**
     * Checks if Supabase authentication is available and enabled.
     */
    public boolean hasSupabaseAuth() {
        return supabaseAuthService != null && supabaseAuthService.isSupabaseAuthEnabled();
    }

    /**
     * Authenticates a user by email and password.
     *
     * @param email the user's email
     * @param password the user's password
     * @return the user ID if authentication is successful
     * @throws RuntimeException if authentication fails
     */
    public Long authenticate(String email, String password) {
        var sample = metricsService.startAuthenticationTimer();
        try {
            // Use email hash for efficient lookup
            String emailHash = encryptionService.hashEmail(email);
            User user = userRepository.findByEmailHash(emailHash)
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

            if (Boolean.FALSE.equals(user.getIsActive())) {
                throw new AuthenticationException("User account is disabled");
            }

            // Check if user has a password (Supabase users may not have local passwords)
            if (user.getPassword() == null) {
                throw new AuthenticationException("User account uses external authentication. Please use Supabase authentication.");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                sentryService.addBreadcrumb("Authentication failed: invalid password", "auth", io.sentry.SentryLevel.WARNING);
                throw new AuthenticationException("Invalid email or password");
            }

            // Avoid logging PII such as email at INFO level
            log.info("User authenticated successfully");
            return user.getId();
        } finally {
            metricsService.recordAuthenticationTime(sample);
        }
    }

    /**
     * Changes the password for the current authenticated user.
     *
     * @param currentPassword the current password for validation
     * @param newPassword the new password to set
     * @throws AuthenticationException if current password is invalid or user not found
     * @throws IllegalStateException if user ID is not available in context
     * @throws UnsupportedOperationException if user is Supabase-only (no local password)
     */
    public void changePassword(String currentPassword, String newPassword) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User ID not available in context. User must be authenticated.");
        }

        log.debug("Password change requested for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new AuthenticationException("User account is disabled");
        }

        // Check if user has a local password (Supabase-only users don't have local passwords)
        if (user.getPassword() == null) {
            throw new UnsupportedOperationException(
                "User account uses external authentication (Supabase). " +
                "Please change password through Supabase authentication endpoints."
            );
        }

        // Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            sentryService.addBreadcrumb("Password change failed: invalid current password", "auth", io.sentry.SentryLevel.WARNING);
            throw new AuthenticationException("Current password is incorrect");
        }

        // Update password
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", userId);
    }

    /**
     * Authenticates a user using Supabase Auth.
     * This creates or maps a Supabase user to a local user account automatically.
     *
     * @param email the user's email
     * @param password the user's password
     * @return AuthResponse with tokens and mapped user information
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public AuthResponse authenticateWithSupabase(String email, String password) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            throw new IllegalStateException("Supabase authentication is not enabled");
        }

        if (userMappingService == null) {
            throw new IllegalStateException("User mapping service is not available");
        }

        log.debug("Authenticating user with Supabase: {}", email);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        try {
            AuthResponse supabaseResponse = supabaseAuthService.signin(loginRequest);

            // Map Supabase user to local user
            Long localUserId = userMappingService.findOrCreateUserFromSupabase(supabaseResponse);

            // Create enhanced response with local user ID
            AuthResponse enhancedResponse = AuthResponse.builder()
                    .accessToken(supabaseResponse.getAccessToken())
                    .refreshToken(supabaseResponse.getRefreshToken())
                    .tokenType(supabaseResponse.getTokenType())
                    .expiresIn(supabaseResponse.getExpiresIn())
                    .user(supabaseResponse.getUser())
                    .build();

            log.info("User authenticated successfully with Supabase, mapped to local user: {}", localUserId);
            return enhancedResponse;

        } catch (Exception e) {
            log.warn("Supabase authentication failed for user: {} - {}", email, e.getMessage());
            throw new RuntimeException("Failed to authenticate with Supabase", e);
        }
    }

    /**
     * Signs up a new user using Supabase Auth.
     * This creates a user account in Supabase and automatically creates a corresponding local user account.
     *
     * @param email the user's email
     * @param password the user's password
     * @param metadata optional user metadata
     * @return AuthResponse with tokens and user information
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public AuthResponse signupWithSupabase(String email, String password, java.util.Map<String, Object> metadata) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            throw new IllegalStateException("Supabase authentication is not enabled");
        }

        if (userMappingService == null) {
            throw new IllegalStateException("User mapping service is not available");
        }

        log.debug("Signing up user with Supabase: {}", email);

        var signupRequest = com.finance_control.shared.dto.SignupRequest.builder()
                .email(email)
                .password(password)
                .metadata(metadata != null ? metadata : java.util.Map.of())
                .build();

        try {
            AuthResponse supabaseResponse = supabaseAuthService.signup(signupRequest);

            // Create local user account from Supabase response
            Long localUserId = userMappingService.findOrCreateUserFromSupabase(supabaseResponse);

            // Create enhanced response
            AuthResponse enhancedResponse = AuthResponse.builder()
                    .accessToken(supabaseResponse.getAccessToken())
                    .refreshToken(supabaseResponse.getRefreshToken())
                    .tokenType(supabaseResponse.getTokenType())
                    .expiresIn(supabaseResponse.getExpiresIn())
                    .user(supabaseResponse.getUser())
                    .build();

            log.info("User signed up successfully with Supabase, created local user: {}", localUserId);
            return enhancedResponse;

        } catch (Exception e) {
            log.warn("Supabase signup failed for user: {} - {}", email, e.getMessage());
            throw new RuntimeException("Failed to sign up with Supabase", e);
        }
    }

    /**
     * Refreshes a Supabase access token using a refresh token.
     *
     * @param refreshToken the refresh token
     * @return AuthResponse with new tokens
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public AuthResponse refreshSupabaseToken(String refreshToken) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            throw new IllegalStateException("Supabase authentication is not enabled");
        }

        log.debug("Refreshing Supabase token");

        try {
            AuthResponse response = supabaseAuthService.refreshToken(refreshToken);
            log.info("Supabase token refreshed successfully");
            return response;
        } catch (Exception e) {
            log.warn("Supabase token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Failed to refresh Supabase token", e);
        }
    }

    /**
     * Initiates password reset for a user via Supabase.
     *
     * @param email the user's email
     * @param redirectTo optional redirect URL after password reset
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public void resetPasswordWithSupabase(String email, String redirectTo) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            throw new IllegalStateException("Supabase authentication is not enabled");
        }

        log.debug("Initiating password reset with Supabase for: {}", email);

        var resetRequest = com.finance_control.shared.dto.PasswordResetRequest.builder()
                .email(email)
                .redirectTo(redirectTo)
                .build();

        try {
            supabaseAuthService.resetPassword(resetRequest);
            log.info("Password reset email sent via Supabase to: {}", email);
        } catch (Exception e) {
            log.warn("Supabase password reset failed for: {} - {}", email, e.getMessage());
            throw new RuntimeException("Failed to reset password with Supabase", e);
        }
    }

    /**
     * Updates user password using Supabase Auth.
     * Requires a valid access token for authentication.
     *
     * @param newPassword the new password
     * @param accessToken the access token for authentication
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public void updatePasswordWithSupabase(String newPassword, String accessToken) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            throw new IllegalStateException("Supabase authentication is not enabled");
        }

        log.debug("Updating password with Supabase");

        try {
            supabaseAuthService.updatePassword(newPassword, accessToken);
            log.info("Password updated successfully with Supabase");
        } catch (Exception e) {
            log.warn("Supabase password update failed: {}", e.getMessage());
            throw new RuntimeException("Failed to update password with Supabase", e);
        }
    }

    /**
     * Signs out a user from Supabase.
     *
     * @param accessToken the access token for authentication
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public void signoutFromSupabase(String accessToken) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            throw new IllegalStateException("Supabase authentication is not enabled");
        }

        log.debug("Signing out user from Supabase");

        try {
            supabaseAuthService.signout(accessToken);
            log.info("User signed out successfully from Supabase");
        } catch (Exception e) {
            log.warn("Supabase signout failed: {}", e.getMessage());
            throw new RuntimeException("Failed to sign out from Supabase", e);
        }
    }

    /**
     * Checks if Supabase authentication is available and enabled.
     *
     * @return true if Supabase authentication is enabled, false otherwise
     */
    /**
     * Gets current user information from Supabase.
     *
     * @param accessToken The access token for authentication
     * @return user information
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public com.fasterxml.jackson.databind.JsonNode getCurrentSupabaseUser(String accessToken) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            throw new IllegalStateException("Supabase authentication is not enabled");
        }

        log.debug("Getting current user info from Supabase");

        try {
            com.fasterxml.jackson.databind.JsonNode user = supabaseAuthService.getUserInfo(accessToken);
            log.debug("Successfully retrieved user info from Supabase");
            return user;
        } catch (Exception e) {
            log.error("Failed to get user info from Supabase: {}", e.getMessage());
            throw new RuntimeException("Failed to get user info from Supabase", e);
        }
    }

    public boolean isSupabaseAuthEnabled() {
        return supabaseAuthService != null && supabaseAuthService.isSupabaseAuthEnabled();
    }
}
