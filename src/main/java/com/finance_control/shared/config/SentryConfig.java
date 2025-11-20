package com.finance_control.shared.config;

import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Sentry configuration for error tracking and performance monitoring.
 *
 * This configuration class sets up Sentry with comprehensive monitoring
 * capabilities including error tracking, performance monitoring, and
 * custom context information.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SentryConfig {

    private final AppProperties appProperties;

    @PostConstruct
    public void initializeSentry() {
        com.finance_control.shared.config.properties.MonitoringProperties.SentryProperties sentryConfig = appProperties.monitoring().sentry();

        if (!sentryConfig.enabled() || sentryConfig.dsn() == null || sentryConfig.dsn().isEmpty()) {
            log.warn("Sentry is disabled or DSN not configured. Error tracking will be disabled.");
            return;
        }

        try {
            Sentry.init(options -> {
                configureBasicOptions(options, sentryConfig);
                configureTags(options, sentryConfig);
                options.setBeforeSend(this::filterSentryEvent);
                options.setBeforeBreadcrumb(this::filterSentryBreadcrumb);
            });

            log.info("Sentry initialized successfully for environment: {} with release: {}",
                    sentryConfig.environment(), sentryConfig.release());

            // Add initial breadcrumb
            Sentry.addBreadcrumb("Sentry initialized", "system");

        } catch (Exception e) {
            log.error("Failed to initialize Sentry", e);
        }
    }

    private void configureBasicOptions(io.sentry.SentryOptions options, com.finance_control.shared.config.properties.MonitoringProperties.SentryProperties sentryConfig) {
        options.setDsn(sentryConfig.dsn());
        options.setEnvironment(sentryConfig.environment());
        options.setRelease(sentryConfig.release());
        options.setSampleRate(sentryConfig.sampleRate());
        options.setTracesSampleRate(sentryConfig.tracesSampleRate());
        options.setSendDefaultPii(sentryConfig.sendDefaultPii());
        options.setAttachStacktrace(sentryConfig.attachStacktrace());
        options.setEnableTracing(sentryConfig.enableTracing());
        options.setServerName("finance-control");
    }

    private void configureTags(io.sentry.SentryOptions options, com.finance_control.shared.config.properties.MonitoringProperties.SentryProperties sentryConfig) {
        options.setTag("service", "finance-control");
        options.setTag("version", sentryConfig.release());
        options.setTag("java.version", System.getProperty("java.version"));
        options.setTag("spring.profiles.active", sentryConfig.environment());
    }

    private io.sentry.SentryEvent filterSentryEvent(io.sentry.SentryEvent event, io.sentry.Hint hint) {
        if (event.getRequest() != null && event.getRequest().getUrl() != null) {
            String url = event.getRequest().getUrl();
            if (shouldFilterUrl(url)) {
                return null;
            }
        }
        return event;
    }

    private io.sentry.Breadcrumb filterSentryBreadcrumb(io.sentry.Breadcrumb breadcrumb, io.sentry.Hint hint) {
        if (breadcrumb.getCategory() != null && breadcrumb.getCategory().equals("http")) {
            String url = (String) breadcrumb.getData("url");
            if (url != null && shouldFilterUrl(url)) {
                return null;
            }
        }
        return breadcrumb;
    }

    private boolean shouldFilterUrl(String url) {
        return url.contains("/actuator/health") ||
               url.contains("/actuator/metrics") ||
               url.contains("/actuator/info") ||
               url.contains("/swagger-ui") ||
               url.contains("/v3/api-docs");
    }

}
