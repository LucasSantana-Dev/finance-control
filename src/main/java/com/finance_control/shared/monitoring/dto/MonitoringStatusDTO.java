package com.finance_control.shared.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for monitoring status response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringStatusDTO {

    private String database;
    private String cache;
    private String externalServices;
    private String overall;
    private LocalDateTime timestamp;
}
