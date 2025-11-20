package com.finance_control.shared.config.properties;

/**
 * Monitoring configuration properties.
 */
public record MonitoringProperties(
    boolean enabled,
    SentryProperties sentry
) {
    public MonitoringProperties() {
        this(true, new SentryProperties());
    }

    public record SentryProperties(
        boolean enabled,
        String dsn,
        String environment,
        String release,
        double sampleRate,
        double tracesSampleRate,
        boolean sendDefaultPii,
        boolean attachStacktrace,
        boolean enableTracing
    ) {
        public SentryProperties() {
            this(true, "", "dev", "1.0.0", 0.1, 0.1, false, true, true);
        }
    }
}

