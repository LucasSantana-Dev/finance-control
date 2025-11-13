package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuration for Supabase PostgreSQL database integration.
 * This allows the application to use Supabase's PostgreSQL database instead of a local one.
 * Only loads when app.supabase.database.enabled=true
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.database.enabled", havingValue = "true", matchIfMissing = false)
public class SupabaseDatabaseConfig {

    private final Environment environment;

    /**
     * Creates a DataSourceProperties bean configured for Supabase PostgreSQL.
     * This overrides the default data source properties when Supabase database is enabled.
     */
    @Bean
    @Primary
    public DataSourceProperties dataSourceProperties() {
        // Read directly from environment to ensure we get values from .env file
        String host = environment.getProperty("SUPABASE_DATABASE_HOST",
            environment.getProperty("app.supabase.database.host", ""));
        int port = environment.getProperty("SUPABASE_DATABASE_PORT", Integer.class,
            environment.getProperty("app.supabase.database.port", Integer.class, 5432));
        String database = environment.getProperty("SUPABASE_DATABASE_NAME",
            environment.getProperty("app.supabase.database.database", ""));
        String username = environment.getProperty("SUPABASE_DATABASE_USERNAME",
            environment.getProperty("app.supabase.database.username", ""));
        String password = environment.getProperty("SUPABASE_DATABASE_PASSWORD",
            environment.getProperty("app.supabase.database.password", ""));
        boolean sslEnabled = environment.getProperty("SUPABASE_DATABASE_SSL_ENABLED", Boolean.class,
            environment.getProperty("app.supabase.database.ssl-enabled", Boolean.class, true));
        String sslMode = environment.getProperty("SUPABASE_DATABASE_SSL_MODE",
            environment.getProperty("app.supabase.database.ssl-mode", "require"));

        log.info("Supabase database properties from environment - host: {}, port: {}, database: {}, username: {}",
            host, port, database, username != null && !username.isEmpty() ? "***" : "empty");

        validateDatabaseConfiguration(host, port, database, username, password);

        String jdbcUrl = buildJdbcUrl(host, port, database, sslEnabled, sslMode);

        log.info("Configuring Supabase PostgreSQL database: host={}, port={}, database={}",
            host, port, database);

        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl(jdbcUrl);
        properties.setUsername(username);
        properties.setPassword(password);
        properties.setDriverClassName("org.postgresql.Driver");

        log.info("Configured Supabase PostgreSQL database: {}@{}:{}/{}",
            username, host, port, database);

        return properties;
    }

    private String buildJdbcUrl(String host, int port, String database, boolean sslEnabled, String sslMode) {
        StringBuilder url = new StringBuilder("jdbc:postgresql://");
        url.append(host).append(":").append(port).append("/").append(database);

        if (sslEnabled) {
            url.append("?sslmode=").append(sslMode);
        }

        return url.toString();
    }

    private void validateDatabaseConfiguration(String host, int port, String database, String username, String password) {
        if (!StringUtils.hasText(host)) {
            throw new IllegalStateException("Supabase database host is required but not configured");
        }
        if (!StringUtils.hasText(database)) {
            throw new IllegalStateException("Supabase database name is required but not configured");
        }
        if (!StringUtils.hasText(username)) {
            throw new IllegalStateException("Supabase database username is required but not configured");
        }
        if (!StringUtils.hasText(password)) {
            throw new IllegalStateException("Supabase database password is required but not configured");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalStateException("Supabase database port must be between 1 and 65535");
        }
    }

    /**
     * Creates the Supabase data source bean.
     */
    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    /**
     * Creates the EntityManagerFactory bean for JPA when using Supabase database.
     */
    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.finance_control");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "validate");
        jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.setProperty("hibernate.show_sql", "false");
        jpaProperties.setProperty("hibernate.format_sql", "false");

        factory.setJpaProperties(jpaProperties);

        log.info("Created EntityManagerFactory for Supabase PostgreSQL database");
        return factory;
    }

    /**
     * Creates the TransactionManager bean for JPA when using Supabase database.
     */
    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

}
