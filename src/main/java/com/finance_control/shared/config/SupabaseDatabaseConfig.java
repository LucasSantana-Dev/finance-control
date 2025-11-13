package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuration for Supabase PostgreSQL database integration.
 * This allows the application to use Supabase's PostgreSQL database instead of a local one.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.database.enabled", havingValue = "true", matchIfMissing = false)
public class SupabaseDatabaseConfig {

    private final AppProperties appProperties;

    /**
     * Creates a DataSourceProperties bean configured for Supabase PostgreSQL.
     * This overrides the default data source properties when Supabase database is enabled.
     */
    @Bean
    @Primary
    @ConfigurationProperties("app.supabase.database")
    public DataSourceProperties supabaseDataSourceProperties() {
        AppProperties.SupabaseDatabase dbConfig = appProperties.supabase().database();

        if (!dbConfig.enabled()) {
            throw new IllegalStateException("Supabase database is not enabled");
        }

        if (dbConfig.host().isEmpty() || dbConfig.database().isEmpty() ||
            dbConfig.username().isEmpty() || dbConfig.password().isEmpty()) {
            throw new IllegalStateException("Supabase database configuration is incomplete. " +
                "Please provide SUPABASE_DATABASE_HOST, SUPABASE_DATABASE_NAME, " +
                "SUPABASE_DATABASE_USERNAME, and SUPABASE_DATABASE_PASSWORD");
        }

        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl(dbConfig.getJdbcUrl());
        properties.setUsername(dbConfig.username());
        properties.setPassword(dbConfig.password());
        properties.setDriverClassName("org.postgresql.Driver");

        log.info("Configured Supabase PostgreSQL database: {}@{}:{}/{}",
            dbConfig.username(), dbConfig.host(), dbConfig.port(), dbConfig.database());

        return properties;
    }

    /**
     * Creates the Supabase data source bean.
     */
    @Bean
    @Primary
    public DataSource supabaseDataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
}
