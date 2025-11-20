package com.finance_control.shared.feature;

/**
 * Enumeration of all available feature flags in the application.
 * Each feature can be enabled or disabled via configuration.
 */
public enum Feature {
    /**
     * AI-powered financial predictions feature.
     */
    FINANCIAL_PREDICTIONS("financial-predictions"),

    /**
     * Brazilian market data integration feature.
     */
    BRAZILIAN_MARKET("brazilian-market"),

    /**
     * Open Finance/Open Banking features.
     */
    OPEN_FINANCE("open-finance"),

    /**
     * Financial reports generation feature.
     */
    REPORTS("reports"),

    /**
     * Data export functionality feature.
     */
    DATA_EXPORT("data-export"),

    /**
     * Real-time notifications feature.
     */
    REALTIME_NOTIFICATIONS("realtime-notifications"),

    /**
     * Monitoring and observability features.
     */
    MONITORING("monitoring"),

    /**
     * Supabase authentication feature.
     */
    SUPABASE_AUTH("supabase-auth"),

    /**
     * Supabase storage feature.
     */
    SUPABASE_STORAGE("supabase-storage"),

    /**
     * Supabase realtime feature.
     */
    SUPABASE_REALTIME("supabase-realtime");

    private final String configKey;

    Feature(String configKey) {
        this.configKey = configKey;
    }

    /**
     * Get the configuration key for this feature.
     * Used to look up the feature flag in configuration.
     *
     * @return the configuration key
     */
    public String getConfigKey() {
        return configKey;
    }
}
