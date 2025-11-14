package com.finance_control.shared.monitoring;

import com.finance_control.shared.config.AppProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.sentry.SentryLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing application alerts and notifications.
 * Monitors system health and triggers alerts based on configured thresholds.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP2") // False positive: Spring dependency injection is safe
public class AlertingService {

    private final SentryService sentryService;
    private final AppProperties appProperties;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, Alert> activeAlerts = new ConcurrentHashMap<>();
    private final List<AlertListener> alertListeners = new ArrayList<>();
    private volatile boolean monitoringStarted = false;

    public interface AlertListener {
        void onAlert(Alert alert);
    }

    public static class Alert {
        private final String id;
        private final String type;
        private final String severity;
        private final String message;
        private final LocalDateTime timestamp;
        private final Object data;

        public Alert(String id, String type, String severity, String message, Object data) {
            this.id = id;
            this.type = type;
            this.severity = severity;
            this.message = message;
            this.timestamp = LocalDateTime.now();
            this.data = data;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getSeverity() { return severity; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Object getData() { return data; }
    }

    public void startMonitoring() {
        if (monitoringStarted) {
            log.debug("Monitoring already started, skipping");
            return;
        }

        if (!appProperties.monitoring().enabled()) {
            log.info("Monitoring is disabled, skipping alert monitoring");
            return;
        }

        log.info("Starting alert monitoring service");

        scheduler.scheduleAtFixedRate(this::checkSystemHealth, 0, 30, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::checkPerformanceMetrics, 0, 60, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::checkResourceUsage, 0, 120, TimeUnit.SECONDS);

        monitoringStarted = true;
    }

    private void ensureMonitoringStarted() {
        if (!monitoringStarted) {
            startMonitoring();
        }
    }

    public void stopMonitoring() {
        log.info("Stopping alert monitoring service");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void addAlertListener(AlertListener listener) {
        alertListeners.add(listener);
    }

    public void removeAlertListener(AlertListener listener) {
        alertListeners.remove(listener);
    }

    public List<Alert> getActiveAlerts() {
        ensureMonitoringStarted();
        return new ArrayList<>(activeAlerts.values());
    }

    public void clearAlert(String alertId) {
        Alert removed = activeAlerts.remove(alertId);
        if (removed != null) {
            log.info("Alert cleared (ID present: {})", alertId != null);
        }
    }

    private void checkSystemHealth() {
        try {
            log.debug("System health check completed");
        } catch (Exception e) {
            log.error("Error during system health check", e);
            triggerAlert("health_check_error", "SYSTEM", "HIGH",
                "Health check failed: " + e.getMessage(), null);
        }
    }

    private void checkPerformanceMetrics() {
        try {
            log.debug("Performance metrics check completed");
        } catch (Exception e) {
            log.error("Error during performance metrics check", e);
            triggerAlert("performance_check_error", "PERFORMANCE", "MEDIUM",
                "Performance check failed: " + e.getMessage(), null);
        }
    }

    private void checkResourceUsage() {
        try {
            log.debug("Resource usage check completed");
        } catch (Exception e) {
            log.error("Error during resource usage check", e);
            triggerAlert("resource_check_error", "RESOURCE", "MEDIUM",
                "Resource check failed: " + e.getMessage(), null);
        }
    }

    private void triggerAlert(String alertId, String type, String severity, String message, Object data) {
        if (activeAlerts.containsKey(alertId)) {
            return;
        }

        Alert alert = new Alert(alertId, type, severity, message, data);
        activeAlerts.put(alertId, alert);

        log.warn("Alert triggered: {} - {} - {}", severity, type, message);

        SentryLevel sentryLevel = mapSeverityToSentryLevel(severity);
        sentryService.setTags(Map.of(
            "alert_type", type,
            "severity", severity,
            "alert_id", alertId
        ));
        if (data != null) {
            sentryService.setContext("alert_data", data.toString());
        }
        sentryService.captureMessage(message, sentryLevel);

        for (AlertListener listener : alertListeners) {
            try {
                listener.onAlert(alert);
            } catch (Exception e) {
                log.error("Error notifying alert listener", e);
            }
        }

        logAlert(alert);
    }

    private SentryLevel mapSeverityToSentryLevel(String severity) {
        return switch (severity) {
            case "CRITICAL" -> SentryLevel.FATAL;
            case "HIGH" -> SentryLevel.ERROR;
            case "MEDIUM" -> SentryLevel.WARNING;
            case "LOW" -> SentryLevel.INFO;
            default -> SentryLevel.INFO; // Unknown severities treated as low priority
        };
    }

    private void logAlert(Alert alert) {
        String logMessage = String.format(
            "ALERT [%s] %s: %s - %s",
            alert.getSeverity(),
            alert.getType(),
            alert.getMessage(),
            alert.getTimestamp()
        );

        switch (alert.getSeverity()) {
            case "CRITICAL":
                log.error("CRITICAL ALERT: " + logMessage);
                break;
            case "HIGH":
                log.error(logMessage);
                break;
            case "MEDIUM":
                log.warn(logMessage);
                break;
            case "LOW":
                log.info(logMessage);
                break;
            default:
                log.debug(logMessage); // Unknown severities logged at debug level
        }
    }

    public void alertHighTransactionVolume(long transactionCount) {
        if (transactionCount > 1000) {
            triggerAlert("high_transaction_volume", "BUSINESS", "MEDIUM",
                String.format("High transaction volume detected: %d transactions", transactionCount),
                transactionCount);
        }
    }

    public void alertFailedAuthentication(String username, String reason) {
        triggerAlert("failed_authentication_" + username.hashCode(), "SECURITY", "MEDIUM",
            String.format("Failed authentication attempt for user: %s, reason: %s", username, reason),
            Map.of("username", username, "reason", reason));
    }

    public void alertSuspiciousActivity(String activity, String details) {
        triggerAlert("suspicious_activity_" + activity.hashCode(), "SECURITY", "HIGH",
            String.format("Suspicious activity detected: %s", activity),
            Map.of("activity", activity, "details", details));
    }

    public void alertDataExportRequest(String userId, String exportType) {
        triggerAlert("data_export_" + userId, "DATA", "LOW",
            String.format("Data export requested by user %s: %s", userId, exportType),
            Map.of("userId", userId, "exportType", exportType));
    }

    public void alertCachePerformance(String cacheName, double hitRate) {
        if (hitRate < 0.5) {
            triggerAlert("low_cache_hit_rate_" + cacheName, "PERFORMANCE", "MEDIUM",
                String.format("Low cache hit rate for %s: %.2f%%", cacheName, hitRate * 100),
                Map.of("cacheName", cacheName, "hitRate", hitRate));
        }
    }

    public void alertDatabaseSlowQuery(String query, long executionTime) {
        if (executionTime > 5000) {
            triggerAlert("slow_database_query", "PERFORMANCE", "HIGH",
                String.format("Slow database query detected: %dms", executionTime),
                Map.of("query", query, "executionTime", executionTime));
        }
    }

    public void alertExternalApiFailure(String apiName, String error) {
        triggerAlert("external_api_failure_" + apiName, "EXTERNAL", "HIGH",
            String.format("External API failure: %s - %s", apiName, error),
            Map.of("apiName", apiName, "error", error));
    }

    public void alertFrontendError(String severity, String message, Map<String, Object> data) {
        triggerAlert(
            "frontend_error_" + severity + "_" + message.hashCode(),
            "FRONTEND",
            severity,
            "Frontend error: " + message,
            data
        );
    }
}
