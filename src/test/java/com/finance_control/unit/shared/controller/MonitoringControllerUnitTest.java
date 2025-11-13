package com.finance_control.unit.shared.controller;

import com.finance_control.shared.controller.MonitoringController;
import com.finance_control.shared.monitoring.AlertingService;
import com.finance_control.shared.monitoring.HealthCheckService;
import com.finance_control.shared.monitoring.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringController Unit Tests")
class MonitoringControllerUnitTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private AlertingService alertingService;

    @Mock
    private HealthCheckService healthCheckService;

    private MonitoringController monitoringController;

    @BeforeEach
    void setUp() {
        monitoringController = new MonitoringController(alertingService, healthCheckService);
    }

    @Test
    @DisplayName("Should return health status successfully")
    void getHealthStatus_ShouldReturnHealthStatus() {
        // Given
        Map<String, Object> expectedHealth = new HashMap<>();
        expectedHealth.put("status", "UP");
        expectedHealth.put("overallStatus", "HEALTHY");
        expectedHealth.put("timestamp", "2025-01-01T00:00:00");

        when(healthCheckService.getDetailedHealthStatus()).thenReturn(expectedHealth);

        // When
        ResponseEntity<Map<String, Object>> response = monitoringController.getHealthStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("HEALTHY", response.getBody().get("overallStatus"));
        verify(healthCheckService).getDetailedHealthStatus();
    }

    @Test
    @DisplayName("Should return 503 when health check fails")
    void getHealthStatus_WhenHealthCheckFails_ShouldReturn503() {
        // Given
        when(healthCheckService.getDetailedHealthStatus()).thenThrow(new RuntimeException("Health check failed"));

        // When
        ResponseEntity<Map<String, Object>> response = monitoringController.getHealthStatus();

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
        assertTrue(response.getBody().get("message").toString().contains("Health check failed"));
    }

    @Test
    @DisplayName("Should return active alerts successfully")
    void getActiveAlerts_ShouldReturnAlerts() {
        // Given
        List<AlertingService.Alert> expectedAlerts = List.of();
        when(alertingService.getActiveAlerts()).thenReturn(expectedAlerts);

        // When
        ResponseEntity<List<AlertingService.Alert>> response = monitoringController.getActiveAlerts();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedAlerts, response.getBody());
        verify(alertingService).getActiveAlerts();
    }

    @Test
    @DisplayName("Should return 500 when alerting service fails")
    void getActiveAlerts_WhenServiceFails_ShouldReturn500() {
        // Given
        when(alertingService.getActiveAlerts()).thenThrow(new RuntimeException("Alerting service failed"));

        // When
        ResponseEntity<List<AlertingService.Alert>> response = monitoringController.getActiveAlerts();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(alertingService).getActiveAlerts();
    }

    @Test
    @DisplayName("Should clear alert successfully")
    void clearAlert_ShouldClearAlert() {
        // Given
        String alertId = "test_alert";
        doNothing().when(alertingService).clearAlert(alertId);

        // When
        ResponseEntity<Map<String, String>> response = monitoringController.clearAlert(alertId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Alert cleared successfully", response.getBody().get("message"));
        assertEquals(alertId, response.getBody().get("alertId"));
        verify(alertingService).clearAlert(alertId);
    }

    @Test
    @DisplayName("Should return 500 when clearing alert fails")
    void clearAlert_WhenServiceFails_ShouldReturn500() {
        // Given
        String alertId = "test_alert";
        doThrow(new RuntimeException("Failed to clear alert")).when(alertingService).clearAlert(alertId);

        // When
        ResponseEntity<Map<String, String>> response = monitoringController.clearAlert(alertId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(alertingService).clearAlert(alertId);
    }

    @Test
    @DisplayName("Should clear all alerts successfully")
    void clearAllAlerts_ShouldClearAllAlerts() {
        // Given
        List<AlertingService.Alert> alerts = List.of(
            createMockAlert("alert1"),
            createMockAlert("alert2")
        );
        when(alertingService.getActiveAlerts()).thenReturn(alerts);
        doNothing().when(alertingService).clearAlert(anyString());

        // When
        ResponseEntity<Map<String, String>> response = monitoringController.clearAllAlerts();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("All alerts cleared successfully", response.getBody().get("message"));
        assertEquals("2", response.getBody().get("clearedCount"));
        verify(alertingService).getActiveAlerts();
        verify(alertingService, times(2)).clearAlert(anyString());
    }

    @Test
    @DisplayName("Should return metrics summary successfully")
    void getMetricsSummary_ShouldReturnMetrics() {
        // When
        ResponseEntity<Map<String, Object>> response = monitoringController.getMetricsSummary();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("system"));
        assertTrue(response.getBody().containsKey("application"));

        @SuppressWarnings("unchecked")
        Map<String, Object> system = (Map<String, Object>) response.getBody().get("system");
        assertTrue(system.containsKey("memoryUsed"));
        assertTrue(system.containsKey("memoryTotal"));
        assertTrue(system.containsKey("processors"));
    }

    @Test
    @DisplayName("Should trigger test alert successfully")
    void triggerTestAlert_ShouldTriggerAlert() {
        // Given
        doNothing().when(alertingService).alertHighTransactionVolume(1500L);

        // When
        ResponseEntity<Map<String, String>> response = monitoringController.triggerTestAlert();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test alert triggered successfully", response.getBody().get("message"));
        assertEquals("high_transaction_volume", response.getBody().get("alertType"));
        verify(alertingService).alertHighTransactionVolume(1500L);
    }

    @Test
    @DisplayName("Should return monitoring status successfully")
    void getMonitoringStatus_ShouldReturnStatus() {
        // Given
        List<AlertingService.Alert> alerts = List.of();
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("overallStatus", "HEALTHY");

        when(alertingService.getActiveAlerts()).thenReturn(alerts);
        when(healthCheckService.getDetailedHealthStatus()).thenReturn(healthStatus);

        // When
        ResponseEntity<Map<String, Object>> response = monitoringController.getMonitoringStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("alerting"));
        assertTrue(response.getBody().containsKey("healthCheck"));
        assertTrue(response.getBody().containsKey("timestamp"));

        @SuppressWarnings("unchecked")
        Map<String, Object> alerting = (Map<String, Object>) response.getBody().get("alerting");
        assertEquals(true, alerting.get("active"));
        assertEquals(0, alerting.get("activeAlerts"));
        assertEquals("HEALTHY", alerting.get("status"));
    }

    @Test
    @DisplayName("Should handle health check failure in monitoring status")
    void getMonitoringStatus_WhenHealthCheckFails_ShouldHandleGracefully() {
        // Given
        List<AlertingService.Alert> alerts = List.of();
        when(alertingService.getActiveAlerts()).thenReturn(alerts);
        when(healthCheckService.getDetailedHealthStatus()).thenThrow(new RuntimeException("Health check failed"));

        // When
        ResponseEntity<Map<String, Object>> response = monitoringController.getMonitoringStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("alerting"));
        assertTrue(response.getBody().containsKey("healthCheck"));

        @SuppressWarnings("unchecked")
        Map<String, Object> healthCheck = (Map<String, Object>) response.getBody().get("healthCheck");
        assertEquals(false, healthCheck.get("active"));
        assertEquals("ERROR", healthCheck.get("status"));
        assertTrue(healthCheck.get("message").toString().contains("Health checks failed"));
    }

    private AlertingService.Alert createMockAlert(String id) {
        AlertingService.Alert alert = mock(AlertingService.Alert.class);
        when(alert.getId()).thenReturn(id);
        return alert;
    }
}
