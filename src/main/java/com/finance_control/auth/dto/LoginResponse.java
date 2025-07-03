package com.finance_control.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login response.
 * Contains authentication token and user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private Long userId;
    
    /**
     * Validates the login response.
     * Ensures all required fields are present.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
    }
} 