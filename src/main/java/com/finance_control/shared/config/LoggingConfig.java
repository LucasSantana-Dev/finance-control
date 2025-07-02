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
        AppProperties.Logging logging = appProperties.getLogging();
        
        log.info("Configuring logging - Level: {}, File: {}, MaxSize: {}MB, History: {} days", 
                logging.getLevel(),
                logging.getFileName(),
                logging.getMaxFileSize() / (1024 * 1024),
                logging.getMaxHistory());
        
        // Set system properties for logback configuration
        System.setProperty("LOG_PATH", logging.getFilePath());
        System.setProperty("LOG_FILE", logging.getFileName());
        System.setProperty("LOG_FILE_ERROR", logging.getErrorFileName());
        System.setProperty("LOG_PATTERN", logging.getPattern());
        System.setProperty("LOG_MAX_FILE_SIZE", String.valueOf(logging.getMaxFileSize()));
        System.setProperty("LOG_MAX_HISTORY", String.valueOf(logging.getMaxHistory()));
        System.setProperty("LOG_QUEUE_SIZE", String.valueOf(logging.getQueueSize()));
        System.setProperty("LOG_ASYNC", String.valueOf(logging.isAsync()));
        
        log.info("Logging system properties configured successfully");
    }
} 