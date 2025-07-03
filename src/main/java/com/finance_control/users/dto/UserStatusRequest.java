package com.finance_control.users.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for user status updates.
 * Used for activating/deactivating users by administrators.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusRequest {
    
    @NotNull(message = "Active status is required")
    private Boolean active;
    
    private String reason;
} 