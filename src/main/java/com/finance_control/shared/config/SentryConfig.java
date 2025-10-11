package com.finance_control.shared.config;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
public class SentryConfig {

    @Value("${sentry.dsn:}")
    private String sentryDsn;

    @Value("${sentry.environment:dev}")
    private String environment;

    @Value("${sentry.release:1.0.0}")
    private String release;

    @Value("${sentry.sample-rate:0.1}")
    private Double sampleRate;

    @Value("${sentry.traces-sample-rate:0.1}")
    private Double tracesSampleRate;

    @Value("${sentry.profiles-sample-rate:0.1}")
    private Double profilesSampleRate;

    @Value("${sentry.send-default-pii:false}")
    private Boolean sendDefaultPii;

    @Value("${sentry.attach-stacktrace:true}")
    private Boolean attachStacktrace;

    @Value("${sentry.enable-tracing:true}")
    private Boolean enableTracing;

    @Value("${sentry.debug:false}")
    private Boolean debug;

    @Value("${sentry.server-name:finance-control}")
    private String serverName;

    @Value("${sentry.tags:}")
    private String tags;

    @PostConstruct
    public void initializeSentry() {
        if (sentryDsn == null || sentryDsn.isEmpty()) {
            log.warn("Sentry DSN not configured. Error tracking will be disabled.");
            return;
        }

        try {
            Sentry.init(options -> {
                options.setDsn(sentryDsn);
                options.setEnvironment(environment);
                options.setRelease(release);
                options.setSampleRate(sampleRate);
                options.setTracesSampleRate(tracesSampleRate);
                options.setProfilesSampleRate(profilesSampleRate);
                options.setSendDefaultPii(sendDefaultPii);
                options.setAttachStacktrace(attachStacktrace);
                options.setEnableTracing(enableTracing);
                options.setDebug(debug);
                options.setServerName(serverName);

                // Add custom tags
                if (tags != null && !tags.isEmpty()) {
                    String[] tagPairs = tags.split(",");
                    for (String tagPair : tagPairs) {
                        String[] keyValue = tagPair.split(":");
                        if (keyValue.length == 2) {
                            options.setTag(keyValue[0].trim(), keyValue[1].trim());
                        }
                    }
                }

                // Add custom context
                options.setTag("service", "finance-control");
                options.setTag("version", release);
                options.setTag("java.version", System.getProperty("java.version"));
                options.setTag("spring.profiles.active", environment);

                // Configure before send callback for filtering
                options.setBeforeSend((event, hint) -> {
                    // Filter out health check requests
                    if (event.getRequest() != null &&
                        event.getRequest().getUrl() != null &&
                        event.getRequest().getUrl().contains("/actuator/health")) {
                        return null;
                    }
                    return event;
                });

                // Configure before breadcrumb callback
                options.setBeforeBreadcrumb((breadcrumb, hint) -> {
                    // Filter out noisy breadcrumbs
                    if (breadcrumb.getCategory() != null &&
                        breadcrumb.getCategory().equals("http")) {
                        String url = (String) breadcrumb.getData("url");
                        if (url != null && url.contains("/actuator/health")) {
                            return null;
                        }
                    }
                    return breadcrumb;
                });
            });

            log.info("Sentry initialized successfully for environment: {} with release: {}",
                    environment, release);

            // Add initial breadcrumb
            Sentry.addBreadcrumb("Sentry initialized", "system");

        } catch (Exception e) {
            log.error("Failed to initialize Sentry", e);
        }
    }

}
