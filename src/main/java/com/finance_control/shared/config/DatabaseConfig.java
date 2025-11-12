package com.finance_control.shared.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Database configuration that uses environment variables through AppProperties.
 * Configures the datasource and connection pool with settings from environment variables.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
public class DatabaseConfig {

    private final AppProperties appProperties;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring datasource with URL: {}",
                appProperties.database().url() + ":" +
                appProperties.database().port() + "/" +
                appProperties.database().name());

        HikariConfig config = new HikariConfig();

        // Basic datasource configuration
        config.setJdbcUrl(buildJdbcUrl());
        config.setUsername(appProperties.database().username());
        config.setPassword(appProperties.database().password());
        config.setDriverClassName(appProperties.database().driverClassName());

        // Connection pool configuration
        AppProperties.Pool pool = appProperties.database().pool();
        config.setMaximumPoolSize(pool.maxSize());
        config.setMinimumIdle(pool.minIdle());
        config.setConnectionTimeout(pool.connectionTimeout());
        config.setIdleTimeout(pool.idleTimeout());
        config.setMaxLifetime(pool.maxLifetime());
        config.setLeakDetectionThreshold(pool.leakDetectionThreshold());

        // Connection pool name for monitoring
        config.setPoolName("FinanceControlHikariPool");

        // Connection validation
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        log.info("Database pool configured - Max: {}, Min: {}, Timeout: {}ms",
                pool.maxSize(), pool.minIdle(), pool.connectionTimeout());

        return new HikariDataSource(config);
    }


    private String buildJdbcUrl() {
        String url = appProperties.database().url();
        String port = appProperties.database().port();
        String dbName = appProperties.database().name();

        // For H2 databases, use the URL as-is (it already contains all necessary parameters)
        if (url.startsWith("jdbc:h2:")) {
            return url;
        }

        // For PostgreSQL databases, build the URL with sslmode parameter
        // If URL already contains the database name, use it as-is
        if (url.contains("/" + dbName)) {
            return url + "?sslmode=disable";
        }

        // If URL already contains a port (like jdbc:postgresql://host:5432), add database name
        if (url.matches(".*:\\d+$")) {
            return url + "/" + dbName + "?sslmode=disable";
        }

        // If URL is just jdbc:postgresql://host, add port and database name
        if (url.matches("jdbc:postgresql://[^:]+$")) {
            return url + ":" + port + "/" + dbName + "?sslmode=disable";
        }

        // Otherwise, build the URL from components
        return url + ":" + port + "/" + dbName + "?sslmode=disable";
    }

}
