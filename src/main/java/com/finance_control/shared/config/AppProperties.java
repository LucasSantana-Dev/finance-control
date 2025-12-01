package com.finance_control.shared.config;

import com.finance_control.shared.config.properties.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Application properties configuration that binds environment variables to typed properties.
 * This centralizes all application configuration and provides type-safe access to environment variables.
 * Uses immutable records with constructor binding for thread safety and no EI_EXPOSE_REP warnings.
 */
@Slf4j
@ConfigurationProperties(prefix = "app")
@SuppressFBWarnings("EI_EXPOSE_REP2") // False positive: Spring Boot handles List injection safely in @ConfigurationProperties
public record AppProperties(
    @org.springframework.beans.factory.annotation.Value("${app.supabase.database.enabled:false}")
    boolean supabaseDatabaseEnabled,
    DatabaseProperties database,
    SecurityProperties security,
    ServerProperties server,
    LoggingProperties logging,
    JpaProperties jpa,
    FlywayProperties flyway,
    ActuatorProperties actuator,
    OpenApiProperties openApi,
    PaginationProperties pagination,
    RedisProperties redis,
    CacheProperties cache,
    RateLimitProperties rateLimit,
    AiProperties ai,
    SupabaseProperties supabase,
    MonitoringProperties monitoring,
    OpenFinanceProperties openFinance,
    FeatureFlagsProperties featureFlags
) {



}
