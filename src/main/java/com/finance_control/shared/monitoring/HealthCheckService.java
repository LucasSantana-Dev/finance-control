package com.finance_control.shared.monitoring;

import com.finance_control.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * Custom health check service for monitoring application components.
 * Provides detailed health status for database, Redis, and other critical components.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AppProperties appProperties;
    private final MetricsService metricsService;

    public Map<String, Object> health() {
        try {
            Map<String, Object> details = new HashMap<>();
            boolean isHealthy = true;

            Map<String, Object> databaseHealth = checkDatabaseHealth();
            details.put("database", databaseHealth);
            if (!"UP".equals(databaseHealth.get("status"))) {
                isHealthy = false;
            }

            Map<String, Object> redisHealth = checkRedisHealth();
            details.put("redis", redisHealth);
            if (!"UP".equals(redisHealth.get("status"))) {
                isHealthy = false;
            }

            Map<String, Object> configHealth = checkConfigurationHealth();
            details.put("configuration", configHealth);
            if (!"UP".equals(configHealth.get("status"))) {
                isHealthy = false;
            }

            details.put("timestamp", LocalDateTime.now().toString());
            details.put("version", getApplicationVersion());
            details.put("environment", "docker");

            Map<String, Object> result = new HashMap<>();
            result.put("status", isHealthy ? "UP" : "DOWN");
            result.put("details", details);

            return result;

        } catch (Exception e) {
            log.error("Error during health check", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "DOWN");
            errorResult.put("error", e.getMessage());
            errorResult.put("timestamp", LocalDateTime.now().toString());
            return errorResult;
        }
    }

    private Map<String, Object> checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);

            Map<String, Object> details = new HashMap<>();
            details.put("status", isValid ? "UP" : "DOWN");
            details.put("url", connection.getMetaData().getURL());
            details.put("driver", connection.getMetaData().getDriverName());
            details.put("version", connection.getMetaData().getDriverVersion());
            details.put("checkTime", LocalDateTime.now().toString());

            return details;

        } catch (SQLException e) {
            log.error("Database health check failed", e);
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", "DOWN");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("checkTime", LocalDateTime.now().toString());
            return errorDetails;
        }
    }

    private Map<String, Object> checkRedisHealth() {
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();

            Map<String, Object> details = new HashMap<>();
            details.put("status", "UP");
            details.put("response", pong);
            details.put("host", appProperties.getRedis().getHost());
            details.put("port", appProperties.getRedis().getPort());
            details.put("database", appProperties.getRedis().getDatabase());
            details.put("checkTime", LocalDateTime.now().toString());

            return details;

        } catch (Exception e) {
            log.error("Redis health check failed", e);
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", "DOWN");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("host", appProperties.getRedis().getHost());
            errorDetails.put("port", appProperties.getRedis().getPort());
            errorDetails.put("checkTime", LocalDateTime.now().toString());
            return errorDetails;
        }
    }

    private Map<String, Object> checkConfigurationHealth() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;

        try {
            if (appProperties.getDatabase().getUrl() == null || appProperties.getDatabase().getUrl().isEmpty()) {
                details.put("databaseUrl", "MISSING");
                isHealthy = false;
            } else {
                details.put("databaseUrl", "CONFIGURED");
            }

            if (appProperties.getSecurity().getJwt().getSecret() == null || appProperties.getSecurity().getJwt().getSecret().isEmpty()) {
                details.put("jwtSecret", "MISSING");
                isHealthy = false;
            } else {
                details.put("jwtSecret", "CONFIGURED");
            }

            if (appProperties.getRedis().getHost() == null || appProperties.getRedis().getHost().isEmpty()) {
                details.put("redisHost", "MISSING");
                isHealthy = false;
            } else {
                details.put("redisHost", "CONFIGURED");
            }

            details.put("cacheEnabled", appProperties.getCache().isEnabled());
            details.put("rateLimitEnabled", appProperties.getRateLimit().isEnabled());
            details.put("monitoringEnabled", appProperties.getMonitoring().isEnabled());

            details.put("checkTime", LocalDateTime.now().toString());
            details.put("status", isHealthy ? "UP" : "DOWN");

            return details;

        } catch (Exception e) {
            log.error("Configuration health check failed", e);
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", "DOWN");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("checkTime", LocalDateTime.now().toString());
            return errorDetails;
        }
    }

    private String getApplicationVersion() {
        try {
            return this.getClass().getPackage().getImplementationVersion();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public Map<String, Object> getDetailedHealthStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            status.put("database", getDatabaseStatus());
            status.put("redis", getRedisStatus());
            status.put("metrics", getApplicationMetrics());
            status.put("system", getSystemResources());

            status.put("timestamp", LocalDateTime.now().toString());
            status.put("overallStatus", "HEALTHY");

        } catch (Exception e) {
            log.error("Error getting detailed health status", e);
            status.put("error", e.getMessage());
            status.put("overallStatus", "UNHEALTHY");
        }

        return status;
    }

    private Map<String, Object> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            status.put("status", "UP");
            status.put("url", connection.getMetaData().getURL());
            status.put("driver", connection.getMetaData().getDriverName());
            status.put("version", connection.getMetaData().getDriverVersion());
            status.put("maxConnections", connection.getMetaData().getMaxConnections());
            status.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
            status.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
        }

        return status;
    }

    private Map<String, Object> getRedisStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();

            status.put("status", "UP");
            status.put("response", pong);
            status.put("host", appProperties.getRedis().getHost());
            status.put("port", appProperties.getRedis().getPort());
            status.put("database", appProperties.getRedis().getDatabase());
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
        }

        return status;
    }

    private Map<String, Object> getApplicationMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            Runtime runtime = Runtime.getRuntime();
            metrics.put("memoryUsed", runtime.totalMemory() - runtime.freeMemory());
            metrics.put("memoryTotal", runtime.totalMemory());
            metrics.put("memoryMax", runtime.maxMemory());
            metrics.put("processors", runtime.availableProcessors());
            metrics.put("uptime", System.currentTimeMillis() - getStartTime());
        } catch (Exception e) {
            metrics.put("error", e.getMessage());
        }

        return metrics;
    }

    private Map<String, Object> getSystemResources() {
        Map<String, Object> resources = new HashMap<>();

        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            resources.put("memoryUsage", (double) usedMemory / totalMemory * 100);
            resources.put("freeMemory", freeMemory);
            resources.put("totalMemory", totalMemory);
            resources.put("maxMemory", runtime.maxMemory());
            resources.put("availableProcessors", runtime.availableProcessors());
        } catch (Exception e) {
            resources.put("error", e.getMessage());
        }

        return resources;
    }

    private long getStartTime() {
        return System.currentTimeMillis() - 3600000;
    }
}
