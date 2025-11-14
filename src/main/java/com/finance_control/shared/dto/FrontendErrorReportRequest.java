package com.finance_control.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance_control.shared.error.FrontendErrorSeverity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FrontendErrorReportRequest {

    @NotBlank
    @Size(max = 1024)
    private final String message;

    @Size(max = 255)
    private final String errorType;

    private final String severity;

    @NotNull
    private final Instant occurredAt;

    @Size(max = 128)
    private final String release;

    @Size(max = 64)
    private final String environment;

    @Valid
    private final UserContext user;

    @Valid
    private final ClientContext client;

    @Size(max = 2048)
    private final String url;

    @Size(max = 255)
    private final String component;

    private final Map<String, Object> metadata;

    @Size(max = 10000)
    private final String stackTrace;

    public FrontendErrorSeverity resolvedSeverity() {
        return FrontendErrorSeverity.from(severity);
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserContext {
        @Size(max = 64)
        private final String id;

        @Size(max = 255)
        private final String email;

        @Size(max = 128)
        private final String sessionId;

        @Size(max = 64)
        private final String ipAddress;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ClientContext {
        @Size(max = 255)
        private final String browser;

        @Size(max = 512)
        private final String userAgent;

        @Size(max = 255)
        private final String platform;

        @Size(max = 64)
        private final String locale;
    }
}
