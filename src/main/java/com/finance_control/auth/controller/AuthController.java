package com.finance_control.auth.controller;

import com.finance_control.auth.dto.LoginRequest;
import com.finance_control.auth.dto.LoginResponse;
import com.finance_control.auth.dto.PasswordChangeRequest;
import com.finance_control.auth.service.AuthService;
import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.dto.PasswordResetRequest;
import com.finance_control.shared.dto.SignupRequest;
import com.finance_control.shared.security.JwtUtils;
import com.finance_control.users.dto.UserDTO;
import com.finance_control.users.service.UserService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Authentication", description = "Authentication endpoints")
@SuppressFBWarnings("EI_EXPOSE_REP2") // False positive: Spring dependency injection is safe
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Long userId = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        String token = jwtUtils.generateToken(userId);

        return ResponseEntity.ok(new LoginResponse(token, userId));
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserDTO userDTO) {
        // Avoid logging email or other PII at INFO level
        log.info("Registering new user");
        UserDTO createdUser = userService.create(userDTO);
        log.info("User registered successfully");
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT token and return user ID")
    public ResponseEntity<LoginResponse> validateToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                return ResponseEntity.ok(new LoginResponse(token, userId));
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Change user password with current password validation")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.debug("PUT request to change password");

        if (request.isPasswordConfirmationInvalid()) {
            log.warn("Password confirmation does not match");
            return ResponseEntity.badRequest().build();
        }

        authService.changePassword(request.getCurrentPassword(), request.getNewPassword());
        log.info("Password changed successfully");
        return ResponseEntity.ok().build();
    }

    // ========== SUPABASE AUTHENTICATION ENDPOINTS ==========

    @PostMapping("/supabase/signup")
    @Operation(summary = "Sign up with Supabase", description = "Create a new user account using Supabase Auth")
    public Mono<ResponseEntity<AuthResponse>> supabaseSignup(
            @Valid @RequestBody SignupRequest request) {
        log.debug("POST /auth/supabase/signup for email: {}", request.getEmail());

        return authService.signupWithSupabase(request.getEmail(), request.getPassword(), request.getMetadata())
                .map(authResponse -> ResponseEntity.ok(authResponse))
                .doOnSuccess(response -> log.info("Supabase signup successful for email: {}", request.getEmail()))
                .doOnError(error -> log.error("Supabase signup failed for email: {} - {}", request.getEmail(), error.getMessage()));
    }

    @PostMapping("/supabase/login")
    @Operation(summary = "Login with Supabase", description = "Authenticate user using Supabase Auth")
    public Mono<ResponseEntity<AuthResponse>> supabaseLogin(
            @Valid @RequestBody LoginRequest request) {
        log.debug("POST /auth/supabase/login for email: {}", request.getEmail());

        return authService.authenticateWithSupabase(request.getEmail(), request.getPassword())
                .map(authResponse -> ResponseEntity.ok(authResponse))
                .doOnSuccess(response -> log.info("Supabase login successful for email: {}", request.getEmail()))
                .doOnError(error -> log.error("Supabase login failed for email: {} - {}", request.getEmail(), error.getMessage()));
    }

    @PostMapping("/supabase/refresh")
    @Operation(summary = "Refresh Supabase token", description = "Obtain new access token using refresh token")
    public Mono<ResponseEntity<AuthResponse>> supabaseRefreshToken(
            @RequestBody RefreshTokenRequest request) {
        log.debug("POST /auth/supabase/refresh");

        return authService.refreshSupabaseToken(request.getRefreshToken())
                .map(authResponse -> ResponseEntity.ok(authResponse))
                .doOnSuccess(response -> log.info("Supabase token refresh successful"))
                .doOnError(error -> log.error("Supabase token refresh failed: {}", error.getMessage()));
    }

    @PostMapping("/supabase/reset-password")
    @Operation(summary = "Reset password with Supabase", description = "Send password reset email via Supabase")
    public Mono<ResponseEntity<String>> supabaseResetPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        log.debug("POST /auth/supabase/reset-password for email: {}", request.getEmail());

        return authService.resetPasswordWithSupabase(request.getEmail(), request.getRedirectTo())
                .then(Mono.just(ResponseEntity.ok("Password reset email sent successfully")))
                .doOnSuccess(response -> log.info("Password reset email sent to: {}", request.getEmail()))
                .doOnError(error -> log.error("Password reset failed for email: {} - {}", request.getEmail(), error.getMessage()));
    }

    @PutMapping("/supabase/password")
    @Operation(summary = "Update password with Supabase", description = "Update authenticated user's password")
    public Mono<ResponseEntity<String>> supabaseUpdatePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UpdatePasswordRequest request) {
        log.debug("PUT /auth/supabase/password");

        String accessToken = extractTokenFromHeader(authorizationHeader);

        return authService.updatePasswordWithSupabase(request.getNewPassword(), accessToken)
                .then(Mono.just(ResponseEntity.ok("Password updated successfully")))
                .doOnSuccess(response -> log.info("Password update successful"))
                .doOnError(error -> log.error("Password update failed: {}", error.getMessage()));
    }

    @PostMapping("/supabase/logout")
    @Operation(summary = "Logout from Supabase", description = "Sign out user and invalidate session")
    public Mono<ResponseEntity<String>> supabaseLogout(
            @RequestHeader("Authorization") String authorizationHeader) {
        log.debug("POST /auth/supabase/logout");

        String accessToken = extractTokenFromHeader(authorizationHeader);

        return authService.signoutFromSupabase(accessToken)
                .then(Mono.just(ResponseEntity.ok("Logout successful")))
                .doOnSuccess(response -> log.info("Supabase logout successful"))
                .doOnError(error -> log.error("Supabase logout failed: {}", error.getMessage()));
    }

    @GetMapping("/supabase/me")
    @Operation(summary = "Get current Supabase user", description = "Retrieve information about authenticated Supabase user")
    public Mono<ResponseEntity<com.fasterxml.jackson.databind.JsonNode>> getSupabaseUser(
            @RequestHeader("Authorization") String authorizationHeader) {
        log.debug("GET /auth/supabase/me");

        String accessToken = extractTokenFromHeader(authorizationHeader);

        return authService.getCurrentSupabaseUser(accessToken)
                .map(userInfo -> ResponseEntity.ok(userInfo))
                .doOnSuccess(response -> log.debug("Supabase user info retrieved successfully"))
                .doOnError(error -> log.error("Failed to get Supabase user info: {}", error.getMessage()));
    }

    /**
     * Extracts JWT token from Authorization header.
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
}
