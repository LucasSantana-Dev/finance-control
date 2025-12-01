package com.finance_control.shared.config;

import io.sentry.Breadcrumb;
import io.sentry.Hint;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Sentry configuration for error tracking and performance monitoring.
 *
 * This configuration class customizes Sentry options for Spring Boot auto-configuration.
 * The sentry-spring-boot-starter automatically initializes Sentry from application.yml,
 * and this class provides additional customizations like filters and tags.
 *
 * Tracing is automatically enabled by the Spring Boot starter when:
 * - sentry.enable-tracing=true (configured in application.yml)
 * - sentry.traces-sample-rate > 0 (configured in application.yml, default: 0.1)
 *
 * The Spring Boot starter automatically instruments:
 * - HTTP requests (Spring MVC, WebFlux)
 * - Database queries (JPA/Hibernate)
 * - Scheduled tasks
 * - WebSocket connections
 *
 * Configuration is primarily done via application.yml. This class adds:
 * - Custom event and breadcrumb filters
 * - Additional tags
 * - Verification of tracing configuration
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.monitoring.sentry.enabled", havingValue = "true", matchIfMissing = true)
public class SentryConfig {

    private final AppProperties appProperties;

    /**
     * Customizes Sentry options after Spring Boot auto-configuration.
     * This method runs after Sentry is initialized by the Spring Boot starter,
     * allowing us to add custom filters and tags.
     */
    @PostConstruct
    public void customizeSentryOptions() {
        com.finance_control.shared.config.properties.MonitoringProperties.SentryProperties sentryConfig =
                appProperties.monitoring().sentry();

        if (!sentryConfig.enabled() || sentryConfig.dsn() == null || sentryConfig.dsn().isEmpty()) {
            log.warn("Sentry is disabled or DSN not configured. Error tracking will be disabled.");
            return;
        }

        if (!Sentry.isEnabled()) {
            log.warn("Sentry is not enabled. Make sure sentry.dsn is configured in application.yml");
            return;
        }

        try {
            SentryOptions options = Sentry.getCurrentHub().getOptions();
            if (options != null) {
                configureFilters(options);
                configureTags(options, sentryConfig);
                verifyTracingConfiguration(sentryConfig);

                log.info("Sentry configured successfully for environment: {} with release: {} (tracing: {})",
                        sentryConfig.environment(),
                        sentryConfig.release(),
                        sentryConfig.enableTracing() ? "enabled" : "disabled");
            }
        } catch (Exception e) {
            log.error("Failed to customize Sentry options", e);
        }
    }

    /**
     * Verifies tracing configuration and logs status.
     * Tracing configuration is done via application.yml, this method just verifies it.
     */
    private void verifyTracingConfiguration(
            com.finance_control.shared.config.properties.MonitoringProperties.SentryProperties sentryConfig) {
        if (sentryConfig.enableTracing()) {
            if (sentryConfig.tracesSampleRate() <= 0) {
                log.warn("Tracing is enabled but traces-sample-rate is 0. No traces will be sent.");
            } else {
                log.info("Sentry tracing enabled with sample rate: {} ({}% of transactions will be traced)",
                        sentryConfig.tracesSampleRate(),
                        (int) (sentryConfig.tracesSampleRate() * 100));
            }
        } else {
            log.info("Sentry tracing is disabled. Enable it by setting sentry.enable-tracing=true");
        }
    }

    /**
     * Configures custom tags for all Sentry events.
     */
    private void configureTags(SentryOptions options,
                               com.finance_control.shared.config.properties.MonitoringProperties.SentryProperties sentryConfig) {
        options.setTag("service", "finance-control");
        options.setTag("version", sentryConfig.release());
        options.setTag("java.version", System.getProperty("java.version"));
        options.setTag("spring.profiles.active", sentryConfig.environment());
    }

    /**
     * Configures event and breadcrumb filters to exclude health checks and API docs.
     */
    private void configureFilters(SentryOptions options) {
        options.setBeforeSend(this::filterSentryEvent);
        options.setBeforeBreadcrumb(this::filterSentryBreadcrumb);
    }

    /**
     * Filters out Sentry events for health checks and API documentation endpoints.
     *
     * @param event The Sentry event
     * @param hint  Additional context
     * @return The event if it should be sent, null if it should be filtered out
     */
    private SentryEvent filterSentryEvent(SentryEvent event, Hint hint) {
        if (event.getRequest() != null && event.getRequest().getUrl() != null) {
            String url = event.getRequest().getUrl();
            if (shouldFilterUrl(url)) {
                return null;
            }
        }
        return event;
    }

    /**
     * Filters out breadcrumbs for health checks and API documentation endpoints.
     *
     * @param breadcrumb The breadcrumb
     * @param hint       Additional context
     * @return The breadcrumb if it should be added, null if it should be filtered out
     */
    private Breadcrumb filterSentryBreadcrumb(Breadcrumb breadcrumb, Hint hint) {
        if (breadcrumb.getCategory() != null && breadcrumb.getCategory().equals("http")) {
            String url = (String) breadcrumb.getData("url");
            if (url != null && shouldFilterUrl(url)) {
                return null;
            }
        }
        return breadcrumb;
    }

    /**
     * Determines if a URL should be filtered out from Sentry tracking.
     *
     * @param url The URL to check
     * @return true if the URL should be filtered out
     */
    private boolean shouldFilterUrl(String url) {
        return url.contains("/actuator/health") ||
               url.contains("/actuator/metrics") ||
               url.contains("/actuator/info") ||
               url.contains("/swagger-ui") ||
               url.contains("/v3/api-docs");
    }
}
