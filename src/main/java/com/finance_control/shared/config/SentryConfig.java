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
        AppProperties.Sentry sentryConfig = appProperties.monitoring().sentry();

        if (!sentryConfig.enabled() || sentryConfig.dsn() == null || sentryConfig.dsn().isEmpty()) {
            log.warn("Sentry is disabled or DSN not configured. Error tracking will be disabled.");
            return;
        }

        try {
            Sentry.init(options -> {
                options.setDsn(sentryConfig.dsn());
                options.setEnvironment(sentryConfig.environment());
                options.setRelease(sentryConfig.release());
                options.setSampleRate(sentryConfig.sampleRate());
                options.setTracesSampleRate(sentryConfig.tracesSampleRate());
                options.setSendDefaultPii(sentryConfig.sendDefaultPii());
                options.setAttachStacktrace(sentryConfig.attachStacktrace());
                options.setEnableTracing(sentryConfig.enableTracing());
                options.setServerName("finance-control");

                // Add custom context tags
                options.setTag("service", "finance-control");
                options.setTag("version", sentryConfig.release());
                options.setTag("java.version", System.getProperty("java.version"));
                options.setTag("spring.profiles.active", sentryConfig.environment());

                // Filter out health check requests and other noise
                options.setBeforeSend((event, hint) -> {
                    if (event.getRequest() != null && event.getRequest().getUrl() != null) {
                        String url = event.getRequest().getUrl();
                        // Filter out health checks, metrics, and other monitoring endpoints
                        if (url.contains("/actuator/health") ||
                            url.contains("/actuator/metrics") ||
                            url.contains("/actuator/info") ||
                            url.contains("/swagger-ui") ||
                            url.contains("/v3/api-docs")) {
                            return null;
                        }
                    }
                    return event;
                });

                options.setBeforeBreadcrumb((breadcrumb, hint) -> {
                    // Filter out noisy breadcrumbs from monitoring endpoints
                    if (breadcrumb.getCategory() != null && breadcrumb.getCategory().equals("http")) {
                        String url = (String) breadcrumb.getData("url");
                        if (url != null && (url.contains("/actuator/health") ||
                                           url.contains("/actuator/metrics") ||
                                           url.contains("/swagger-ui"))) {
                            return null;
                        }
                    }
                    return breadcrumb;
                });
            });

            log.info("Sentry initialized successfully for environment: {} with release: {}",
                    sentryConfig.environment(), sentryConfig.release());

            // Add initial breadcrumb
            Sentry.addBreadcrumb("Sentry initialized", "system");

        } catch (Exception e) {
            log.error("Failed to initialize Sentry", e);
        }
    }

}
