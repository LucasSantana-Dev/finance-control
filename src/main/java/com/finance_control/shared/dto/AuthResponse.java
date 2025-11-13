package com.finance_control.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for Supabase authentication operations.
 * Contains access token, refresh token, user information, and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {

    /**
     * Access token for API authentication
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Refresh token for obtaining new access tokens
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * Token type (usually "bearer")
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * Access token expiration time in seconds
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;

    /**
     * Expiration timestamp of the access token
     */
    @JsonProperty("expires_at")
    private Long expiresAt;

    /**
     * User information
     */
    private User user;

    /**
     * User metadata (for signup responses)
     */
    private Map<String, Object> data;

    /**
     * User information nested in the response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {

        /**
         * Unique user identifier (UUID)
         */
        private String id;

        /**
         * User's email address
         */
        private String email;

        /**
         * Whether the user's email is confirmed
         */
        @JsonProperty("email_confirmed_at")
        private String emailConfirmedAt;

        /**
         * User role in the system
         */
        private String role;

        /**
         * User metadata
         */
        private Map<String, Object> userMetadata;

        /**
         * Application metadata
         */
        private Map<String, Object> appMetadata;

        /**
         * User creation timestamp
         */
        @JsonProperty("created_at")
        private Instant createdAt;

        /**
         * User last update timestamp
         */
        @JsonProperty("updated_at")
        private Instant updatedAt;

        /**
         * Whether the user is confirmed
         */
        private Boolean confirmedAt;
    }
}
