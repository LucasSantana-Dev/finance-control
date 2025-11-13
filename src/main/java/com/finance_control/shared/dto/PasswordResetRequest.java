package com.finance_control.shared.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for password reset operations.
 * Contains user email and optional redirect URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {

    /**
     * User's email address
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Optional redirect URL after password reset
     */
    private String redirectTo;
}
