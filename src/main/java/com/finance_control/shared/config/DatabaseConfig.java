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
                appProperties.getDatabase().getUrl() + ":" +
                appProperties.getDatabase().getPort() + "/" +
                appProperties.getDatabase().getName());

        HikariConfig config = new HikariConfig();

        // Basic datasource configuration
        config.setJdbcUrl(buildJdbcUrl());
        config.setUsername(appProperties.getDatabase().getUsername());
        config.setPassword(appProperties.getDatabase().getPassword());
        config.setDriverClassName(appProperties.getDatabase().getDriverClassName());

        // Connection pool configuration
        AppProperties.Database.Pool pool = appProperties.getDatabase().getPool();
        config.setMaximumPoolSize(pool.getMaxSize());
        config.setMinimumIdle(pool.getMinIdle());
        config.setConnectionTimeout(pool.getConnectionTimeout());
        config.setIdleTimeout(pool.getIdleTimeout());
        config.setMaxLifetime(pool.getMaxLifetime());
        config.setLeakDetectionThreshold(pool.getLeakDetectionThreshold());

        // Connection pool name for monitoring
        config.setPoolName("FinanceControlHikariPool");

        // Connection validation
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        log.info("Database pool configured - Max: {}, Min: {}, Timeout: {}ms",
                pool.getMaxSize(), pool.getMinIdle(), pool.getConnectionTimeout());

        return new HikariDataSource(config);
    }


    private String buildJdbcUrl() {
        String url = appProperties.getDatabase().getUrl();
        // If URL already contains the database name, use it as-is
        if (url.contains("/" + appProperties.getDatabase().getName())) {
            return url + "?sslmode=disable";
        }
        // If URL already contains a port, don't add another one
        if (url.contains(":")) {
            return url + "/" + appProperties.getDatabase().getName() + "?sslmode=disable";
        }
        // Otherwise, build the URL from components
        return url + ":" +
               appProperties.getDatabase().getPort() + "/" +
               appProperties.getDatabase().getName() +
               "?sslmode=disable";
    }

}
