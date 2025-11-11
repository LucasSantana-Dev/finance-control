package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration for caching and rate limiting.
 * Provides Redis connection, cache manager, and Redis template configuration.
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
public class RedisConfig {

    private final AppProperties appProperties;

    /**
     * Configures Redis connection factory with connection pooling.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        AppProperties.Redis redis = appProperties.getRedis();

        log.info("Configuring Redis connection - Host: {}, Port: {}, Database: {}",
                redis.getHost(), redis.getPort(), redis.getDatabase());

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redis.getHost());
        config.setPort(redis.getPort());
        config.setDatabase(redis.getDatabase());

        if (redis.getPassword() != null && !redis.getPassword().isEmpty()) {
            config.setPassword(redis.getPassword());
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setTimeout(redis.getTimeout());

        return factory;
    }

    /**
     * Configures Redis template for general Redis operations.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configures cache manager with different TTL for different cache names.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        AppProperties.Cache cache = appProperties.getCache();

        log.info("Configuring cache manager - Dashboard TTL: {}ms, Market Data TTL: {}ms, User Data TTL: {}ms",
                cache.getTtlDashboard(), cache.getTtlMarketData(), cache.getTtlUserData());

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(cache.getTtlDashboard()))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Cache-specific configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Dashboard cache - 15 minutes
        cacheConfigurations.put("dashboard", defaultConfig.entryTtl(Duration.ofMillis(cache.getTtlDashboard())));

        // Market data cache - 5 minutes
        cacheConfigurations.put("market-data", defaultConfig.entryTtl(Duration.ofMillis(cache.getTtlMarketData())));

        // User data cache - 30 minutes
        cacheConfigurations.put("user-data", defaultConfig.entryTtl(Duration.ofMillis(cache.getTtlUserData())));

        // Transaction cache - 10 minutes
        cacheConfigurations.put("transactions", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Goals cache - 20 minutes
        cacheConfigurations.put("goals", defaultConfig.entryTtl(Duration.ofMinutes(20)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
