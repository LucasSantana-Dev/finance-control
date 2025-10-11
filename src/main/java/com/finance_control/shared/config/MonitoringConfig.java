package com.finance_control.shared.config;

import com.finance_control.shared.monitoring.AlertingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

/**
 * Configuration class for monitoring and observability features.
 * Sets up metrics, health checks, and alerting systems.
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class MonitoringConfig {

    private final AppProperties appProperties;
    private final AlertingService alertingService;

    @PostConstruct
    public void initializeMonitoring() {
        if (appProperties.getMonitoring().isEnabled()) {
            log.info("Initializing monitoring and alerting systems");

            alertingService.startMonitoring();

            alertingService.addAlertListener(alert -> {
                log.warn("Alert received: {} - {} - {}",
                    alert.getSeverity(), alert.getType(), alert.getMessage());
            });

            log.info("Monitoring systems initialized successfully");
        } else {
            log.info("Monitoring is disabled in configuration");
        }
    }

    private String getApplicationVersion() {
        try {
            return this.getClass().getPackage().getImplementationVersion();
        } catch (Exception e) {
            return "1.0.0-SNAPSHOT";
        }
    }
}
