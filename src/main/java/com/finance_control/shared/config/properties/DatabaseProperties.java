package com.finance_control.shared.config.properties;

/**
 * Database configuration properties.
 */
public record DatabaseProperties(
    String url,
    String username,
    String password,
    String driverClassName,
    String port,
    String name,
    PoolProperties pool
) {
    public record PoolProperties(
        int initialSize,
        int maxSize,
        int minIdle,
        long maxLifetime,
        long connectionTimeout,
        long idleTimeout,
        long leakDetectionThreshold
    ) {}
}
