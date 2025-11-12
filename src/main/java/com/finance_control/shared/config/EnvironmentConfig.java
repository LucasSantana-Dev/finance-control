package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

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
                appProperties.database().url(),
                appProperties.database().port(),
                appProperties.database().name());
        log.info("Database Username: {}", appProperties.database().username());
        log.info("Database Pool - Max: {}, Min: {}, Timeout: {}ms",
                appProperties.database().pool().maxSize(),
                appProperties.database().pool().minIdle(),
                appProperties.database().pool().connectionTimeout());

        // Server Configuration
        log.info("Server Port: {}", appProperties.server().port());
        log.info("Server Context Path: '{}'", appProperties.server().contextPath());

        // Security Configuration
        log.info("JWT Secret configured: {}",
                appProperties.security().jwt().secret() != null ? "YES" : "NO");
        log.info("JWT Expiration: {}ms", appProperties.security().jwt().expirationMs());
        log.info("CORS Origins: {}", String.join(", ", appProperties.security().cors().allowedOrigins()));

        // Logging Configuration
        log.info("Logging Level: {}", appProperties.logging().level());
        log.info("Log File: {}/{}", appProperties.logging().filePath(), appProperties.logging().fileName());

        // JPA Configuration
        log.info("JPA DDL Auto: {}", appProperties.jpa().hibernateDdlAuto());
        log.info("JPA Show SQL: {}", appProperties.jpa().showSql());

        // Flyway Configuration
        log.info("Flyway Enabled: {}", appProperties.flyway().enabled());
        log.info("Flyway Locations: {}", String.join(", ", appProperties.flyway().locations()));

        // Actuator Configuration
        log.info("Actuator Enabled: {}", appProperties.actuator().enabled());
        log.info("Actuator Endpoints: {}", String.join(", ", appProperties.actuator().endpoints()));

        // OpenAPI Configuration
        log.info("OpenAPI Title: {}", appProperties.openApi().title());
        log.info("OpenAPI Version: {}", appProperties.openApi().version());

        // Pagination Configuration
        log.info("Pagination - Default: {}, Max: {}",
                appProperties.pagination().defaultPageSize(),
                appProperties.pagination().maxPageSize());

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
            return environment.acceptsProfiles(Profiles.of("dev"));
        }

        public boolean isProduction() {
            return environment.acceptsProfiles(Profiles.of("prod"));
        }

        public boolean isTest() {
            return environment.acceptsProfiles(Profiles.of("test"));
        }

        public String getDatabaseUrl() {
            return appProperties.database().url() + ":" +
                   appProperties.database().port() + "/" +
                   appProperties.database().name();
        }

        public String getJwtSecret() {
            return appProperties.security().jwt().secret();
        }

        public int getServerPort() {
            return appProperties.server().port();
        }
    }
}
