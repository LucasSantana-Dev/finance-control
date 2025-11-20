package com.finance_control.shared.monitoring;

import com.finance_control.shared.monitoring.dto.AlertDTO;
import com.finance_control.shared.monitoring.dto.FrontendErrorDTO;
import com.finance_control.shared.monitoring.dto.HealthStatusDTO;
import com.finance_control.shared.monitoring.dto.MonitoringStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for monitoring operations including health checks, alerts, and status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    @PersistenceContext
    private EntityManager entityManager;

    private final SentryService sentryService;

    // In-memory storage for alerts (in production, use a database)
    private final ConcurrentMap<String, AlertDTO> alerts = new ConcurrentHashMap<>();

    // In-memory storage for frontend errors (in production, use a database or Sentry)
    private final ConcurrentMap<String, FrontendErrorDTO> frontendErrors = new ConcurrentHashMap<>();

    /**
     * Get health status.
     */
    public HealthStatusDTO getHealthStatus() {
        try {
            // Check database connectivity
            boolean dbHealthy = checkDatabaseHealth();

            String status = dbHealthy ? "UP" : "DOWN";
            String version = getApplicationVersion();

            return HealthStatusDTO.builder()
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .version(version)
                    .build();
        } catch (Exception e) {
            log.error("Error checking health status", e);
            return HealthStatusDTO.builder()
                    .status("DOWN")
                    .timestamp(LocalDateTime.now())
                    .version("unknown")
                    .build();
        }
    }

    /**
     * Get active alerts.
     */
    public List<AlertDTO> getActiveAlerts() {
        return alerts.values().stream()
                .filter(alert -> !Boolean.TRUE.equals(alert.getResolved()))
                .toList();
    }

    /**
     * Get monitoring status for all components.
     */
    public MonitoringStatusDTO getMonitoringStatus() {
        String databaseStatus = checkDatabaseHealth() ? "UP" : "DOWN";
        String cacheStatus = checkCacheHealth() ? "UP" : "DOWN";
        String externalServicesStatus = checkExternalServicesHealth() ? "UP" : "DOWN";

        // Overall status is DOWN if any component is DOWN
        String overallStatus = (databaseStatus.equals("UP") && cacheStatus.equals("UP") && externalServicesStatus.equals("UP"))
                ? "UP" : "DOWN";

        return MonitoringStatusDTO.builder()
                .database(databaseStatus)
                .cache(cacheStatus)
                .externalServices(externalServicesStatus)
                .overall(overallStatus)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Submit a frontend error.
     */
    public void submitFrontendError(FrontendErrorDTO error) {
        log.warn("Frontend error received: {} - {}", error.getErrorType(), error.getMessage());

        // Store the error
        String errorId = UUID.randomUUID().toString();
        error.setOccurredAt(LocalDateTime.now());
        frontendErrors.put(errorId, error);

        // Send to Sentry if configured
        if (sentryService != null) {
            try {
                // Create a simple exception for Sentry
                Exception exception = new Exception(error.getMessage());
                Map<String, Object> context = new HashMap<>();
                context.put("errorType", error.getErrorType());
                context.put("component", error.getComponent() != null ? error.getComponent() : "frontend");
                context.put("url", error.getUrl());
                context.put("severity", error.getSeverity());
                context.put("environment", error.getEnvironment() != null ? error.getEnvironment() : "unknown");
                context.put("release", error.getRelease() != null ? error.getRelease() : "unknown");
                sentryService.captureException(exception, context);
            } catch (Exception e) {
                log.error("Failed to send frontend error to Sentry", e);
            }
        }

        // Create an alert if severity is HIGH or CRITICAL
        if ("HIGH".equals(error.getSeverity()) || "CRITICAL".equals(error.getSeverity())) {
            createAlert(error.getErrorType(), error.getMessage(), error.getComponent());
        }
    }

    private boolean checkDatabaseHealth() {
        try {
            // Simple database connectivity check
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            log.debug("Database health check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkCacheHealth() {
        // For now, assume cache is always UP
        // In production, check Redis or other cache connectivity
        return true;
    }

    private boolean checkExternalServicesHealth() {
        // For now, assume external services are UP
        // In production, check external API connectivity
        return true;
    }

    private String getApplicationVersion() {
        try {
            Package pkg = this.getClass().getPackage();
            String version = pkg != null ? pkg.getImplementationVersion() : null;
            return version != null ? version : "1.0.0";
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    private void createAlert(String type, String message, String component) {
        AlertDTO alert = AlertDTO.builder()
                .id(UUID.randomUUID().toString())
                .severity("HIGH")
                .message(message)
                .component(component != null ? component : "frontend")
                .occurredAt(LocalDateTime.now())
                .resolved(false)
                .build();

        alerts.put(alert.getId(), alert);
        log.warn("Alert created: {} - {}", alert.getId(), alert.getMessage());
    }
}
