package com.finance_control.shared.controller;

import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.dto.SignupRequest;
import com.finance_control.shared.dto.LoginRequest;
import com.finance_control.shared.dto.PasswordResetRequest;
import com.finance_control.shared.service.SupabaseAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Supabase Authentication operations.
 * Provides endpoints for user signup, login, password reset, and token management.
 * Only loads when app.supabase.enabled=true
 */
@RestController
@RequestMapping("/auth/supabase")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "app.supabase.enabled", havingValue = "true", matchIfMissing = true)
@Tag(name = "Supabase Authentication", description = "Endpoints for Supabase authentication operations")
public class SupabaseAuthController {

    private final SupabaseAuthService authService;

    /**
     * Signs up a new user with Supabase Auth.
     *
     * @param request Signup request containing email, password, and optional metadata
     * @return AuthResponse with tokens and user information
     */
    @PostMapping("/signup")
    @Operation(summary = "Sign up a new user", description = "Creates a new user account using Supabase Auth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User created successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "User already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.debug("POST /auth/supabase/signup for email: {}", request.getEmail());

        try {
            AuthResponse authResponse = authService.signup(request);
            log.info("Supabase signup successful for email: {}", request.getEmail());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Supabase signup failed for email: {} - {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Signs in an existing user with Supabase Auth.
     *
     * @param request Login request containing email and password
     * @return AuthResponse with tokens and user information
     */
    @PostMapping("/signin")
    @Operation(summary = "Sign in user", description = "Authenticates an existing user using Supabase Auth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid credentials"),
        @ApiResponse(responseCode = "401", description = "Authentication failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AuthResponse> signin(@Valid @RequestBody LoginRequest request) {
        log.debug("POST /auth/supabase/signin for email: {}", request.getEmail());

        try {
            AuthResponse authResponse = authService.signin(request);
            log.info("Supabase signin successful for email: {}", request.getEmail());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Supabase signin failed for email: {} - {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken The refresh token in request body
     * @return AuthResponse with new tokens
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Obtains a new access token using a refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
        @ApiResponse(responseCode = "401", description = "Refresh token expired or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.debug("POST /auth/supabase/refresh");

        try {
            AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());
            log.info("Token refresh successful");
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Initiates password reset for a user.
     *
     * @param request Password reset request containing email
     * @return Confirmation message
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Sends a password reset email to the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent"),
        @ApiResponse(responseCode = "400", description = "Invalid email"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.debug("POST /auth/supabase/reset-password for email: {}", request.getEmail());

        try {
            authService.resetPassword(request);
            log.info("Password reset email sent to: {}", request.getEmail());
            return ResponseEntity.ok("Password reset email sent successfully");
        } catch (Exception e) {
            log.error("Password reset failed for email: {} - {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Updates user password. Requires authentication.
     *
     * @param request Password update request
     * @param authorizationHeader Authorization header with Bearer token
     * @return Confirmation message
     */
    @PutMapping("/password")
    @Operation(summary = "Update password", description = "Updates the authenticated user's password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid password"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("PUT /auth/supabase/password");

        String accessToken = extractTokenFromHeader(authorizationHeader);

        try {
            authService.updatePassword(request.getNewPassword(), accessToken);
            log.info("Password update successful");
            return ResponseEntity.ok("Password updated successfully");
        } catch (Exception e) {
            log.error("Password update failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Updates user email. Requires authentication.
     *
     * @param request Email update request
     * @param authorizationHeader Authorization header with Bearer token
     * @return Confirmation message
     */
    @PutMapping("/email")
    @Operation(summary = "Update email", description = "Updates the authenticated user's email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email update initiated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "409", description = "Email already in use"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> updateEmail(
            @Valid @RequestBody UpdateEmailRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("PUT /auth/supabase/email to: {}", request.getNewEmail());

        String accessToken = extractTokenFromHeader(authorizationHeader);

        try {
            authService.updateEmail(request.getNewEmail(), accessToken);
            log.info("Email update initiated for: {}", request.getNewEmail());
            return ResponseEntity.ok("Email update initiated successfully. Please check your email for confirmation.");
        } catch (Exception e) {
            log.error("Email update failed for: {} - {}", request.getNewEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Signs out the current user.
     *
     * @param authorizationHeader Authorization header with Bearer token
     * @return Confirmation message
     */
    @PostMapping("/signout")
    @Operation(summary = "Sign out user", description = "Signs out the current user and invalidates their session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sign out successful"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> signout(@RequestHeader("Authorization") String authorizationHeader) {
        log.debug("POST /auth/supabase/signout");

        String accessToken = extractTokenFromHeader(authorizationHeader);

        try {
            authService.signout(accessToken);
            log.info("User sign out successful");
            return ResponseEntity.ok("Sign out successful");
        } catch (Exception e) {
            log.error("User sign out failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Verifies user email using verification token.
     *
     * @param token Verification token
     * @param type Verification type (signup, recovery, invite)
     * @param email Email address being verified
     * @return AuthResponse with tokens if verification successful
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify email", description = "Verifies user email using verification token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email verified successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid verification token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AuthResponse> verifyEmail(
            @RequestParam String token,
            @RequestParam String type,
            @RequestParam String email) {

        log.debug("POST /auth/supabase/verify for email: {}", email);

        try {
            AuthResponse authResponse = authService.verifyEmail(token, type, email);
            log.info("Email verification successful for: {}", email);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Email verification failed for: {} - {}", email, e.getMessage());
            throw e;
        }
    }

    /**
     * Gets current user information. Requires authentication.
     *
     * @param authorizationHeader Authorization header with Bearer token
     * @return User information
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieves information about the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Object> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        log.debug("GET /auth/supabase/me");

        String accessToken = extractTokenFromHeader(authorizationHeader);

        try {
            Object userInfo = authService.getUserInfo(accessToken);
            log.debug("User info retrieved successfully");
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("Failed to get user info: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts the JWT token from the Authorization header.
     */
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    /**
     * Inner class for refresh token request.
     */
    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    /**
     * Inner class for password update request.
     */
    public static class UpdatePasswordRequest {
        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    /**
     * Inner class for email update request.
     */
    public static class UpdateEmailRequest {
        private String newEmail;

        public String getNewEmail() {
            return newEmail;
        }

        public void setNewEmail(String newEmail) {
            this.newEmail = newEmail;
        }
    }
}
