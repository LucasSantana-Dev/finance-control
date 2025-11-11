package com.finance_control.auth.controller;

import com.finance_control.auth.dto.LoginRequest;
import com.finance_control.auth.dto.LoginResponse;
import com.finance_control.auth.dto.PasswordChangeRequest;
import com.finance_control.auth.service.AuthService;
import com.finance_control.shared.security.JwtUtils;
import com.finance_control.users.dto.UserDTO;
import com.finance_control.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
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
        log.info("Registering new user with email: {}", userDTO.getEmail());
        UserDTO createdUser = userService.create(userDTO);
        log.info("User registered successfully with ID: {}", createdUser.getId());
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT token and return user ID")
    public ResponseEntity<LoginResponse> validateToken(@RequestHeader("Authorization") String authorization) {
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
}
