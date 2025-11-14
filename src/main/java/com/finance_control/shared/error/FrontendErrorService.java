package com.finance_control.shared.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.dto.FrontendErrorReportRequest;
import com.finance_control.shared.dto.FrontendErrorReportResponse;
import com.finance_control.shared.monitoring.AlertingService;
import com.finance_control.shared.monitoring.SentryService;
import io.sentry.SentryLevel;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "app.monitoring.frontendErrors.enabled", havingValue = "true", matchIfMissing = true)
public class FrontendErrorService {

    private final FrontendErrorLogRepository repository;
    private final SentryService sentryService;
    private final AlertingService alertingService;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    @Transactional
    public FrontendErrorReportResponse logFrontendError(FrontendErrorReportRequest request) {
        FrontendErrorSeverity severity = request.resolvedSeverity();
        FrontendErrorLog logEntry = mapToEntity(request, severity);
        repository.save(logEntry);

        String sentryEventId = forwardToSentry(logEntry, request);
        triggerAlertsIfNeeded(logEntry, severity);

        return FrontendErrorReportResponse.builder()
            .id(logEntry.getId())
            .status("RECEIVED")
            .sentryEventId(sentryEventId)
            .receivedAt(logEntry.getReceivedAt())
            .build();
    }

    private FrontendErrorLog mapToEntity(FrontendErrorReportRequest request, FrontendErrorSeverity severity) {
        FrontendErrorLog logEntry = new FrontendErrorLog();
        logEntry.setMessage(request.getMessage());
        logEntry.setErrorType(request.getErrorType());
        logEntry.setSeverity(severity);
        logEntry.setOccurredAt(request.getOccurredAt());
        logEntry.setRelease(request.getRelease());
        logEntry.setEnvironment(request.getEnvironment());
        if (request.getUser() != null) {
            logEntry.setUserId(request.getUser().getId());
            logEntry.setUserEmail(request.getUser().getEmail());
            logEntry.setSessionId(request.getUser().getSessionId());
            logEntry.setIpAddress(request.getUser().getIpAddress());
        }
        if (request.getClient() != null) {
            logEntry.setBrowser(request.getClient().getBrowser());
            logEntry.setUserAgent(request.getClient().getUserAgent());
        }
        logEntry.setComponent(request.getComponent());
        logEntry.setUrl(request.getUrl());
        logEntry.setStackTrace(request.getStackTrace());
        logEntry.setMetadataJson(serializeMetadata(request.getMetadata()));
        logEntry.setReceivedAt(Instant.now());
        return logEntry;
    }

    private String forwardToSentry(FrontendErrorLog logEntry, FrontendErrorReportRequest request) {
        if (!sentryService.isEnabled()) {
            return null;
        }

        Map<String, String> tags = new HashMap<>();
        tags.put("source", "frontend");
        tags.put("severity", logEntry.getSeverity().name());
        if (StringUtils.hasText(logEntry.getComponent())) {
            tags.put("component", logEntry.getComponent());
        }
        if (StringUtils.hasText(logEntry.getEnvironment())) {
            tags.put("environment", logEntry.getEnvironment());
        }
        if (StringUtils.hasText(logEntry.getRelease())) {
            tags.put("release", logEntry.getRelease());
        }
        sentryService.setTags(tags);

        Map<String, Object> context = new HashMap<>();
        context.put("url", logEntry.getUrl());
        context.put("stackTrace", logEntry.getStackTrace());
        context.put("metadata", request.getMetadata());
        context.put("occurredAt", logEntry.getOccurredAt());
        context.put("receivedAt", logEntry.getReceivedAt());
        sentryService.setContext("frontend_error", context);

        if (request.getUser() != null) {
            sentryService.setUserContext(
                parseUserId(logEntry.getUserId()),
                logEntry.getUserEmail(),
                request.getUser().getId());
        }

        try {
            return sentryService.captureMessageWithResponse(
                formatSentryMessage(logEntry),
                mapSeverity(logEntry.getSeverity()));
        } finally {
            sentryService.clearUserContext();
        }
    }

    private void triggerAlertsIfNeeded(FrontendErrorLog logEntry, FrontendErrorSeverity severity) {
        AppProperties.Monitoring monitoring = appProperties.monitoring();
        if (!monitoring.enabled() || monitoring.frontendErrors() == null || !monitoring.frontendErrors().enabled()) {
            return;
        }

        int threshold = monitoring.frontendErrors().alertThreshold();
        int windowMinutes = monitoring.frontendErrors().alertWindowMinutes();
        Instant windowStart = Instant.now().minus(windowMinutes, ChronoUnit.MINUTES);
        long count = repository.countBySeveritySince(severity, windowStart);

        if (count >= threshold || severity.isAtLeast(FrontendErrorSeverity.HIGH)) {
            Map<String, Object> data = new HashMap<>();
            data.put("errorId", logEntry.getId());
            data.put("message", logEntry.getMessage());
            data.put("severity", severity.name());
            data.put("count", count);
            data.put("url", logEntry.getUrl());
            data.put("component", logEntry.getComponent());
            alertingService.alertFrontendError(severity.name(), logEntry.getMessage(), data);
        }
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (CollectionUtils.isEmpty(metadata)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize metadata for frontend error: {}", e.getMessage());
            return null;
        }
    }

    private String formatSentryMessage(FrontendErrorLog logEntry) {
        return String.format(
            "Frontend error reported: %s | component=%s | url=%s",
            logEntry.getMessage(),
            StringUtils.hasText(logEntry.getComponent()) ? logEntry.getComponent() : "unknown",
            StringUtils.hasText(logEntry.getUrl()) ? logEntry.getUrl() : "unknown");
    }

    private Long parseUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException ex) {
            log.debug("Unable to parse userId '{}' into Long, skipping numeric context", userId);
            return null;
        }
    }

    private SentryLevel mapSeverity(FrontendErrorSeverity severity) {
        return switch (severity) {
            case CRITICAL -> SentryLevel.FATAL;
            case HIGH -> SentryLevel.ERROR;
            case MEDIUM -> SentryLevel.WARNING;
            case LOW -> SentryLevel.INFO;
        };
    }
}
