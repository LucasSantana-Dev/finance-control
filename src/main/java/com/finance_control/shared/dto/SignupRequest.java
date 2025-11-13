package com.finance_control.shared.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for user signup operations.
 * Contains user email, password, and optional metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    /**
     * User's email address
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * User's password
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    /**
     * Optional user metadata (e.g., full name, preferences)
     */
    private Map<String, Object> metadata;

    /**
     * Optional redirect URL after email confirmation
     */
    private String redirectTo;
}
