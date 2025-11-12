package com.finance_control.shared.controller;

import com.finance_control.shared.monitoring.AlertingService;
import com.finance_control.shared.monitoring.HealthCheckService;
import com.finance_control.shared.monitoring.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for monitoring and observability endpoints.
 * Provides access to system health, metrics, and alerting information.
 */
@RestController
@RequestMapping("/monitoring")
@Tag(name = "Monitoring", description = "System monitoring and observability endpoints")
@RequiredArgsConstructor
@Slf4j
public class MonitoringController {

    private final MetricsService metricsService;
    private final AlertingService alertingService;
    private final HealthCheckService healthCheckService;

    @GetMapping("/health")
    @Operation(summary = "Get basic system health status", description = "Returns basic health information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health status retrieved successfully"),
            @ApiResponse(responseCode = "503", description = "System is unhealthy"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        log.debug("Health status requested");

        try {
            Map<String, Object> healthStatus = healthCheckService.getDetailedHealthStatus();
            return ResponseEntity.ok(healthStatus);
        } catch (Exception e) {
            log.error("Error retrieving health status", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(503).body(errorResponse);
        }
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get active alerts", description = "Returns list of currently active system alerts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<AlertingService.Alert>> getActiveAlerts() {
        log.debug("Active alerts requested");

        try {
            List<AlertingService.Alert> alerts = alertingService.getActiveAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error retrieving active alerts", e);
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/alerts/{alertId}")
    @Operation(summary = "Clear an alert", description = "Manually clear a specific alert by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert cleared successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> clearAlert(@PathVariable String alertId) {
        if (alertId == null || alertId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Clearing alert (ID length: {})", alertId.length());

        try {
            alertingService.clearAlert(alertId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Alert cleared successfully");
            response.put("alertId", alertId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing alert (ID length: {})", alertId.length(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/alerts/clear-all")
    @Operation(summary = "Clear all alerts", description = "Manually clear all active alerts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All alerts cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> clearAllAlerts() {
        log.info("Clearing all alerts");

        try {
            List<AlertingService.Alert> alerts = alertingService.getActiveAlerts();
            for (AlertingService.Alert alert : alerts) {
                alertingService.clearAlert(alert.getId());
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "All alerts cleared successfully");
            response.put("clearedCount", String.valueOf(alerts.size()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing all alerts", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics/summary")
    @Operation(summary = "Get metrics summary", description = "Returns a summary of key application metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrics summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        log.debug("Metrics summary requested");

        try {
            Map<String, Object> summary = new HashMap<>();

            // Add system metrics
            Runtime runtime = Runtime.getRuntime();
            summary.put("system", Map.of(
                "memoryUsed", runtime.totalMemory() - runtime.freeMemory(),
                "memoryTotal", runtime.totalMemory(),
                "memoryMax", runtime.maxMemory(),
                "processors", runtime.availableProcessors(),
                "uptime", System.currentTimeMillis() // This would be actual uptime in production
            ));

            // Add application-specific metrics placeholders
            summary.put("application", Map.of(
                "activeUsers", "N/A", // Would be actual count
                "totalTransactions", "N/A", // Would be actual count
                "activeGoals", "N/A", // Would be actual count
                "cacheHitRate", "N/A" // Would be actual rate
            ));

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error retrieving metrics summary", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/test-alert")
    @Operation(summary = "Trigger test alert", description = "Triggers a test alert for monitoring system validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test alert triggered successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> triggerTestAlert() {
        log.info("Triggering test alert");

        try {
            alertingService.alertHighTransactionVolume(1500L);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Test alert triggered successfully");
            response.put("alertType", "high_transaction_volume");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error triggering test alert", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get monitoring system status", description = "Returns the current status of monitoring systems")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monitoring status retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> getMonitoringStatus() {
        log.debug("Monitoring status requested");

        try {
            Map<String, Object> status = new HashMap<>();

            // Check if monitoring systems are active
            List<AlertingService.Alert> alerts = alertingService.getActiveAlerts();
            status.put("alerting", Map.of(
                "active", true,
                "activeAlerts", alerts.size(),
                "status", alerts.isEmpty() ? "HEALTHY" : "ALERTS_ACTIVE"
            ));

            try {
                Map<String, Object> healthStatus = healthCheckService.getDetailedHealthStatus();
                status.put("healthCheck", Map.of(
                    "active", true,
                    "status", healthStatus.get("overallStatus"),
                    "message", "Health checks active"
                ));
            } catch (Exception e) {
                status.put("healthCheck", Map.of(
                    "active", false,
                    "status", "ERROR",
                    "message", "Health checks failed: " + e.getMessage()
                ));
            }

            status.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error retrieving monitoring status", e);
            return ResponseEntity.status(500).build();
        }
    }
}
