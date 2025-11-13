package com.finance_control.shared.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration that uses environment variables through AppProperties.
 * Configures the datasource and connection pool with settings from environment variables.
 * Only loads when Supabase database is disabled (app.supabase.database.enabled != true).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
@ConditionalOnProperty(value = "app.supabase.database.enabled", havingValue = "false", matchIfMissing = true)
public class DatabaseConfig {

    private final AppProperties appProperties;

    @Bean
    @Primary
    public DataSource dataSource() {
        String jdbcUrl = buildJdbcUrl();
        log.info("Configuring datasource with URL: {}", jdbcUrl);

        HikariConfig config = new HikariConfig();

                // Basic datasource configuration
                config.setJdbcUrl(jdbcUrl);
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

    /**
     * Creates the EntityManagerFactory bean for JPA when using default database.
     */
    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.finance_control");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        String databasePlatform = appProperties.database().url().startsWith("jdbc:h2:")
            ? "org.hibernate.dialect.H2Dialect"
            : "org.hibernate.dialect.PostgreSQLDialect";
        // Use Spring property for ddl-auto, default to create-drop for H2, validate for PostgreSQL
        String ddlAuto = appProperties.database().url().startsWith("jdbc:h2:")
            ? "create-drop"
            : "validate";
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        jpaProperties.setProperty("hibernate.dialect", databasePlatform);
        jpaProperties.setProperty("hibernate.show_sql", "false");
        jpaProperties.setProperty("hibernate.format_sql", "false");

        factory.setJpaProperties(jpaProperties);

        log.info("Created EntityManagerFactory for database: {}", databasePlatform);
        return factory;
    }

    /**
     * Creates the TransactionManager bean for JPA when using default database.
     */
    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

}
