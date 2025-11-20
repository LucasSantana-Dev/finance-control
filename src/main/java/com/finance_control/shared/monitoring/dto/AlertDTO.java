package com.finance_control.shared.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for alert information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {

    private String id;
    private String severity;
    private String message;
    private String component;
    private LocalDateTime occurredAt;
    private Boolean resolved;
}
