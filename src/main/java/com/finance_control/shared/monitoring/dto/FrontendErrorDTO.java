package com.finance_control.shared.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for frontend error submission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontendErrorDTO {

    private String message;
    private String severity;
    private String errorType;
    private String component;
    private String url;
    private LocalDateTime occurredAt;
    private String stackTrace;
    private String environment;
    private String release;
}
