package com.finance_control.shared.config.properties;

/**
 * Feature flags configuration properties.
 * Controls which features are enabled or disabled in the application.
 */
public record FeatureFlagsProperties(
    FinancialPredictionsProperties financialPredictions,
    BrazilianMarketProperties brazilianMarket,
    OpenFinanceProperties openFinance,
    ReportsProperties reports,
    DataExportProperties dataExport,
    RealtimeNotificationsProperties realtimeNotifications,
    MonitoringProperties monitoring,
    SupabaseFeaturesProperties supabaseFeatures
) {
    public FeatureFlagsProperties() {
        this(
            new FinancialPredictionsProperties(),
            new BrazilianMarketProperties(),
            new OpenFinanceProperties(),
            new ReportsProperties(),
            new DataExportProperties(),
            new RealtimeNotificationsProperties(),
            new MonitoringProperties(),
            new SupabaseFeaturesProperties()
        );
    }

    /**
     * Financial predictions feature properties.
     */
    public record FinancialPredictionsProperties(
        boolean enabled
    ) {
        public FinancialPredictionsProperties() {
            this(true);
        }
    }

    /**
     * Brazilian market data feature properties.
     */
    public record BrazilianMarketProperties(
        boolean enabled
    ) {
        public BrazilianMarketProperties() {
            this(true);
        }
    }

    /**
     * Open Finance feature properties.
     */
    public record OpenFinanceProperties(
        boolean enabled
    ) {
        public OpenFinanceProperties() {
            this(false);
        }
    }

    /**
     * Reports feature properties.
     */
    public record ReportsProperties(
        boolean enabled
    ) {
        public ReportsProperties() {
            this(true);
        }
    }

    /**
     * Data export feature properties.
     */
    public record DataExportProperties(
        boolean enabled
    ) {
        public DataExportProperties() {
            this(true);
        }
    }

    /**
     * Real-time notifications feature properties.
     */
    public record RealtimeNotificationsProperties(
        boolean enabled
    ) {
        public RealtimeNotificationsProperties() {
            this(true);
        }
    }

    /**
     * Monitoring feature properties.
     */
    public record MonitoringProperties(
        boolean enabled
    ) {
        public MonitoringProperties() {
            this(true);
        }
    }

    /**
     * Supabase features properties.
     */
    public record SupabaseFeaturesProperties(
        boolean auth,
        boolean storage,
        boolean realtime
    ) {
        public SupabaseFeaturesProperties() {
            this(true, true, true);
        }
    }
}
