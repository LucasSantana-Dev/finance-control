package com.finance_control.auth.controller;

import com.finance_control.auth.dto.LoginRequest;
import com.finance_control.auth.dto.LoginResponse;
import com.finance_control.auth.dto.PasswordChangeRequest;
import com.finance_control.auth.service.AuthService;
import com.finance_control.shared.security.JwtUtils;
import com.finance_control.shared.service.UserMappingService;
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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private UserMappingService userMappingService;

    @PostMapping("/login")
    @Operation(summary = "User login",
            description = "Authenticate user and return JWT token. Uses Supabase Auth if enabled, otherwise uses local authentication.")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Try Supabase Auth first if available
        if (authService.hasSupabaseAuth()) {
            try {
                return authService.authenticateWithSupabase(loginRequest.getEmail(), loginRequest.getPassword())
                        .map(authResponse -> {
                            // Return Supabase auth response
                            LoginResponse response = new LoginResponse(
                                    authResponse.getAccessToken(),
                                    authResponse.getUser() != null ?
                                        userMappingService.findUserIdBySupabaseId(authResponse.getUser().getId()) : null
                            );
                            return ResponseEntity.ok(response);
                        })
                        .block();
            } catch (Exception e) {
                log.debug("Supabase authentication failed, falling back to local auth: {}", e.getMessage());
                // Fall through to local auth
            }
        }

        // Fallback to local authentication
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
    // NOTE: These endpoints have been moved to SupabaseAuthController
    // They are only available when app.supabase.enabled=true
    // When Supabase is disabled, use the standard /auth/login and /auth/register endpoints
}
