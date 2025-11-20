package com.finance_control.shared.config.properties;

/**
 * Cache configuration properties.
 */
public record CacheProperties(
    boolean enabled,
    long ttlDashboard,
    long ttlMarketData,
    long ttlUserData
) {
    public CacheProperties() {
        this(true, 900000, 300000, 1800000);
    }
}

