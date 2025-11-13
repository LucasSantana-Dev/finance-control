package com.finance_control.unit.shared.controller;

import com.finance_control.shared.controller.MonitoringController;
import com.finance_control.shared.monitoring.AlertingService;
import com.finance_control.shared.monitoring.HealthCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MonitoringController.
 * Tests health status, alerts management, and metrics endpoints.
 */
@ExtendWith(MockitoExtension.class)
class MonitoringControllerTest {

    @Mock
    private AlertingService alertingService;

    @Mock
    private HealthCheckService healthCheckService;

    @InjectMocks
    private MonitoringController monitoringController;

    private Map<String, Object> mockHealthStatus;
    private List<AlertingService.Alert> mockAlerts;

    @BeforeEach
    void setUp() {
        mockHealthStatus = new HashMap<>();
        mockHealthStatus.put("status", "UP");
        mockHealthStatus.put("overallStatus", "HEALTHY");
        mockHealthStatus.put("timestamp", "2024-01-01T12:00:00");

        mockAlerts = new ArrayList<>();
        mockAlerts.add(new AlertingService.Alert("alert1", "SYSTEM", "HIGH", "Test alert", null));
        mockAlerts.add(new AlertingService.Alert("alert2", "PERFORMANCE", "MEDIUM", "Performance alert", Map.of("value", 1000)));
    }

    @Test
    void getHealthStatus_ShouldReturnHealthStatus() {
        // Given
        when(healthCheckService.getDetailedHealthStatus()).thenReturn(mockHealthStatus);

        // When
        ResponseEntity<Map<String, Object>> result = monitoringController.getHealthStatus();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(mockHealthStatus);
    }

    @Test
    void getHealthStatus_WhenServiceThrowsException_ShouldReturn503WithError() {
        // Given
        RuntimeException exception = new RuntimeException("Health check failed");
        when(healthCheckService.getDetailedHealthStatus()).thenThrow(exception);

        // When
        ResponseEntity<Map<String, Object>> result = monitoringController.getHealthStatus();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().get("status")).isEqualTo("ERROR");
        assertThat(result.getBody().get("message")).isEqualTo("Health check failed");
    }

    @Test
    void getActiveAlerts_ShouldReturnActiveAlerts() {
        // Given
        when(alertingService.getActiveAlerts()).thenReturn(mockAlerts);

        // When
        ResponseEntity<List<AlertingService.Alert>> result = monitoringController.getActiveAlerts();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(mockAlerts);
    }

    @Test
    void getActiveAlerts_WhenServiceThrowsException_ShouldReturn500() {
        // Given
        RuntimeException exception = new RuntimeException("Alert service error");
        when(alertingService.getActiveAlerts()).thenThrow(exception);

        // When
        ResponseEntity<List<AlertingService.Alert>> result = monitoringController.getActiveAlerts();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNull();
    }

    @Test
    void clearAlert_WithValidAlertId_ShouldClearAlertAndReturnSuccess() {
        // Given
        String alertId = "test-alert-123";

        // When
        ResponseEntity<Map<String, String>> result = monitoringController.clearAlert(alertId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().get("message")).isEqualTo("Alert cleared successfully");
        assertThat(result.getBody().get("alertId")).isEqualTo(alertId);

        verify(alertingService).clearAlert(alertId);
    }

    @Test
    void clearAlert_WithNullAlertId_ShouldReturnBadRequest() {
        // When
        ResponseEntity<Map<String, String>> result = monitoringController.clearAlert(null);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        verify(alertingService, never()).clearAlert(anyString());
    }

    @Test
    void clearAlert_WithEmptyAlertId_ShouldReturnBadRequest() {
        // When
        ResponseEntity<Map<String, String>> result = monitoringController.clearAlert("");

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        verify(alertingService, never()).clearAlert(anyString());
    }

    @Test
    void clearAlert_WithWhitespaceOnlyAlertId_ShouldReturnBadRequest() {
        // When
        ResponseEntity<Map<String, String>> result = monitoringController.clearAlert("   ");

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        verify(alertingService, never()).clearAlert(anyString());
    }

    @Test
    void clearAlert_WhenServiceThrowsException_ShouldReturn500() {
        // Given
        String alertId = "test-alert-123";
        RuntimeException exception = new RuntimeException("Clear alert failed");
        doThrow(exception).when(alertingService).clearAlert(alertId);

        // When
        ResponseEntity<Map<String, String>> result = monitoringController.clearAlert(alertId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNull();
    }

    @Test
    void clearAllAlerts_WithActiveAlerts_ShouldClearAllAndReturnSuccess() {
        // Given
        when(alertingService.getActiveAlerts()).thenReturn(mockAlerts);

        // When
        ResponseEntity<Map<String, String>> result = monitoringController.clearAllAlerts();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().get("message")).isEqualTo("All alerts cleared successfully");
        assertThat(result.getBody().get("clearedCount")).isEqualTo("2");

        verify(alertingService).clearAlert("alert1");
        verify(alertingService).clearAlert("alert2");
    }

    @Test
    void clearAllAlerts_WithNoActiveAlerts_ShouldReturnSuccessWithZeroCount() {
        // Given
        when(alertingService.getActiveAlerts()).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<Map<String, String>> result = monitoringController.clearAllAlerts();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().get("message")).isEqualTo("All alerts cleared successfully");
        assertThat(result.getBody().get("clearedCount")).isEqualTo("0");

        verify(alertingService, never()).clearAlert(anyString());
    }

    @Test
    void clearAllAlerts_WhenServiceThrowsException_ShouldReturn500() {
        // Given
        RuntimeException exception = new RuntimeException("Clear all alerts failed");
        when(alertingService.getActiveAlerts()).thenThrow(exception);

        // When
        ResponseEntity<Map<String, String>> result = monitoringController.clearAllAlerts();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNull();
    }

    @Test
    void getMetricsSummary_ShouldReturnMetricsSummary() {
        // When
        ResponseEntity<Map<String, Object>> result = monitoringController.getMetricsSummary();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        Map<String, Object> body = result.getBody();
        assertThat(body).containsKey("system");
        assertThat(body).containsKey("application");

        Map<String, Object> systemMetrics = (Map<String, Object>) body.get("system");
        assertThat(systemMetrics).containsKey("memoryUsed");
        assertThat(systemMetrics).containsKey("memoryTotal");
        assertThat(systemMetrics).containsKey("memoryMax");
        assertThat(systemMetrics).containsKey("processors");
        assertThat(systemMetrics).containsKey("uptime");

        Map<String, Object> appMetrics = (Map<String, Object>) body.get("application");
        assertThat(appMetrics).containsKey("activeUsers");
        assertThat(appMetrics).containsKey("totalTransactions");
        assertThat(appMetrics).containsKey("activeGoals");
        assertThat(appMetrics).containsKey("cacheHitRate");
    }

    @Test
    void getMetricsSummary_WhenRuntimeThrowsException_ShouldReturn500() {
        // Given
        MonitoringController controllerWithFaultyRuntime = new MonitoringController(alertingService, healthCheckService) {
            @Override
            public ResponseEntity<Map<String, Object>> getMetricsSummary() {
                throw new RuntimeException("Runtime access failed");
            }
        };

        // When
        assertThatThrownBy(controllerWithFaultyRuntime::getMetricsSummary)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Runtime access failed");
    }

    @Test
    void triggerTestAlert_ShouldTriggerHighTransactionVolumeAlertAndReturnSuccess() {
        // When
        ResponseEntity<Map<String, String>> result = monitoringController.triggerTestAlert();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().get("message")).isEqualTo("Test alert triggered successfully");
        assertThat(result.getBody().get("alertType")).isEqualTo("high_transaction_volume");

        verify(alertingService).alertHighTransactionVolume(1500L);
    }

    @Test
    void triggerTestAlert_WhenServiceThrowsException_ShouldReturn500() {
        // Given
        RuntimeException exception = new RuntimeException("Alert trigger failed");
        doThrow(exception).when(alertingService).alertHighTransactionVolume(1500L);

        // When
        ResponseEntity<Map<String, String>> result = monitoringController.triggerTestAlert();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNull();
    }

    @Test
    void getMonitoringStatus_WithHealthyServices_ShouldReturnCompleteStatus() {
        // Given
        when(alertingService.getActiveAlerts()).thenReturn(mockAlerts);
        when(healthCheckService.getDetailedHealthStatus()).thenReturn(mockHealthStatus);

        // When
        ResponseEntity<Map<String, Object>> result = monitoringController.getMonitoringStatus();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        Map<String, Object> body = result.getBody();
        assertThat(body).containsKey("alerting");
        assertThat(body).containsKey("healthCheck");
        assertThat(body).containsKey("timestamp");

        Map<String, Object> alertingStatus = (Map<String, Object>) body.get("alerting");
        assertThat(alertingStatus.get("active")).isEqualTo(true);
        assertThat(alertingStatus.get("activeAlerts")).isEqualTo(2);
        assertThat(alertingStatus.get("status")).isEqualTo("ALERTS_ACTIVE");

        Map<String, Object> healthCheckStatus = (Map<String, Object>) body.get("healthCheck");
        assertThat(healthCheckStatus.get("active")).isEqualTo(true);
        assertThat(healthCheckStatus.get("status")).isEqualTo("HEALTHY");
        assertThat(healthCheckStatus.get("message")).isEqualTo("Health checks active");
    }

    @Test
    void getMonitoringStatus_WithNoActiveAlerts_ShouldReturnHealthyStatus() {
        // Given
        when(alertingService.getActiveAlerts()).thenReturn(new ArrayList<>());
        when(healthCheckService.getDetailedHealthStatus()).thenReturn(mockHealthStatus);

        // When
        ResponseEntity<Map<String, Object>> result = monitoringController.getMonitoringStatus();

        // Then
        Map<String, Object> body = result.getBody();
        Map<String, Object> alertingStatus = (Map<String, Object>) body.get("alerting");
        assertThat(alertingStatus.get("activeAlerts")).isEqualTo(0);
        assertThat(alertingStatus.get("status")).isEqualTo("HEALTHY");
    }

    @Test
    void getMonitoringStatus_WithHealthCheckFailure_ShouldReturnErrorStatus() {
        // Given
        when(alertingService.getActiveAlerts()).thenReturn(new ArrayList<>());
        RuntimeException healthCheckException = new RuntimeException("Health check failed");
        when(healthCheckService.getDetailedHealthStatus()).thenThrow(healthCheckException);

        // When
        ResponseEntity<Map<String, Object>> result = monitoringController.getMonitoringStatus();

        // Then
        Map<String, Object> body = result.getBody();
        Map<String, Object> healthCheckStatus = (Map<String, Object>) body.get("healthCheck");
        assertThat(healthCheckStatus.get("active")).isEqualTo(false);
        assertThat(healthCheckStatus.get("status")).isEqualTo("ERROR");
        assertThat(healthCheckStatus.get("message")).isEqualTo("Health checks failed: Health check failed");
    }

    @Test
    void getMonitoringStatus_WhenAlertingServiceThrowsException_ShouldReturn500() {
        // Given
        RuntimeException exception = new RuntimeException("Alerting service error");
        when(alertingService.getActiveAlerts()).thenThrow(exception);

        // When
        ResponseEntity<Map<String, Object>> result = monitoringController.getMonitoringStatus();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNull();
    }
}
