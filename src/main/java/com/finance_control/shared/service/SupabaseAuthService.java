package com.finance_control.shared.service;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.dto.SignupRequest;
import com.finance_control.shared.dto.LoginRequest;
import com.finance_control.shared.dto.PasswordResetRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Service for handling Supabase Authentication operations.
 * Provides methods for user signup, login, password reset, and token refresh.
 * Uses Supabase REST API endpoints for authentication operations.
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "app.supabase.enabled", havingValue = "true", matchIfMissing = false)
public class SupabaseAuthService {

    @Qualifier("supabaseWebClient")
    private final WebClient webClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    private static final String AUTH_SIGNUP = "/auth/v1/signup";
    private static final String AUTH_SIGNIN = "/auth/v1/token?grant_type=password";
    private static final String AUTH_REFRESH = "/auth/v1/token?grant_type=refresh_token";
    private static final String AUTH_RECOVER = "/auth/v1/recover";
    private static final String AUTH_UPDATE = "/auth/v1/user";
    private static final String AUTH_LOGOUT = "/auth/v1/logout";
    private static final String AUTH_VERIFY = "/auth/v1/verify";

    public SupabaseAuthService(@Qualifier("supabaseWebClient") WebClient webClient,
                               AppProperties appProperties,
                               ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.appProperties = appProperties;
        ObjectMapper configuredMapper = objectMapper.copy();
        configuredMapper.findAndRegisterModules();
        this.objectMapper = configuredMapper;
    }

    /**
     * Signs up a new user with email and password.
     *
     * @param request Signup request containing email, password, and optional metadata
     * @return AuthResponse containing access token, refresh token, and user info
     */
    public Mono<AuthResponse> signup(SignupRequest request) {
        log.debug("Attempting Supabase signup for email: {}", request.getEmail());

        Map<String, Object> signupData = Map.of(
            "email", request.getEmail(),
            "password", request.getPassword(),
            "data", request.getMetadata() != null ? request.getMetadata() : Map.of()
        );

        return webClient.post()
                .uri(AUTH_SIGNUP)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupData)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::mapToAuthResponse)
                .doOnSuccess(response -> log.info("Supabase signup successful for email: {}", request.getEmail()))
                .doOnError(error -> log.error("Supabase signup failed for email: {} - {}", request.getEmail(), error.getMessage()))
                .onErrorResume(WebClientResponseException.class, this::handleAuthError);
    }

    /**
     * Signs in an existing user with email and password.
     *
     * @param request Login request containing email and password
     * @return AuthResponse containing access token, refresh token, and user info
     */
    public Mono<AuthResponse> signin(LoginRequest request) {
        log.debug("Attempting Supabase signin for email: {}", request.getEmail());

        Map<String, Object> signinData = Map.of(
            "email", request.getEmail(),
            "password", request.getPassword()
        );

        return webClient.post()
                .uri(AUTH_SIGNIN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signinData)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::mapToAuthResponse)
                .doOnSuccess(response -> log.info("Supabase signin successful for email: {}", request.getEmail()))
                .doOnError(error -> log.error("Supabase signin failed for email: {} - {}", request.getEmail(), error.getMessage()))
                .onErrorResume(WebClientResponseException.class, this::handleAuthError);
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken The refresh token
     * @return AuthResponse containing new access token and refresh token
     */
    public Mono<AuthResponse> refreshToken(String refreshToken) {
        log.debug("Attempting token refresh");

        Map<String, Object> refreshData = Map.of(
            "refresh_token", refreshToken
        );

        return webClient.post()
                .uri(AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshData)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::mapToAuthResponse)
                .doOnSuccess(response -> log.info("Token refresh successful"))
                .doOnError(error -> log.error("Token refresh failed: {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class, this::handleAuthError);
    }

    /**
     * Initiates password recovery for a user.
     *
     * @param request Password reset request containing email
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> resetPassword(PasswordResetRequest request) {
        log.debug("Initiating password reset for email: {}", request.getEmail());

        Map<String, Object> resetData = Map.of(
            "email", request.getEmail(),
            "redirect_to", request.getRedirectTo() != null ? request.getRedirectTo() : getDefaultRedirectUrl()
        );

        return webClient.post()
                .uri(AUTH_RECOVER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(resetData)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Password reset email sent to: {}", request.getEmail()))
                .doOnError(error -> log.error("Password reset failed for email: {} - {}", request.getEmail(), error.getMessage()))
                .onErrorResume(WebClientResponseException.class, this::handleAuthError);
    }

    /**
     * Updates user password. Requires valid access token.
     *
     * @param newPassword The new password
     * @param accessToken The access token for authentication
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> updatePassword(String newPassword, String accessToken) {
        log.debug("Attempting password update");

        Map<String, Object> updateData = Map.of(
            "password", newPassword
        );

        return webClient.put()
                .uri(AUTH_UPDATE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateData)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Password update successful"))
                .doOnError(error -> log.error("Password update failed: {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class, this::handleAuthError);
    }

    /**
     * Updates user email. Requires valid access token.
     *
     * @param newEmail The new email address
     * @param accessToken The access token for authentication
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> updateEmail(String newEmail, String accessToken) {
        log.debug("Attempting email update to: {}", newEmail);

        Map<String, Object> updateData = Map.of(
            "email", newEmail
        );

        return webClient.put()
                .uri(AUTH_UPDATE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateData)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Email update successful"))
                .doOnError(error -> log.error("Email update failed: {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class, this::handleAuthError);
    }

    /**
     * Signs out the current user by invalidating their session.
     *
     * @param accessToken The access token for authentication
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> signout(String accessToken) {
        log.debug("Attempting user signout");

        return webClient.post()
                .uri(AUTH_LOGOUT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("User signout successful"))
                .doOnError(error -> log.error("User signout failed: {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class, this::handleAuthError);
    }

    /**
     * Verifies a user's email using a verification token.
     *
     * @param token The verification token
     * @param type The verification type (signup, recovery, invite)
     * @param email The email address being verified
     * @return AuthResponse containing tokens if verification successful
     */
    public Mono<AuthResponse> verifyEmail(String token, String type, String email) {
        log.debug("Attempting email verification for: {}", email);

        Map<String, Object> verifyData = Map.of(
            "token", token,
            "type", type,
            "email", email
        );

        return webClient.post()
                .uri(AUTH_VERIFY)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(verifyData)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::mapToAuthResponse)
                .doOnSuccess(response -> log.info("Email verification successful for: {}", email))
                .doOnError(error -> log.error("Email verification failed for: {} - {}", email, error.getMessage()))
                .onErrorResume(WebClientResponseException.class, this::handleAuthError);
    }

    /**
     * Gets user information from Supabase using access token.
     *
     * @param accessToken The access token
     * @return JsonNode containing user information
     */
    public Mono<JsonNode> getUserInfo(String accessToken) {
        log.debug("Fetching user info from Supabase");

        return webClient.get()
                .uri(AUTH_UPDATE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnSuccess(user -> log.debug("User info retrieved successfully"))
                .doOnError(error -> log.error("Failed to get user info: {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class, this::handleAuthError);
    }

    /**
     * Maps Supabase JSON response to AuthResponse DTO.
     */
    private AuthResponse mapToAuthResponse(JsonNode jsonNode) {
        try {
            return objectMapper.treeToValue(jsonNode, AuthResponse.class);
        } catch (Exception e) {
            log.error("Failed to map Supabase response to AuthResponse: {}", e.getMessage());
            throw new RuntimeException("Failed to parse authentication response", e);
        }
    }

    /**
     * Handles authentication errors from Supabase API.
     */
    private <T> Mono<T> handleAuthError(WebClientResponseException ex) {
        log.warn("Supabase auth error - Status: {}, Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());

        // You can create custom exceptions based on the error response
        // For now, we'll just propagate the error
        return Mono.error(ex);
    }

    /**
     * Gets the default redirect URL for password reset emails.
     */
    private String getDefaultRedirectUrl() {
        // This could be configured in application properties
        return appProperties.supabase().url() + "/reset-password";
    }

    /**
     * Checks if Supabase auth is enabled and configured.
     */
    public boolean isSupabaseAuthEnabled() {
        return appProperties.supabase().enabled() &&
               appProperties.supabase().url() != null &&
               !appProperties.supabase().url().isEmpty() &&
               appProperties.supabase().anonKey() != null &&
               !appProperties.supabase().anonKey().isEmpty();
    }
}
