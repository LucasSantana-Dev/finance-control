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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Service for handling Supabase Authentication operations.
 * Provides methods for user signup, login, password reset, and token refresh.
 * Uses Supabase REST API endpoints for authentication operations.
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "app.supabase.enabled", havingValue = "true", matchIfMissing = true)
public class SupabaseAuthService {

    @Qualifier("supabaseRestClient")
    private final RestClient restClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    private static final String AUTH_SIGNUP = "/auth/v1/signup";
    private static final String AUTH_SIGNIN = "/auth/v1/token?grant_type=password";
    private static final String AUTH_REFRESH = "/auth/v1/token?grant_type=refresh_token";
    private static final String AUTH_RECOVER = "/auth/v1/recover";
    private static final String AUTH_UPDATE = "/auth/v1/user";
    private static final String AUTH_LOGOUT = "/auth/v1/logout";
    private static final String AUTH_VERIFY = "/auth/v1/verify";

    public SupabaseAuthService(@Qualifier("supabaseRestClient") RestClient restClient,
                               AppProperties appProperties,
                               ObjectMapper objectMapper) {
        this.restClient = restClient;
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
    public AuthResponse signup(SignupRequest request) {
        log.debug("Attempting Supabase signup for email: {}", request.getEmail());

        Map<String, Object> signupData = Map.of(
            "email", request.getEmail(),
            "password", request.getPassword(),
            "data", request.getMetadata() != null ? request.getMetadata() : Map.of()
        );

        try {
            JsonNode response = restClient.post()
                    .uri(AUTH_SIGNUP)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(signupData)
                    .retrieve()
                    .body(JsonNode.class);

            AuthResponse authResponse = mapToAuthResponse(response);
            log.info("Supabase signup successful for email: {}", request.getEmail());
            return authResponse;
        } catch (RestClientException e) {
            log.error("Supabase signup failed for email: {} - {}", request.getEmail(), e.getMessage());
            throw handleAuthError(e);
        }
    }

    /**
     * Signs in an existing user with email and password.
     *
     * @param request Login request containing email and password
     * @return AuthResponse containing access token, refresh token, and user info
     */
    public AuthResponse signin(LoginRequest request) {
        log.debug("Attempting Supabase signin for email: {}", request.getEmail());

        Map<String, Object> signinData = Map.of(
            "email", request.getEmail(),
            "password", request.getPassword()
        );

        try {
            JsonNode response = restClient.post()
                    .uri(AUTH_SIGNIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(signinData)
                    .retrieve()
                    .body(JsonNode.class);

            AuthResponse authResponse = mapToAuthResponse(response);
            log.info("Supabase signin successful for email: {}", request.getEmail());
            return authResponse;
        } catch (RestClientException e) {
            log.error("Supabase signin failed for email: {} - {}", request.getEmail(), e.getMessage());
            throw handleAuthError(e);
        }
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken The refresh token
     * @return AuthResponse containing new access token and refresh token
     */
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Attempting token refresh");

        Map<String, Object> refreshData = Map.of(
            "refresh_token", refreshToken
        );

        try {
            JsonNode response = restClient.post()
                    .uri(AUTH_REFRESH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(refreshData)
                    .retrieve()
                    .body(JsonNode.class);

            AuthResponse authResponse = mapToAuthResponse(response);
            log.info("Token refresh successful");
            return authResponse;
        } catch (RestClientException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw handleAuthError(e);
        }
    }

    /**
     * Initiates password recovery for a user.
     *
     * @param request Password reset request containing email
     */
    public void resetPassword(PasswordResetRequest request) {
        log.debug("Initiating password reset for email: {}", request.getEmail());

        Map<String, Object> resetData = Map.of(
            "email", request.getEmail(),
            "redirect_to", request.getRedirectTo() != null ? request.getRedirectTo() : getDefaultRedirectUrl()
        );

        try {
            restClient.post()
                    .uri(AUTH_RECOVER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resetData)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Password reset email sent to: {}", request.getEmail());
        } catch (RestClientException e) {
            log.error("Password reset failed for email: {} - {}", request.getEmail(), e.getMessage());
            throw handleAuthError(e);
        }
    }

    /**
     * Updates user password. Requires valid access token.
     *
     * @param newPassword The new password
     * @param accessToken The access token for authentication
     */
    public void updatePassword(String newPassword, String accessToken) {
        log.debug("Attempting password update");

        Map<String, Object> updateData = Map.of(
            "password", newPassword
        );

        try {
            restClient.put()
                    .uri(AUTH_UPDATE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(updateData)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Password update successful");
        } catch (RestClientException e) {
            log.error("Password update failed: {}", e.getMessage());
            throw handleAuthError(e);
        }
    }

    /**
     * Updates user email. Requires valid access token.
     *
     * @param newEmail The new email address
     * @param accessToken The access token for authentication
     */
    public void updateEmail(String newEmail, String accessToken) {
        log.debug("Attempting email update to: {}", newEmail);

        Map<String, Object> updateData = Map.of(
            "email", newEmail
        );

        try {
            restClient.put()
                    .uri(AUTH_UPDATE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(updateData)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email update successful");
        } catch (RestClientException e) {
            log.error("Email update failed: {}", e.getMessage());
            throw handleAuthError(e);
        }
    }

    /**
     * Signs out the current user by invalidating their session.
     *
     * @param accessToken The access token for authentication
     */
    public void signout(String accessToken) {
        log.debug("Attempting user signout");

        try {
            restClient.post()
                    .uri(AUTH_LOGOUT)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();

            log.info("User signout successful");
        } catch (RestClientException e) {
            log.error("User signout failed: {}", e.getMessage());
            throw handleAuthError(e);
        }
    }

    /**
     * Verifies a user's email using a verification token.
     *
     * @param token The verification token
     * @param type The verification type (signup, recovery, invite)
     * @param email The email address being verified
     * @return AuthResponse containing tokens if verification successful
     */
    public AuthResponse verifyEmail(String token, String type, String email) {
        log.debug("Attempting email verification for: {}", email);

        Map<String, Object> verifyData = Map.of(
            "token", token,
            "type", type,
            "email", email
        );

        try {
            JsonNode response = restClient.post()
                    .uri(AUTH_VERIFY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(verifyData)
                    .retrieve()
                    .body(JsonNode.class);

            AuthResponse authResponse = mapToAuthResponse(response);
            log.info("Email verification successful for: {}", email);
            return authResponse;
        } catch (RestClientException e) {
            log.error("Email verification failed for: {} - {}", email, e.getMessage());
            throw handleAuthError(e);
        }
    }

    /**
     * Gets user information from Supabase using access token.
     *
     * @param accessToken The access token
     * @return JsonNode containing user information
     */
    public JsonNode getUserInfo(String accessToken) {
        log.debug("Fetching user info from Supabase");

        try {
            JsonNode user = restClient.get()
                    .uri(AUTH_UPDATE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);

            log.debug("User info retrieved successfully");
            return user;
        } catch (RestClientException e) {
            log.error("Failed to get user info: {}", e.getMessage());
            throw handleAuthError(e);
        }
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
    private RuntimeException handleAuthError(RestClientException ex) {
        String errorMessage = ex.getMessage();
        HttpStatusCode statusCode = null;

        if (ex instanceof HttpClientErrorException httpEx) {
            statusCode = httpEx.getStatusCode();
            errorMessage = httpEx.getResponseBodyAsString();
            log.warn("Supabase auth error - Status: {}, Body: {}", statusCode, errorMessage);
        } else if (ex instanceof HttpServerErrorException serverEx) {
            statusCode = serverEx.getStatusCode();
            errorMessage = serverEx.getResponseBodyAsString();
            log.warn("Supabase auth server error - Status: {}, Body: {}", statusCode, errorMessage);
        } else {
            log.warn("Supabase auth error: {}", errorMessage);
        }

        return new RuntimeException("Supabase authentication error: " + errorMessage, ex);
    }

    /**
     * Gets the default redirect URL for password reset emails.
     */
    private String getDefaultRedirectUrl() {
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
