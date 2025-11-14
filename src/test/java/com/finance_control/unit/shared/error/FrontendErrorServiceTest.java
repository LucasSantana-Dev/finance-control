package com.finance_control.unit.shared.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.dto.FrontendErrorReportRequest;
import com.finance_control.shared.dto.FrontendErrorReportRequest.ClientContext;
import com.finance_control.shared.dto.FrontendErrorReportRequest.UserContext;
import com.finance_control.shared.dto.FrontendErrorReportResponse;
import com.finance_control.shared.error.FrontendErrorLog;
import com.finance_control.shared.error.FrontendErrorLogRepository;
import com.finance_control.shared.error.FrontendErrorService;
import com.finance_control.shared.monitoring.AlertingService;
import com.finance_control.shared.monitoring.SentryService;
import io.sentry.SentryLevel;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FrontendErrorServiceTest {

    @Mock
    private FrontendErrorLogRepository repository;

    @Mock
    private SentryService sentryService;

    @Mock
    private AlertingService alertingService;

    @Mock
    private AppProperties appProperties;

    private FrontendErrorService frontendErrorService;
    private ObjectMapper objectMapper;
    private AppProperties.Monitoring monitoringConfig;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        monitoringConfig = new AppProperties.Monitoring(
            true,
            new AppProperties.Sentry(),
            new AppProperties.HealthCheck(),
            new AppProperties.FrontendErrors(true, 1, 5)
        );
        frontendErrorService = new FrontendErrorService(
            repository,
            sentryService,
            alertingService,
            objectMapper,
            appProperties
        );
    }

    @Test
    void logFrontendError_ShouldPersistForwardToSentryAndTriggerAlert() {
        FrontendErrorReportRequest request = FrontendErrorReportRequest.builder()
            .message("TypeError: Cannot read properties of undefined")
            .errorType("TypeError")
            .severity("HIGH")
            .occurredAt(Instant.now())
            .release("1.0.0")
            .environment("production")
            .url("https://app.example.com/dashboard")
            .component("DashboardChart")
            .stackTrace("TypeError stack ...")
            .metadata(Map.of("tenantId", "tenant-1"))
            .user(UserContext.builder().id("123").email("user@example.com").ipAddress("127.0.0.1").build())
            .client(ClientContext.builder().browser("Chrome").userAgent("Chrome/125").locale("pt-BR").build())
            .build();

        when(appProperties.monitoring()).thenReturn(monitoringConfig);
        when(repository.save(any(FrontendErrorLog.class))).thenAnswer(invocation -> {
            FrontendErrorLog log = invocation.getArgument(0);
            log.setId(42L);
            return log;
        });
        when(repository.countBySeveritySince(any(), any())).thenReturn(1L);
        when(sentryService.isEnabled()).thenReturn(true);
        when(sentryService.captureMessageWithResponse(anyString(), eq(SentryLevel.ERROR))).thenReturn("event-123");

        FrontendErrorReportResponse response = frontendErrorService.logFrontendError(request);

        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getStatus()).isEqualTo("RECEIVED");
        assertThat(response.getSentryEventId()).isEqualTo("event-123");

        verify(repository).save(any(FrontendErrorLog.class));
        verify(sentryService).setTags(anyMap());
        verify(sentryService).setContext(eq("frontend_error"), anyMap());
        verify(sentryService).captureMessageWithResponse(anyString(), eq(SentryLevel.ERROR));

        ArgumentCaptor<Map<String, Object>> alertCaptor = ArgumentCaptor.forClass(Map.class);
        verify(alertingService).alertFrontendError(eq("HIGH"), anyString(), alertCaptor.capture());
        assertThat(alertCaptor.getValue()).containsEntry("severity", "HIGH");
    }

    @Test
    void logFrontendError_ShouldSkipAlertWhenThresholdNotMet() {
        FrontendErrorReportRequest request = FrontendErrorReportRequest.builder()
            .message("Minor warning")
            .occurredAt(Instant.now())
            .severity("LOW")
            .build();

        AppProperties.Monitoring monitoring = new AppProperties.Monitoring(
            true,
            new AppProperties.Sentry(),
            new AppProperties.HealthCheck(),
            new AppProperties.FrontendErrors(true, 5, 10)
        );
        when(appProperties.monitoring()).thenReturn(monitoring);

        when(repository.save(any(FrontendErrorLog.class))).thenAnswer(invocation -> {
            FrontendErrorLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });
        when(repository.countBySeveritySince(any(), any())).thenReturn(1L);
        when(sentryService.isEnabled()).thenReturn(false);

        FrontendErrorReportResponse response = frontendErrorService.logFrontendError(request);

        assertThat(response.getSentryEventId()).isNull();
        verify(alertingService, never()).alertFrontendError(anyString(), anyString(), anyMap());
    }
}
