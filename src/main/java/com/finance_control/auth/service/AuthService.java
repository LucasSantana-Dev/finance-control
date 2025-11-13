package com.finance_control.auth.service;

import com.finance_control.auth.exception.AuthenticationException;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.service.SupabaseAuthService;
import com.finance_control.shared.service.UserMappingService;
import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.dto.LoginRequest;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MetricsService metricsService;

    // Optional Supabase authentication provider
    @Autowired(required = false)
    private SupabaseAuthService supabaseAuthService;

    @Autowired(required = false)
    private UserMappingService userMappingService;

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
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

            if (Boolean.FALSE.equals(user.getIsActive())) {
                throw new AuthenticationException("User account is disabled");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new AuthenticationException("Invalid email or password");
            }

            metricsService.incrementUserLogin();
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
     * @throws AuthenticationException if current password is invalid
     */
    public void changePassword(String currentPassword, String newPassword) {
        // TODO: Get current user from security context
        // For now, this is a placeholder implementation
        log.info("Password change requested");

        // TODO: Implement with proper security context to get current user and validate current password
        throw new UnsupportedOperationException("Password change not yet implemented - requires security context integration");
    }

    /**
     * Authenticates a user using Supabase Auth.
     * This creates or maps a Supabase user to a local user account automatically.
     *
     * @param email the user's email
     * @param password the user's password
     * @return Mono containing AuthResponse with tokens and mapped user information
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public Mono<AuthResponse> authenticateWithSupabase(String email, String password) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            return Mono.error(new IllegalStateException("Supabase authentication is not enabled"));
        }

        if (userMappingService == null) {
            return Mono.error(new IllegalStateException("User mapping service is not available"));
        }

        log.debug("Authenticating user with Supabase: {}", email);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        return supabaseAuthService.signin(loginRequest)
                .flatMap(supabaseResponse -> {
                    try {
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

                        // Store the local user ID in a custom claim or metadata
                        // For now, we'll log it - in a real implementation, you might want to add it to the token

                        metricsService.incrementUserLogin();
                        log.info("User authenticated successfully with Supabase, mapped to local user: {}", localUserId);

                        return Mono.just(enhancedResponse);

                    } catch (Exception e) {
                        log.error("Failed to map Supabase user to local user: {}", e.getMessage(), e);
                        return Mono.error(new RuntimeException("Failed to create or find local user account", e));
                    }
                })
                .doOnError(error -> {
                    log.warn("Supabase authentication failed for user: {} - {}", email, error.getMessage());
                });
    }

    /**
     * Signs up a new user using Supabase Auth.
     * This creates a user account in Supabase and automatically creates a corresponding local user account.
     *
     * @param email the user's email
     * @param password the user's password
     * @param metadata optional user metadata
     * @return Mono containing AuthResponse with tokens and user information
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public Mono<AuthResponse> signupWithSupabase(String email, String password, java.util.Map<String, Object> metadata) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            return Mono.error(new IllegalStateException("Supabase authentication is not enabled"));
        }

        if (userMappingService == null) {
            return Mono.error(new IllegalStateException("User mapping service is not available"));
        }

        log.debug("Signing up user with Supabase: {}", email);

        var signupRequest = com.finance_control.shared.dto.SignupRequest.builder()
                .email(email)
                .password(password)
                .metadata(metadata != null ? metadata : java.util.Map.of())
                .build();

        return supabaseAuthService.signup(signupRequest)
                .flatMap(supabaseResponse -> {
                    try {
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

                        return Mono.just(enhancedResponse);

                    } catch (Exception e) {
                        log.error("Failed to create local user account for Supabase signup: {}", e.getMessage(), e);
                        return Mono.error(new RuntimeException("Failed to create local user account", e));
                    }
                })
                .doOnError(error -> {
                    log.warn("Supabase signup failed for user: {} - {}", email, error.getMessage());
                });
    }

    /**
     * Refreshes a Supabase access token using a refresh token.
     *
     * @param refreshToken the refresh token
     * @return Mono containing AuthResponse with new tokens
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public Mono<AuthResponse> refreshSupabaseToken(String refreshToken) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            return Mono.error(new IllegalStateException("Supabase authentication is not enabled"));
        }

        log.debug("Refreshing Supabase token");

        return supabaseAuthService.refreshToken(refreshToken)
                .doOnSuccess(response -> log.info("Supabase token refreshed successfully"))
                .doOnError(error -> log.warn("Supabase token refresh failed: {}", error.getMessage()));
    }

    /**
     * Initiates password reset for a user via Supabase.
     *
     * @param email the user's email
     * @param redirectTo optional redirect URL after password reset
     * @return Mono<Void> indicating completion
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public Mono<Void> resetPasswordWithSupabase(String email, String redirectTo) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            return Mono.error(new IllegalStateException("Supabase authentication is not enabled"));
        }

        log.debug("Initiating password reset with Supabase for: {}", email);

        var resetRequest = com.finance_control.shared.dto.PasswordResetRequest.builder()
                .email(email)
                .redirectTo(redirectTo)
                .build();

        return supabaseAuthService.resetPassword(resetRequest)
                .doOnSuccess(v -> log.info("Password reset email sent via Supabase to: {}", email))
                .doOnError(error -> log.warn("Supabase password reset failed for: {} - {}", email, error.getMessage()));
    }

    /**
     * Updates user password using Supabase Auth.
     * Requires a valid access token for authentication.
     *
     * @param newPassword the new password
     * @param accessToken the access token for authentication
     * @return Mono<Void> indicating completion
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public Mono<Void> updatePasswordWithSupabase(String newPassword, String accessToken) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            return Mono.error(new IllegalStateException("Supabase authentication is not enabled"));
        }

        log.debug("Updating password with Supabase");

        return supabaseAuthService.updatePassword(newPassword, accessToken)
                .doOnSuccess(v -> log.info("Password updated successfully with Supabase"))
                .doOnError(error -> log.warn("Supabase password update failed: {}", error.getMessage()));
    }

    /**
     * Signs out a user from Supabase.
     *
     * @param accessToken the access token for authentication
     * @return Mono<Void> indicating completion
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public Mono<Void> signoutFromSupabase(String accessToken) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            return Mono.error(new IllegalStateException("Supabase authentication is not enabled"));
        }

        log.debug("Signing out user from Supabase");

        return supabaseAuthService.signout(accessToken)
                .doOnSuccess(v -> log.info("User signed out successfully from Supabase"))
                .doOnError(error -> log.warn("Supabase signout failed: {}", error.getMessage()));
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
     * @return Mono containing user information
     * @throws IllegalStateException if Supabase authentication is not enabled
     */
    public Mono<com.fasterxml.jackson.databind.JsonNode> getCurrentSupabaseUser(String accessToken) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            return Mono.error(new IllegalStateException("Supabase authentication is not enabled"));
        }

        log.debug("Getting current user info from Supabase");

        return supabaseAuthService.getUserInfo(accessToken)
                .doOnSuccess(user -> log.debug("Successfully retrieved user info from Supabase"))
                .doOnError(error -> log.error("Failed to get user info from Supabase: {}", error.getMessage()));
    }

    public boolean isSupabaseAuthEnabled() {
        return supabaseAuthService != null && supabaseAuthService.isSupabaseAuthEnabled();
    }
}
