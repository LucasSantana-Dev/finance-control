package com.finance_control.shared.config.properties;

/**
 * Logging configuration properties.
 */
public record LoggingProperties(
    String level,
    String pattern,
    String filePath,
    String fileName,
    String errorFileName,
    int maxFileSize,
    int maxHistory,
    int queueSize,
    boolean async
) {
    public LoggingProperties() {
        this("INFO",
             "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n",
             "logs",
             "finance-control.log",
             "finance-control-error.log",
             10485760,
             30,
             512,
             true);
    }
}

