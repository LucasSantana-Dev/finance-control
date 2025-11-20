package com.finance_control.shared.config.properties;

/**
 * Rate limit configuration properties.
 */
public record RateLimitProperties(
    boolean enabled,
    int requestsPerMinute,
    int burstCapacity,
    int refreshPeriod
) {
    public RateLimitProperties() {
        this(true, 100, 200, 60);
    }
}

