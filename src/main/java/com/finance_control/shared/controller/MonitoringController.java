package com.finance_control.shared.controller;

import com.finance_control.shared.feature.Feature;
import com.finance_control.shared.feature.FeatureFlagService;
import com.finance_control.shared.monitoring.MonitoringService;
import com.finance_control.shared.monitoring.dto.AlertDTO;
import com.finance_control.shared.monitoring.dto.FrontendErrorDTO;
import com.finance_control.shared.monitoring.dto.HealthStatusDTO;
import com.finance_control.shared.monitoring.dto.MonitoringStatusDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for monitoring endpoints.
 * Provides basic monitoring information. For detailed health and metrics, use Spring Actuator endpoints.
 */
@RestController
@RequestMapping("/monitoring")
@Tag(name = "Monitoring", description = "Basic monitoring endpoints. Use /actuator for detailed health and metrics")
@RequiredArgsConstructor
@Slf4j
public class MonitoringController {

    private final MonitoringService monitoringService;
    private final FeatureFlagService featureFlagService;

    @GetMapping("/health")
    @Operation(summary = "Get health status", description = "Returns the health status of the application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health status retrieved successfully")
    })
    public ResponseEntity<HealthStatusDTO> getHealth() {
        log.debug("Health status requested");
        featureFlagService.requireEnabled(Feature.MONITORING);
        HealthStatusDTO health = monitoringService.getHealthStatus();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get active alerts", description = "Returns a list of active alerts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active alerts retrieved successfully")
    })
    public ResponseEntity<List<AlertDTO>> getActiveAlerts() {
        log.debug("Active alerts requested");
        featureFlagService.requireEnabled(Feature.MONITORING);
        List<AlertDTO> alerts = monitoringService.getActiveAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/status")
    @Operation(summary = "Get monitoring status", description = "Returns the status of all monitored components")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monitoring status retrieved successfully")
    })
    public ResponseEntity<MonitoringStatusDTO> getStatus() {
        log.debug("Monitoring status requested");
        featureFlagService.requireEnabled(Feature.MONITORING);
        MonitoringStatusDTO status = monitoringService.getMonitoringStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/frontend-errors")
    @Operation(summary = "Submit frontend error", description = "Submit a frontend error for monitoring and tracking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Frontend error submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid error data")
    })
    public ResponseEntity<Void> submitFrontendError(@Valid @RequestBody FrontendErrorDTO error) {
        log.debug("Frontend error submitted: {}", error.getErrorType());
        featureFlagService.requireEnabled(Feature.MONITORING);
        monitoringService.submitFrontendError(error);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/metrics/summary")
    @Operation(summary = "Get metrics summary", description = "Returns a summary of key application metrics. For detailed metrics, use /actuator/metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrics summary retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        log.debug("Metrics summary requested");
        featureFlagService.requireEnabled(Feature.MONITORING);

        try {
            Map<String, Object> summary = new HashMap<>();

            // Add system metrics
            Runtime runtime = Runtime.getRuntime();
            summary.put("system", Map.of(
                "memoryUsed", runtime.totalMemory() - runtime.freeMemory(),
                "memoryTotal", runtime.totalMemory(),
                "memoryMax", runtime.maxMemory(),
                "processors", runtime.availableProcessors()
            ));

            summary.put("note", "For detailed metrics, use /actuator/metrics endpoint");

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error retrieving metrics summary", e);
            return ResponseEntity.status(500).build();
        }
    }
}
