package com.finance_control.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache configuration for in-memory caching.
 * Provides cache manager with TTL settings matching RedisConfig for consistency.
 * This is the default cache provider for single-user deployments.
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
public class CaffeineConfig {

    private final AppProperties appProperties;

    /**
     * Configures Caffeine cache manager with different TTL for different cache names.
     * This bean is primary and will be used unless RedisConfig is active (production profile).
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "cacheManager")
    public CacheManager cacheManager() {
        com.finance_control.shared.config.properties.CacheProperties cache = appProperties.cache();

        log.info("Configuring Caffeine cache manager - Dashboard TTL: {}ms, Market Data TTL: {}ms, User Data TTL: {}ms",
                cache.ttlDashboard(), cache.ttlMarketData(), cache.ttlUserData());

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Default cache configuration (dashboard - 15 minutes)
        Caffeine<Object, Object> defaultCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(cache.ttlDashboard(), TimeUnit.MILLISECONDS)
                .recordStats();

        cacheManager.setCaffeine(defaultCache);

        // Cache-specific configurations
        // Dashboard cache - 15 minutes
        cacheManager.registerCustomCache("dashboard", Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(cache.ttlDashboard(), TimeUnit.MILLISECONDS)
                .recordStats()
                .build());

        // Market data cache - 5 minutes
        cacheManager.registerCustomCache("market-data", Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(cache.ttlMarketData(), TimeUnit.MILLISECONDS)
                .recordStats()
                .build());

        // User data cache - 30 minutes
        cacheManager.registerCustomCache("user-data", Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(cache.ttlUserData(), TimeUnit.MILLISECONDS)
                .recordStats()
                .build());

        // Transaction cache - 10 minutes
        cacheManager.registerCustomCache("transactions", Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build());

        // Goals cache - 20 minutes
        cacheManager.registerCustomCache("goals", Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(20))
                .recordStats()
                .build());

        return cacheManager;
    }
}




