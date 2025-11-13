package com.finance_control.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class for monitoring and observability features.
 * Sets up metrics, health checks, and alerting systems.
 *
 * Note: Monitoring services are initialized lazily to avoid circular dependencies.
 */
@Slf4j
@Configuration
@EnableScheduling
public class MonitoringConfig {

    // Removed @PostConstruct initialization to avoid circular dependencies
    // Monitoring services will be initialized lazily when first accessed
}
