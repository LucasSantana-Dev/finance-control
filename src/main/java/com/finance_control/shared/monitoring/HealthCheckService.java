package com.finance_control.shared.monitoring;

import com.finance_control.shared.config.AppProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
@SuppressFBWarnings("EI_EXPOSE_REP2") // False positive: Spring dependency injection is safe
public class HealthCheckService {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AppProperties appProperties;

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

            return java.util.Collections.unmodifiableMap(result);

        } catch (Exception e) {
            log.error("Error during health check", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "DOWN");
            errorResult.put("error", e.getMessage());
            errorResult.put("timestamp", LocalDateTime.now().toString());
            return java.util.Collections.unmodifiableMap(errorResult);
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

            return java.util.Collections.unmodifiableMap(details);

        } catch (SQLException e) {
            log.error("Database health check failed", e);
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", "DOWN");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("checkTime", LocalDateTime.now().toString());
            return java.util.Collections.unmodifiableMap(errorDetails);
        }
    }

    private Map<String, Object> checkRedisHealth() {
        try {
            var connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory == null) {
                throw new IllegalStateException("Redis connection factory is not available");
            }

            String pong = connectionFactory.getConnection().ping();

            Map<String, Object> details = new HashMap<>();
            details.put("status", "UP");
            details.put("response", pong);
            details.put("host", appProperties.redis().host());
            details.put("port", appProperties.redis().port());
            details.put("database", appProperties.redis().database());
            details.put("checkTime", LocalDateTime.now().toString());

            return java.util.Collections.unmodifiableMap(details);

        } catch (Exception e) {
            log.error("Redis health check failed", e);
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", "DOWN");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("host", appProperties.redis().host());
            errorDetails.put("port", appProperties.redis().port());
            errorDetails.put("checkTime", LocalDateTime.now().toString());
            return java.util.Collections.unmodifiableMap(errorDetails);
        }
    }

    private Map<String, Object> checkConfigurationHealth() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;

        try {
            if (appProperties.database().url() == null || appProperties.database().url().isEmpty()) {
                details.put("databaseUrl", "MISSING");
                isHealthy = false;
            } else {
                details.put("databaseUrl", "CONFIGURED");
            }

            if (appProperties.security().jwt().secret() == null || appProperties.security().jwt().secret().isEmpty()) {
                details.put("jwtSecret", "MISSING");
                isHealthy = false;
            } else {
                details.put("jwtSecret", "CONFIGURED");
            }

            if (appProperties.redis().host() == null || appProperties.redis().host().isEmpty()) {
                details.put("redisHost", "MISSING");
                isHealthy = false;
            } else {
                details.put("redisHost", "CONFIGURED");
            }

            details.put("cacheEnabled", appProperties.cache().enabled());
            details.put("rateLimitEnabled", appProperties.rateLimit().enabled());
            details.put("monitoringEnabled", appProperties.monitoring().enabled());

            details.put("checkTime", LocalDateTime.now().toString());
            details.put("status", isHealthy ? "UP" : "DOWN");

            return java.util.Collections.unmodifiableMap(details);

        } catch (Exception e) {
            log.error("Configuration health check failed", e);
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", "DOWN");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("checkTime", LocalDateTime.now().toString());
            return java.util.Collections.unmodifiableMap(errorDetails);
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
            // Simple test response
            status.put("status", "UP");
            status.put("timestamp", LocalDateTime.now().toString());
            status.put("message", "Health check service is working");
            status.put("overallStatus", "HEALTHY");

        } catch (Exception e) {
            log.error("Error getting detailed health status", e);
            status.put("error", e.getMessage());
            status.put("overallStatus", "UNHEALTHY");
        }

        return java.util.Collections.unmodifiableMap(status);
    }

}
