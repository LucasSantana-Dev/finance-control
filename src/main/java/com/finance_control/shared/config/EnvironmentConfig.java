package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

/**
 * Environment configuration that provides utility methods for accessing environment variables.
 * Logs all important configuration values at startup for debugging purposes.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
public class EnvironmentConfig {
    
    private final AppProperties appProperties;
    private final Environment environment;
    
    @PostConstruct
    public void logConfiguration() {
        log.info("=== Finance Control Application Configuration ===");
        
        // Database Configuration
        log.info("Database URL: {}:{}/{}", 
                appProperties.getDatabase().getUrl(),
                appProperties.getDatabase().getPort(),
                appProperties.getDatabase().getName());
        log.info("Database Username: {}", appProperties.getDatabase().getUsername());
        log.info("Database Pool - Max: {}, Min: {}, Timeout: {}ms",
                appProperties.getDatabase().getPool().getMaxSize(),
                appProperties.getDatabase().getPool().getMinIdle(),
                appProperties.getDatabase().getPool().getConnectionTimeout());
        
        // Server Configuration
        log.info("Server Port: {}", appProperties.getServer().getPort());
        log.info("Server Context Path: '{}'", appProperties.getServer().getContextPath());
        
        // Security Configuration
        log.info("JWT Secret configured: {}", 
                appProperties.getSecurity().getJwt().getSecret() != null ? "YES" : "NO");
        log.info("JWT Expiration: {}ms", appProperties.getSecurity().getJwt().getExpirationMs());
        log.info("CORS Origins: {}", String.join(", ", appProperties.getSecurity().getCors().getAllowedOrigins()));
        
        // Logging Configuration
        log.info("Logging Level: {}", appProperties.getLogging().getLevel());
        log.info("Log File: {}/{}", appProperties.getLogging().getFilePath(), appProperties.getLogging().getFileName());
        
        // JPA Configuration
        log.info("JPA DDL Auto: {}", appProperties.getJpa().getHibernateDdlAuto());
        log.info("JPA Show SQL: {}", appProperties.getJpa().isShowSql());
        
        // Flyway Configuration
        log.info("Flyway Enabled: {}", appProperties.getFlyway().isEnabled());
        log.info("Flyway Locations: {}", String.join(", ", appProperties.getFlyway().getLocations()));
        
        // Actuator Configuration
        log.info("Actuator Enabled: {}", appProperties.getActuator().isEnabled());
        log.info("Actuator Endpoints: {}", String.join(", ", appProperties.getActuator().getEndpoints()));
        
        // OpenAPI Configuration
        log.info("OpenAPI Title: {}", appProperties.getOpenApi().getTitle());
        log.info("OpenAPI Version: {}", appProperties.getOpenApi().getVersion());
        
        // Pagination Configuration
        log.info("Pagination - Default: {}, Max: {}", 
                appProperties.getPagination().getDefaultPageSize(),
                appProperties.getPagination().getMaxPageSize());
        
        // Environment Information
        log.info("Active Profiles: {}", String.join(", ", environment.getActiveProfiles()));
        log.info("Default Profiles: {}", String.join(", ", environment.getDefaultProfiles()));
        
        log.info("=== Configuration Logging Complete ===");
    }
    
    @Bean
    public EnvironmentInfo environmentInfo() {
        return new EnvironmentInfo(appProperties, environment);
    }
    
    /**
     * Utility class for accessing environment information and configuration.
     */
    public static class EnvironmentInfo {
        private final AppProperties appProperties;
        private final Environment environment;
        
        public EnvironmentInfo(AppProperties appProperties, Environment environment) {
            this.appProperties = appProperties;
            this.environment = environment;
        }
        
        public AppProperties getAppProperties() {
            return appProperties;
        }
        
        public Environment getEnvironment() {
            return environment;
        }
        
        public boolean isDevelopment() {
            return environment.acceptsProfiles("dev");
        }
        
        public boolean isProduction() {
            return environment.acceptsProfiles("prod");
        }
        
        public boolean isTest() {
            return environment.acceptsProfiles("test");
        }
        
        public String getDatabaseUrl() {
            return appProperties.getDatabase().getUrl() + ":" + 
                   appProperties.getDatabase().getPort() + "/" + 
                   appProperties.getDatabase().getName();
        }
        
        public String getJwtSecret() {
            return appProperties.getSecurity().getJwt().getSecret();
        }
        
        public int getServerPort() {
            return appProperties.getServer().getPort();
        }
    }
} 