package com.finance_control.shared.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Rate limiting configuration using Bucket4j and Redis.
 * Provides rate limiting capabilities for API endpoints.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
public class RateLimitConfig {

    private final AppProperties appProperties;

    /**
     * Creates a rate limiting bucket with configured limits.
     * For now, using in-memory rate limiting. Distributed rate limiting can be added later.
     */
    @Bean
    public Bucket rateLimitBucket() {
        AppProperties.RateLimit rateLimit = appProperties.getRateLimit();

        log.info("Configuring rate limiting - Requests per minute: {}, Burst capacity: {}, Refresh period: {}s",
                rateLimit.getRequestsPerMinute(), rateLimit.getBurstCapacity(), rateLimit.getRefreshPeriod());

        Bandwidth limit = Bandwidth.classic(
                rateLimit.getBurstCapacity(),
                Refill.intervally(rateLimit.getRequestsPerMinute(), Duration.ofSeconds(rateLimit.getRefreshPeriod()))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
