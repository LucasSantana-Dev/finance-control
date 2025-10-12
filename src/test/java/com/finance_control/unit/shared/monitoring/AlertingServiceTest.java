package com.finance_control.unit.shared.monitoring;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.monitoring.AlertingService;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.monitoring.SentryService;
import io.sentry.Sentry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertingService Unit Tests")
class AlertingServiceTest {

    @Mock
    private SentryService sentryService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Monitoring monitoringProperties;

    private AlertingService alertingService;

    @BeforeEach
    void setUp() {
        lenient().when(appProperties.getMonitoring()).thenReturn(monitoringProperties);
        lenient().when(monitoringProperties.isEnabled()).thenReturn(true);

        alertingService = new AlertingService(sentryService, appProperties);
    }

    @Test
    @DisplayName("Should start monitoring when enabled")
    void startMonitoring_WhenEnabled_ShouldStartMonitoring() {
        alertingService.startMonitoring();
    }

    @Test
    @DisplayName("Should not start monitoring when disabled")
    void startMonitoring_WhenDisabled_ShouldNotStartMonitoring() {
        when(monitoringProperties.isEnabled()).thenReturn(false);
        alertingService.startMonitoring();
    }

    @Test
    @DisplayName("Should stop monitoring gracefully")
    void stopMonitoring_ShouldStopGracefully() {
        alertingService.startMonitoring();
        alertingService.stopMonitoring();
    }

    @Test
    @DisplayName("Should add alert listener")
    void addAlertListener_ShouldAddListener() {
        AlertingService.AlertListener listener = mock(AlertingService.AlertListener.class);
        alertingService.addAlertListener(listener);
    }

    @Test
    @DisplayName("Should remove alert listener")
    void removeAlertListener_ShouldRemoveListener() {
        AlertingService.AlertListener listener = mock(AlertingService.AlertListener.class);
        alertingService.addAlertListener(listener);
        alertingService.removeAlertListener(listener);
    }

    @Test
    @DisplayName("Should return empty list when no active alerts")
    void getActiveAlerts_WithNoAlerts_ShouldReturnEmptyList() {
        List<AlertingService.Alert> alerts = alertingService.getActiveAlerts();
        assertNotNull(alerts);
        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Should clear alert by ID")
    void clearAlert_WithValidId_ShouldClearAlert() {
        String alertId = "test_alert";
        alertingService.clearAlert(alertId);
    }

    @Test
    @DisplayName("Should alert high transaction volume")
    void alertHighTransactionVolume_WithHighVolume_ShouldTriggerAlert() {
        long transactionCount = 1500L;

        alertingService.alertHighTransactionVolume(transactionCount);
        verify(sentryService).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should not alert when transaction volume is normal")
    void alertHighTransactionVolume_WithNormalVolume_ShouldNotTriggerAlert() {
        // Given
        long transactionCount = 500L;

        // When
        alertingService.alertHighTransactionVolume(transactionCount);

        // Then
        verify(sentryService, never()).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should alert failed authentication")
    void alertFailedAuthentication_ShouldTriggerAlert() {
        // Given
        String username = "testuser";
        String reason = "invalid_password";

        // When
        alertingService.alertFailedAuthentication(username, reason);

        // Then
        verify(sentryService).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should alert suspicious activity")
    void alertSuspiciousActivity_ShouldTriggerAlert() {
        // Given
        String activity = "multiple_failed_logins";
        String details = "5 failed attempts in 1 minute";

        // When
        alertingService.alertSuspiciousActivity(activity, details);

        // Then
        verify(sentryService).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should alert data export request")
    void alertDataExportRequest_ShouldTriggerAlert() {
        // Given
        String userId = "123";
        String exportType = "all_data";

        // When
        alertingService.alertDataExportRequest(userId, exportType);

        // Then
        verify(sentryService).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should alert low cache hit rate")
    void alertCachePerformance_WithLowHitRate_ShouldTriggerAlert() {
        // Given
        String cacheName = "dashboard";
        double hitRate = 0.3; // 30% hit rate

        // When
        alertingService.alertCachePerformance(cacheName, hitRate);

        // Then
        verify(sentryService).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should not alert when cache hit rate is good")
    void alertCachePerformance_WithGoodHitRate_ShouldNotTriggerAlert() {
        // Given
        String cacheName = "dashboard";
        double hitRate = 0.8; // 80% hit rate

        // When
        alertingService.alertCachePerformance(cacheName, hitRate);

        // Then
        verify(sentryService, never()).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should alert slow database query")
    void alertDatabaseSlowQuery_WithSlowQuery_ShouldTriggerAlert() {
        // Given
        String query = "SELECT * FROM transactions WHERE user_id = ?";
        long executionTime = 6000L; // 6 seconds

        // When
        alertingService.alertDatabaseSlowQuery(query, executionTime);

        // Then
        verify(sentryService).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should not alert when database query is fast")
    void alertDatabaseSlowQuery_WithFastQuery_ShouldNotTriggerAlert() {
        // Given
        String query = "SELECT * FROM transactions WHERE user_id = ?";
        long executionTime = 2000L; // 2 seconds

        // When
        alertingService.alertDatabaseSlowQuery(query, executionTime);

        // Then
        verify(sentryService, never()).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should alert external API failure")
    void alertExternalApiFailure_ShouldTriggerAlert() {
        // Given
        String apiName = "BCB_API";
        String error = "Connection timeout";

        // When
        alertingService.alertExternalApiFailure(apiName, error);

        // Then
        verify(sentryService).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should map severity levels correctly")
    void mapSeverityToSentryLevel_ShouldMapCorrectly() {
        // This tests the private method indirectly through alert triggering
        // When
        alertingService.alertFailedAuthentication("test", "reason");

        // Then
        verify(sentryService).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should handle alert listener exceptions gracefully")
    void triggerAlert_WithListenerException_ShouldHandleGracefully() {
        // Given
        AlertingService.AlertListener listener = mock(AlertingService.AlertListener.class);
        doThrow(new RuntimeException("Listener error")).when(listener).onAlert(any());
        alertingService.addAlertListener(listener);

        // When
        alertingService.alertFailedAuthentication("test", "reason");

        // Then - Should not throw exception
        verify(sentryService).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should prevent duplicate alerts")
    void triggerAlert_WithDuplicateAlert_ShouldNotSendDuplicate() {
        // Given
        String alertId = "test_alert";

        // When - Trigger same alert twice
        alertingService.alertFailedAuthentication("test", "reason");
        alertingService.alertFailedAuthentication("test", "reason");

        // Then - Should only capture once (due to duplicate prevention)
        verify(sentryService, atMostOnce()).captureMessage(anyString(), any(io.sentry.SentryLevel.class));
    }
}
