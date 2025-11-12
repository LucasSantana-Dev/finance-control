package com.finance_control.shared.service;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.EnvironmentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that demonstrates how to use environment variables and configuration properties.
 * This service provides utility methods for accessing configuration values throughout the application.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private final AppProperties appProperties;
    private final EnvironmentConfig.EnvironmentInfo environmentInfo;

    /**
     * Gets database configuration information.
     *
     * @return Map containing database configuration
     */
    public Map<String, Object> getDatabaseConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Database db = appProperties.database();

        config.put("url", db.url());
        config.put("port", db.port());
        config.put("name", db.name());
        config.put("username", db.username());
        config.put("driverClassName", db.driverClassName());

        // Connection pool configuration
        AppProperties.Pool pool = db.pool();
        Map<String, Object> poolConfig = new HashMap<>();
        poolConfig.put("initialSize", pool.initialSize());
        poolConfig.put("maxSize", pool.maxSize());
        poolConfig.put("minIdle", pool.minIdle());
        poolConfig.put("maxLifetime", pool.maxLifetime());
        poolConfig.put("connectionTimeout", pool.connectionTimeout());
        poolConfig.put("idleTimeout", pool.idleTimeout());
        poolConfig.put("leakDetectionThreshold", pool.leakDetectionThreshold());

        config.put("pool", poolConfig);

        return config;
    }

    /**
     * Gets security configuration information.
     *
     * @return Map containing security configuration
     */
    public Map<String, Object> getSecurityConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Security security = appProperties.security();

        // JWT configuration
        AppProperties.Jwt jwt = security.jwt();
        Map<String, Object> jwtConfig = new HashMap<>();
        jwtConfig.put("secretConfigured", jwt.secret() != null);
        jwtConfig.put("expirationMs", jwt.expirationMs());
        jwtConfig.put("refreshExpirationMs", jwt.refreshExpirationMs());
        jwtConfig.put("issuer", jwt.issuer());
        jwtConfig.put("audience", jwt.audience());

        config.put("jwt", jwtConfig);

        // CORS configuration
        AppProperties.Cors cors = security.cors();
        Map<String, Object> corsConfig = new HashMap<>();
        corsConfig.put("allowedOrigins", cors.allowedOrigins());
        corsConfig.put("allowedMethods", cors.allowedMethods());
        corsConfig.put("allowedHeaders", cors.allowedHeaders());
        corsConfig.put("allowCredentials", cors.allowCredentials());
        corsConfig.put("maxAge", cors.maxAge());

        config.put("cors", corsConfig);
        config.put("publicEndpoints", security.publicEndpoints());

        return config;
    }

    /**
     * Gets server configuration information.
     *
     * @return Map containing server configuration
     */
    public Map<String, Object> getServerConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Server server = appProperties.server();

        config.put("port", server.port());
        config.put("contextPath", server.contextPath());
        config.put("servletPath", server.servletPath());
        config.put("maxHttpHeaderSize", server.maxHttpHeaderSize());
        config.put("maxHttpPostSize", server.maxHttpPostSize());
        config.put("connectionTimeout", server.connectionTimeout());
        config.put("readTimeout", server.readTimeout());
        config.put("writeTimeout", server.writeTimeout());

        return config;
    }

    /**
     * Gets logging configuration information.
     *
     * @return Map containing logging configuration
     */
    public Map<String, Object> getLoggingConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Logging logging = appProperties.logging();

        config.put("level", logging.level());
        config.put("pattern", logging.pattern());
        config.put("filePath", logging.filePath());
        config.put("fileName", logging.fileName());
        config.put("errorFileName", logging.errorFileName());
        config.put("maxFileSize", logging.maxFileSize());
        config.put("maxHistory", logging.maxHistory());
        config.put("queueSize", logging.queueSize());
        config.put("async", logging.async());

        return config;
    }

    /**
     * Gets JPA configuration information.
     *
     * @return Map containing JPA configuration
     */
    public Map<String, Object> getJpaConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Jpa jpa = appProperties.jpa();

        config.put("hibernateDdlAuto", jpa.hibernateDdlAuto());
        config.put("dialect", jpa.dialect());
        config.put("showSql", jpa.showSql());
        config.put("formatSql", jpa.formatSql());
        config.put("useSqlComments", jpa.useSqlComments());
        config.put("deferDatasourceInitialization", jpa.deferDatasourceInitialization());
        config.put("namingStrategy", jpa.namingStrategy());

        // Hibernate properties
        AppProperties.Properties props = jpa.properties();
        Map<String, Object> hibernateProps = new HashMap<>();
        hibernateProps.put("formatSql", props.hibernateFormatSql());
        hibernateProps.put("useSqlComments", props.hibernateUseSqlComments());
        hibernateProps.put("jdbcBatchSize", props.hibernateJdbcBatchSize());
        hibernateProps.put("orderInserts", props.hibernateOrderInserts());
        hibernateProps.put("orderUpdates", props.hibernateOrderUpdates());
        hibernateProps.put("batchVersionedData", props.hibernateBatchVersionedData());
        hibernateProps.put("jdbcFetchSize", props.hibernateJdbcFetchSize());
        hibernateProps.put("defaultBatchFetchSize", props.hibernateDefaultBatchFetchSize());

        config.put("properties", hibernateProps);

        return config;
    }

    /**
     * Gets Flyway configuration information.
     *
     * @return Map containing Flyway configuration
     */
    public Map<String, Object> getFlywayConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Flyway flyway = appProperties.flyway();

        config.put("enabled", flyway.enabled());
        config.put("locations", flyway.locations());
        config.put("baselineOnMigrate", flyway.baselineOnMigrate());
        config.put("baselineVersion", flyway.baselineVersion());
        config.put("validateOnMigrate", flyway.validateOnMigrate());
        config.put("outOfOrder", flyway.outOfOrder());
        config.put("cleanDisabled", flyway.cleanDisabled());
        config.put("cleanOnValidationError", flyway.cleanOnValidationError());

        return config;
    }

    /**
     * Gets Actuator configuration information.
     *
     * @return Map containing Actuator configuration
     */
    public Map<String, Object> getActuatorConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Actuator actuator = appProperties.actuator();

        config.put("enabled", actuator.enabled());
        config.put("endpoints", actuator.endpoints());
        config.put("basePath", actuator.basePath());
        config.put("exposeHealthDetails", actuator.exposeHealthDetails());
        config.put("showDetails", actuator.showDetails());
        config.put("showComponents", actuator.showComponents());

        return config;
    }

    /**
     * Gets OpenAPI configuration information.
     *
     * @return Map containing OpenAPI configuration
     */
    public Map<String, Object> getOpenApiConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.OpenApi openApi = appProperties.openApi();

        config.put("title", openApi.title());
        config.put("description", openApi.description());
        config.put("version", openApi.version());
        config.put("contactName", openApi.contactName());
        config.put("contactEmail", openApi.contactEmail());
        config.put("contactUrl", openApi.contactUrl());
        config.put("licenseName", openApi.licenseName());
        config.put("licenseUrl", openApi.licenseUrl());
        config.put("serverUrl", openApi.serverUrl());
        config.put("serverDescription", openApi.serverDescription());

        return config;
    }

    /**
     * Gets pagination configuration information.
     *
     * @return Map containing pagination configuration
     */
    public Map<String, Object> getPaginationConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Pagination pagination = appProperties.pagination();

        config.put("defaultPageSize", pagination.defaultPageSize());
        config.put("maxPageSize", pagination.maxPageSize());
        config.put("defaultSort", pagination.defaultSort());
        config.put("defaultDirection", pagination.defaultDirection());

        return config;
    }

    /**
     * Gets environment information.
     *
     * @return Map containing environment information
     */
    public Map<String, Object> getEnvironmentInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("isDevelopment", environmentInfo.isDevelopment());
        info.put("isProduction", environmentInfo.isProduction());
        info.put("isTest", environmentInfo.isTest());
        info.put("databaseUrl", environmentInfo.getDatabaseUrl());
        info.put("serverPort", environmentInfo.getServerPort());
        info.put("jwtSecretConfigured", environmentInfo.getJwtSecret() != null);

        return info;
    }

    /**
     * Gets all configuration information.
     *
     * @return Map containing all configuration
     */
    public Map<String, Object> getAllConfig() {
        Map<String, Object> allConfig = new HashMap<>();

        allConfig.put("database", getDatabaseConfig());
        allConfig.put("security", getSecurityConfig());
        allConfig.put("server", getServerConfig());
        allConfig.put("logging", getLoggingConfig());
        allConfig.put("jpa", getJpaConfig());
        allConfig.put("flyway", getFlywayConfig());
        allConfig.put("actuator", getActuatorConfig());
        allConfig.put("openApi", getOpenApiConfig());
        allConfig.put("pagination", getPaginationConfig());
        allConfig.put("environment", getEnvironmentInfo());

        return allConfig;
    }

    /**
     * Logs all configuration information for debugging purposes.
     */
    public void logAllConfiguration() {
        log.info("=== Configuration Service - All Configuration ===");

        Map<String, Object> allConfig = getAllConfig();
        allConfig.forEach((category, config) -> log.info("{}: {}", category, config));

        log.info("=== Configuration Service - End ===");
    }
}
