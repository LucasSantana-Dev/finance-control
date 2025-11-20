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
    public DatabaseProperties() {
        this("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
             "sa", "", "org.h2.Driver", "5432", "finance_control", new PoolProperties());
    }

    public record PoolProperties(
        int initialSize,
        int maxSize,
        int minIdle,
        long maxLifetime,
        long connectionTimeout,
        long idleTimeout,
        long leakDetectionThreshold
    ) {
        public PoolProperties() {
            this(5, 20, 5, 300000, 20000, 300000, 60000);
        }
    }
}

