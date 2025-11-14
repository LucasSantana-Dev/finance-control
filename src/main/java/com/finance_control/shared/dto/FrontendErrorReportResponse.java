package com.finance_control.shared.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FrontendErrorReportResponse {
    Long id;
    String status;
    String sentryEventId;
    Instant receivedAt;
}
