package com.finance_control.unit.shared.monitoring;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.monitoring.HealthCheckService;
import com.finance_control.shared.monitoring.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckService Unit Tests")
class HealthCheckServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private AppProperties appProperties;

    @Mock
    private MetricsService metricsService;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;


    @Mock
    private RedisConnectionFactory redisConnectionFactory;
    
    @Mock
    private RedisConnection redisConnection;

    private HealthCheckService healthCheckService;

    @BeforeEach
    void setUp() throws SQLException {
        // Setup mocks
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getURL()).thenReturn("jdbc:postgresql://localhost:5432/test");
        when(databaseMetaData.getDriverName()).thenReturn("PostgreSQL JDBC Driver");
        when(databaseMetaData.getDriverVersion()).thenReturn("42.6.0");
        when(databaseMetaData.getMaxConnections()).thenReturn(100);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("15.0");

        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        // Setup AppProperties
        AppProperties.Redis redisProperties = new AppProperties.Redis();
        redisProperties.setHost("localhost");
        redisProperties.setPort(6379);
        redisProperties.setDatabase(0);

        AppProperties.Security securityProperties = new AppProperties.Security();
        securityProperties.getJwt().setSecret("test-secret");

        AppProperties.Database databaseProperties = new AppProperties.Database();
        databaseProperties.setUrl("jdbc:postgresql://localhost:5432/test");

        when(appProperties.getRedis()).thenReturn(redisProperties);
        when(appProperties.getSecurity()).thenReturn(securityProperties);
        when(appProperties.getDatabase()).thenReturn(databaseProperties);

        healthCheckService = new HealthCheckService(dataSource, redisTemplate, appProperties);
    }

    @Test
    @DisplayName("Should return healthy status when all components are healthy")
    void health_WithAllComponentsHealthy_ShouldReturnUp() {
        // When
        Map<String, Object> health = healthCheckService.health();

        // Then
        assertEquals("UP", health.get("status"));
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) health.get("details");
        assertTrue(details.containsKey("database"));
        assertTrue(details.containsKey("redis"));
        assertTrue(details.containsKey("configuration"));
        assertTrue(details.containsKey("timestamp"));
    }

    @Test
    @DisplayName("Should return unhealthy status when database is down")
    void health_WithDatabaseDown_ShouldReturnDown() throws SQLException {
        // Given
        when(connection.isValid(anyInt())).thenReturn(false);

        // When
        Map<String, Object> health = healthCheckService.health();

        // Then
        assertEquals("DOWN", health.get("status"));
    }

    @Test
    @DisplayName("Should return unhealthy status when database connection fails")
    void health_WithDatabaseConnectionFailure_ShouldReturnDown() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        // When
        Map<String, Object> health = healthCheckService.health();

        // Then
        assertEquals("DOWN", health.get("status"));
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) health.get("details");
        assertTrue(details.containsKey("error"));
    }

    @Test
    @DisplayName("Should return unhealthy status when Redis is down")
    void health_WithRedisDown_ShouldReturnDown() {
        // Given
        when(redisConnection.ping()).thenThrow(new RuntimeException("Redis connection failed"));

        // When
        Map<String, Object> health = healthCheckService.health();

        // Then
        assertEquals("DOWN", health.get("status"));
    }

    @Test
    @DisplayName("Should return unhealthy status when configuration is missing")
    void health_WithMissingConfiguration_ShouldReturnDown() {
        // Given
        AppProperties.Database databaseProperties = new AppProperties.Database();
        databaseProperties.setUrl(null); // Missing database URL
        when(appProperties.getDatabase()).thenReturn(databaseProperties);

        // When
        Map<String, Object> health = healthCheckService.health();

        // Then
        assertEquals("DOWN", health.get("status"));
    }

    @Test
    @DisplayName("Should return detailed health status")
    void getDetailedHealthStatus_ShouldReturnDetailedStatus() {
        // When
        Map<String, Object> status = healthCheckService.getDetailedHealthStatus();

        // Then
        assertNotNull(status);
        assertTrue(status.containsKey("database"));
        assertTrue(status.containsKey("redis"));
        assertTrue(status.containsKey("metrics"));
        assertTrue(status.containsKey("system"));
        assertTrue(status.containsKey("timestamp"));
        assertEquals("HEALTHY", status.get("overallStatus"));
    }

    @Test
    @DisplayName("Should handle exception in detailed health status")
    void getDetailedHealthStatus_WithException_ShouldReturnUnhealthyStatus() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenThrow(new SQLException("Database error"));

        // When
        Map<String, Object> status = healthCheckService.getDetailedHealthStatus();

        // Then
        assertNotNull(status);
        assertEquals("UNHEALTHY", status.get("overallStatus"));
        assertTrue(status.containsKey("error"));
    }

    @Test
    @DisplayName("Should include database status in detailed health")
    void getDetailedHealthStatus_ShouldIncludeDatabaseStatus() {
        // When
        Map<String, Object> status = healthCheckService.getDetailedHealthStatus();

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> databaseStatus = (Map<String, Object>) status.get("database");
        assertNotNull(databaseStatus);
        assertEquals("UP", databaseStatus.get("status"));
        assertTrue(databaseStatus.containsKey("url"));
        assertTrue(databaseStatus.containsKey("driver"));
        assertTrue(databaseStatus.containsKey("version"));
    }

    @Test
    @DisplayName("Should include Redis status in detailed health")
    void getDetailedHealthStatus_ShouldIncludeRedisStatus() {
        // When
        Map<String, Object> status = healthCheckService.getDetailedHealthStatus();

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> redisStatus = (Map<String, Object>) status.get("redis");
        assertNotNull(redisStatus);
        assertEquals("UP", redisStatus.get("status"));
        assertEquals("PONG", redisStatus.get("response"));
        assertTrue(redisStatus.containsKey("host"));
        assertTrue(redisStatus.containsKey("port"));
    }

    @Test
    @DisplayName("Should include system resources in detailed health")
    void getDetailedHealthStatus_ShouldIncludeSystemResources() {
        // When
        Map<String, Object> status = healthCheckService.getDetailedHealthStatus();

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> systemResources = (Map<String, Object>) status.get("system");
        assertNotNull(systemResources);
        assertTrue(systemResources.containsKey("memoryUsage"));
        assertTrue(systemResources.containsKey("freeMemory"));
        assertTrue(systemResources.containsKey("totalMemory"));
        assertTrue(systemResources.containsKey("maxMemory"));
        assertTrue(systemResources.containsKey("availableProcessors"));
    }

    @Test
    @DisplayName("Should include application metrics in detailed health")
    void getDetailedHealthStatus_ShouldIncludeApplicationMetrics() {
        // When
        Map<String, Object> status = healthCheckService.getDetailedHealthStatus();

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) status.get("metrics");
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("memoryUsed"));
        assertTrue(metrics.containsKey("memoryTotal"));
        assertTrue(metrics.containsKey("memoryMax"));
        assertTrue(metrics.containsKey("processors"));
        assertTrue(metrics.containsKey("uptime"));
    }

    @Test
    @DisplayName("Should handle Redis connection factory failure")
    void getDetailedHealthStatus_WithRedisConnectionFactoryFailure_ShouldHandleGracefully() {
        // Given
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("Connection factory error"));

        // When
        Map<String, Object> status = healthCheckService.getDetailedHealthStatus();

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> redisStatus = (Map<String, Object>) status.get("redis");
        assertNotNull(redisStatus);
        assertEquals("DOWN", redisStatus.get("status"));
        assertTrue(redisStatus.containsKey("error"));
    }

    @Test
    @DisplayName("Should handle database metadata failure")
    void getDetailedHealthStatus_WithDatabaseMetadataFailure_ShouldHandleGracefully() throws SQLException {
        // Given
        when(connection.getMetaData()).thenThrow(new SQLException("Metadata error"));

        // When
        Map<String, Object> status = healthCheckService.getDetailedHealthStatus();

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> databaseStatus = (Map<String, Object>) status.get("database");
        assertNotNull(databaseStatus);
        assertEquals("DOWN", databaseStatus.get("status"));
        assertTrue(databaseStatus.containsKey("error"));
    }
}
