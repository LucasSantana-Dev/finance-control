package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Logging configuration that uses environment variables through AppProperties.
 * Configures logging levels, patterns, and file settings from environment variables.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
public class LoggingConfig {
    
    private final AppProperties appProperties;
    
    @PostConstruct
    public void configureLogging() {
        AppProperties.Logging logging = appProperties.logging();

        log.info("Configuring logging - Level: {}, File: {}, MaxSize: {}MB, History: {} days",
                logging.level(),
                logging.fileName(),
                logging.maxFileSize() / (1024 * 1024),
                logging.maxHistory());

        // Set system properties for logback configuration
        System.setProperty("LOG_PATH", logging.filePath());
        System.setProperty("LOG_FILE", logging.fileName());
        System.setProperty("LOG_FILE_ERROR", logging.errorFileName());
        System.setProperty("LOG_PATTERN", logging.pattern());
        System.setProperty("LOG_MAX_FILE_SIZE", String.valueOf(logging.maxFileSize()));
        System.setProperty("LOG_MAX_HISTORY", String.valueOf(logging.maxHistory()));
        System.setProperty("LOG_QUEUE_SIZE", String.valueOf(logging.queueSize()));
        System.setProperty("LOG_ASYNC", String.valueOf(logging.async()));
        
        log.info("Logging system properties configured successfully");
    }
} 