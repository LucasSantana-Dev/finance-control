package com.finance_control.auth.controller;

import com.finance_control.auth.dto.LoginRequest;
import com.finance_control.auth.dto.LoginResponse;
import com.finance_control.auth.service.AuthService;
import com.finance_control.shared.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtils jwtUtils;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Long userId = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        String token = jwtUtils.generateToken(userId);
        
        return ResponseEntity.ok(new LoginResponse(token, userId));
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
} 