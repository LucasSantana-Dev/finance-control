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
 * Environment configuration that logs important configuration values at startup.
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
        if (log.isInfoEnabled()) {
            log.info("=== Finance Control Application Configuration ===");
            log.info("Database: {}:{}/{}", appProperties.database().url(),
                    appProperties.database().port(), appProperties.database().name());
            log.info("Server Port: {}", appProperties.server().port());
            log.info("JWT Secret configured: {}",
                    appProperties.security().jwt().secret() != null ? "YES" : "NO");
            log.info("Active Profiles: {}", String.join(", ", environment.getActiveProfiles()));
            log.info("=== Configuration Logging Complete ===");
        }
    }

    @Bean
    public EnvironmentInfo environmentInfo() {
        return new EnvironmentInfo(appProperties, environment);
    }

    /**
     * Utility class for accessing environment information.
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
