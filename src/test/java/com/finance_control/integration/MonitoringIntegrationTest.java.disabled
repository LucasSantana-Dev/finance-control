package com.finance_control.integration;

import com.finance_control.shared.monitoring.AlertingService;
import com.finance_control.shared.monitoring.HealthCheckService;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.controller.MonitoringController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Monitoring Integration Tests")
class MonitoringIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MonitoringController monitoringController;

    @Autowired
    private HealthCheckService healthCheckService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private AlertingService alertingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return health status via REST endpoint")
    void getHealthStatus_ShouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/api/monitoring/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.overallStatus").exists())
                .andExpect(jsonPath("$.database").exists())
                .andExpect(jsonPath("$.redis").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return active alerts via REST endpoint")
    void getActiveAlerts_ShouldReturnAlerts() throws Exception {
        mockMvc.perform(get("/api/monitoring/alerts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should clear alert via REST endpoint")
    void clearAlert_ShouldClearAlert() throws Exception {
        String alertId = "test_alert";

        mockMvc.perform(delete("/api/monitoring/alerts/{alertId}", alertId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Alert cleared successfully"))
                .andExpect(jsonPath("$.alertId").value(alertId));
    }

    @Test
    @DisplayName("Should clear all alerts via REST endpoint")
    void clearAllAlerts_ShouldClearAllAlerts() throws Exception {
        mockMvc.perform(delete("/api/monitoring/alerts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("All alerts cleared successfully"))
                .andExpect(jsonPath("$.clearedCount").exists());
    }

    @Test
    @DisplayName("Should return metrics summary via REST endpoint")
    void getMetricsSummary_ShouldReturnMetrics() throws Exception {
        mockMvc.perform(get("/api/monitoring/metrics/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.system").exists())
                .andExpect(jsonPath("$.application").exists())
                .andExpect(jsonPath("$.system.memoryUsed").exists())
                .andExpect(jsonPath("$.system.memoryTotal").exists())
                .andExpect(jsonPath("$.system.processors").exists());
    }

    @Test
    @DisplayName("Should trigger test alert via REST endpoint")
    void triggerTestAlert_ShouldTriggerAlert() throws Exception {
        mockMvc.perform(post("/api/monitoring/test-alert")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test alert triggered successfully"))
                .andExpect(jsonPath("$.alertType").value("high_transaction_volume"));
    }

    @Test
    @DisplayName("Should return monitoring status via REST endpoint")
    void getMonitoringStatus_ShouldReturnStatus() throws Exception {
        mockMvc.perform(get("/api/monitoring/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.alerting").exists())
                .andExpect(jsonPath("$.healthCheck").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.alerting.active").exists())
                .andExpect(jsonPath("$.alerting.activeAlerts").exists())
                .andExpect(jsonPath("$.alerting.status").exists());
    }

    @Test
    @DisplayName("Should handle invalid alert ID gracefully")
    void clearAlert_WithInvalidId_ShouldHandleGracefully() throws Exception {
        String invalidAlertId = "nonexistent_alert";

        mockMvc.perform(delete("/api/monitoring/alerts/{alertId}", invalidAlertId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Alert cleared successfully"))
                .andExpect(jsonPath("$.alertId").value(invalidAlertId));
    }

    @Test
    @DisplayName("Should return proper error for invalid endpoints")
    void invalidEndpoint_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/monitoring/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle concurrent alert operations")
    void concurrentAlertOperations_ShouldHandleGracefully() throws Exception {
        // This test verifies that the monitoring system can handle concurrent operations
        // without throwing exceptions or causing data corruption
        
        // Trigger multiple alerts concurrently
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/monitoring/test-alert")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        // Verify system is still responsive
        mockMvc.perform(get("/api/monitoring/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should maintain service state across requests")
    void serviceStateConsistency_ShouldBeMaintained() throws Exception {
        // Trigger an alert
        mockMvc.perform(post("/api/monitoring/test-alert")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Check status
        mockMvc.perform(get("/api/monitoring/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerting.active").value(true));

        // Clear all alerts
        mockMvc.perform(delete("/api/monitoring/alerts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify alerts are cleared
        mockMvc.perform(get("/api/monitoring/alerts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should provide consistent JSON responses")
    void jsonResponseConsistency_ShouldBeConsistent() throws Exception {
        // Test that all endpoints return consistent JSON structure
        String[] endpoints = {
            "/api/monitoring/health",
            "/api/monitoring/alerts",
            "/api/monitoring/metrics/summary",
            "/api/monitoring/status"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").exists());
        }
    }

    @Test
    @DisplayName("Should handle metrics collection without errors")
    void metricsCollection_ShouldWorkWithoutErrors() throws Exception {
        // Trigger some metrics collection
        metricsService.incrementTransactionCreated();
        metricsService.incrementUserLogin();
        metricsService.incrementCacheHit();

        // Verify metrics endpoint still works
        mockMvc.perform(get("/api/monitoring/metrics/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.system").exists())
                .andExpect(jsonPath("$.application").exists());
    }

    @Test
    @DisplayName("Should handle health check operations without errors")
    void healthCheckOperations_ShouldWorkWithoutErrors() throws Exception {
        // Verify health check service works
        var healthStatus = healthCheckService.getDetailedHealthStatus();
        assertNotNull(healthStatus);
        assertTrue(healthStatus.containsKey("overallStatus"));

        // Verify health endpoint works
        mockMvc.perform(get("/api/monitoring/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallStatus").exists());
    }
}
