package com.finance_control.shared.config.properties;

/**
 * Redis configuration properties.
 */
public record RedisProperties(
    String host,
    int port,
    String password,
    int database,
    long timeout,
    RedisPoolProperties pool
) {
    public RedisProperties() {
        this("localhost", 6379, "", 0, 2000, new RedisPoolProperties());
    }

    public record RedisPoolProperties(
        int maxActive,
        int maxIdle,
        int minIdle,
        long maxWait
    ) {
        public RedisPoolProperties() {
            this(8, 8, 0, -1);
        }
    }
}

