package com.finance_control.shared.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for health status response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatusDTO {

    private String status;
    private LocalDateTime timestamp;
    private String version;
}
