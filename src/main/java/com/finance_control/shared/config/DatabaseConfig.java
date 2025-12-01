package com.finance_control.shared.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
 * Unified database configuration supporting both local PostgreSQL and Supabase PostgreSQL.
 * Automatically selects the appropriate configuration based on app.supabase.database.enabled property.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
public class DatabaseConfig {

    private final AppProperties appProperties;
    private final Environment environment;

    /**
     * Creates DataSource for local PostgreSQL (when Supabase is disabled).
     * Note: Supabase database is now the default. Set app.supabase.database.enabled=false to use local.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(value = "app.supabase.database.enabled", havingValue = "false", matchIfMissing = false)
    public DataSource localDataSource() {
        String jdbcUrl = buildLocalJdbcUrl();
        log.info("Configuring local datasource with URL: {}", jdbcUrl);
        log.info("Driver class name from properties: '{}'", appProperties.database().driverClassName());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(appProperties.database().username());
        config.setPassword(appProperties.database().password());
        config.setDriverClassName(appProperties.database().driverClassName());

        com.finance_control.shared.config.properties.DatabaseProperties.PoolProperties pool = appProperties.database().pool();
        config.setMaximumPoolSize(pool.maxSize());
        config.setMinimumIdle(pool.minIdle());
        config.setConnectionTimeout(pool.connectionTimeout());
        config.setIdleTimeout(pool.idleTimeout());
        config.setMaxLifetime(pool.maxLifetime());
        config.setLeakDetectionThreshold(pool.leakDetectionThreshold());
        config.setPoolName("FinanceControlHikariPool");
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        log.info("Database pool configured - Max: {}, Min: {}, Timeout: {}ms",
                pool.maxSize(), pool.minIdle(), pool.connectionTimeout());

        return new HikariDataSource(config);
    }

    /**
     * Creates DataSourceProperties for Supabase PostgreSQL (default).
     * Supabase database is now the default. Set app.supabase.database.enabled=false to use local.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(value = "app.supabase.database.enabled", havingValue = "true", matchIfMissing = true)
    public DataSourceProperties supabaseDataSourceProperties() {
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

        validateDatabaseConfiguration(host, port, database, username, password);
        String jdbcUrl = buildSupabaseJdbcUrl(host, port, database, sslEnabled, sslMode);

        log.info("Configuring Supabase PostgreSQL database: host={}, port={}, database={}",
            host, port, database);

        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl(jdbcUrl);
        properties.setUsername(username);
        properties.setPassword(password);
        properties.setDriverClassName("org.postgresql.Driver");

        return properties;
    }

    /**
     * Creates DataSource for Supabase (default).
     * Supabase database is now the default. Set app.supabase.database.enabled=false to use local.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(value = "app.supabase.database.enabled", havingValue = "true", matchIfMissing = true)
    public DataSource supabaseDataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    /**
     * Unified EntityManagerFactory for both local and Supabase databases.
     */
    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.finance_control");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        boolean isSupabase = appProperties.supabaseDatabaseEnabled();
        boolean isH2 = !isSupabase && appProperties.database().url().startsWith("jdbc:h2:");

        if (isSupabase || (!isH2 && !isSupabase)) {
            jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            jpaProperties.setProperty("hibernate.hbm2ddl.auto", "validate");
        } else {
            jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            jpaProperties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        }

        jpaProperties.setProperty("hibernate.show_sql", "false");
        jpaProperties.setProperty("hibernate.format_sql", "false");

        factory.setJpaProperties(jpaProperties);
        log.info("Created EntityManagerFactory for {} database",
            isSupabase ? "Supabase PostgreSQL" : (isH2 ? "H2" : "PostgreSQL"));
        return factory;
    }

    /**
     * Unified TransactionManager for both local and Supabase databases.
     */
    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

    private String buildLocalJdbcUrl() {
        String url = appProperties.database().url();
        String port = appProperties.database().port();
        String dbName = appProperties.database().name();

        if (url.startsWith("jdbc:h2:")) {
            return url;
        }

        if (url.contains("/" + dbName)) {
            return url + "?sslmode=disable";
        }

        if (url.matches(".*:\\d+$")) {
            return url + "/" + dbName + "?sslmode=disable";
        }

        if (url.matches("jdbc:postgresql://[^:]+$")) {
            return url + ":" + port + "/" + dbName + "?sslmode=disable";
        }

        return url + ":" + port + "/" + dbName + "?sslmode=disable";
    }

    private String buildSupabaseJdbcUrl(String host, int port, String database, boolean sslEnabled, String sslMode) {
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
}
