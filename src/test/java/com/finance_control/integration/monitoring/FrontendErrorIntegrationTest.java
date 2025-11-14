package com.finance_control.integration.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.dto.FrontendErrorReportRequest;
import com.finance_control.shared.error.FrontendErrorLogRepository;
import com.finance_control.shared.error.FrontendErrorService;
import com.finance_control.shared.monitoring.AlertingService;
import com.finance_control.shared.monitoring.SentryService;
import io.sentry.SentryLevel;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@DataJpaTest
@Import(FrontendErrorIntegrationTest.TestConfig.class)
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "app.monitoring.frontendErrors.enabled=true"
})
class FrontendErrorIntegrationTest {

    @Autowired
    private FrontendErrorService frontendErrorService;

    @Autowired
    private FrontendErrorLogRepository repository;

    @MockBean
    private SentryService sentryService;

    @MockBean
    private AlertingService alertingService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        when(sentryService.isEnabled()).thenReturn(true);
        when(sentryService.captureMessageWithResponse(any(), eq(SentryLevel.ERROR))).thenReturn("event-123");
    }

    @Test
    void logFrontendError_ShouldPersistAndTriggerAlert() {
        FrontendErrorReportRequest request = FrontendErrorReportRequest.builder()
            .message("Unhandled promise rejection")
            .severity("HIGH")
            .occurredAt(Instant.now())
            .url("https://app.local/dashboard")
            .component("DashboardOverview")
            .metadata(Map.of("route", "/dashboard"))
            .build();

        frontendErrorService.logFrontendError(request);

        assertThat(repository.count()).isEqualTo(1);
        var saved = repository.findAll().get(0);
        assertThat(saved.getMessage()).isEqualTo("Unhandled promise rejection");
        assertThat(saved.getSeverity().name()).isEqualTo("HIGH");
        assertThat(saved.getUrl()).isEqualTo("https://app.local/dashboard");
        assertThat(saved.getMetadataJson()).contains("route");

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(alertingService).alertFrontendError(eq("HIGH"), any(), dataCaptor.capture());
        assertThat(dataCaptor.getValue()).containsEntry("severity", "HIGH");
        verify(sentryService).captureMessageWithResponse(any(), eq(SentryLevel.ERROR));
    }

    static class TestConfig {

        @Bean
        @Primary
        AppProperties appProperties() {
            return new AppProperties(
                false,
                new AppProperties.Database(),
                new AppProperties.Security(),
                new AppProperties.Server(),
                new AppProperties.Logging(),
                new AppProperties.Jpa(),
                new AppProperties.Flyway(),
                new AppProperties.Actuator(),
                new AppProperties.OpenApi(),
                new AppProperties.Pagination(),
                new AppProperties.Redis(),
                new AppProperties.Cache(),
                new AppProperties.RateLimit(),
                new AppProperties.Ai(),
                new AppProperties.Supabase(),
                new AppProperties.Monitoring(
                    true,
                    new AppProperties.Sentry(),
                    new AppProperties.HealthCheck(),
                    new AppProperties.FrontendErrors(true, 1, 5)
                ),
                new AppProperties.OpenFinance()
            );
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        FrontendErrorService frontendErrorService(
            FrontendErrorLogRepository repository,
            SentryService sentryService,
            AlertingService alertingService,
            ObjectMapper objectMapper,
            AppProperties appProperties
        ) {
            return new FrontendErrorService(repository, sentryService, alertingService, objectMapper, appProperties);
        }
    }
}
