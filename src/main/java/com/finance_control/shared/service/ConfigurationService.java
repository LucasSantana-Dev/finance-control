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
        AppProperties.Database db = appProperties.getDatabase();

        config.put("url", db.getUrl());
        config.put("port", db.getPort());
        config.put("name", db.getName());
        config.put("username", db.getUsername());
        config.put("driverClassName", db.getDriverClassName());

        // Connection pool configuration
        AppProperties.Database.Pool pool = db.getPool();
        Map<String, Object> poolConfig = new HashMap<>();
        poolConfig.put("initialSize", pool.getInitialSize());
        poolConfig.put("maxSize", pool.getMaxSize());
        poolConfig.put("minIdle", pool.getMinIdle());
        poolConfig.put("maxLifetime", pool.getMaxLifetime());
        poolConfig.put("connectionTimeout", pool.getConnectionTimeout());
        poolConfig.put("idleTimeout", pool.getIdleTimeout());
        poolConfig.put("leakDetectionThreshold", pool.getLeakDetectionThreshold());

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
        AppProperties.Security security = appProperties.getSecurity();

        // JWT configuration
        AppProperties.Security.Jwt jwt = security.getJwt();
        Map<String, Object> jwtConfig = new HashMap<>();
        jwtConfig.put("secretConfigured", jwt.getSecret() != null);
        jwtConfig.put("expirationMs", jwt.getExpirationMs());
        jwtConfig.put("refreshExpirationMs", jwt.getRefreshExpirationMs());
        jwtConfig.put("issuer", jwt.getIssuer());
        jwtConfig.put("audience", jwt.getAudience());

        config.put("jwt", jwtConfig);

        // CORS configuration
        AppProperties.Security.Cors cors = security.getCors();
        Map<String, Object> corsConfig = new HashMap<>();
        corsConfig.put("allowedOrigins", cors.getAllowedOrigins());
        corsConfig.put("allowedMethods", cors.getAllowedMethods());
        corsConfig.put("allowedHeaders", cors.getAllowedHeaders());
        corsConfig.put("allowCredentials", cors.isAllowCredentials());
        corsConfig.put("maxAge", cors.getMaxAge());

        config.put("cors", corsConfig);
        config.put("publicEndpoints", security.getPublicEndpoints());

        return config;
    }

    /**
     * Gets server configuration information.
     *
     * @return Map containing server configuration
     */
    public Map<String, Object> getServerConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Server server = appProperties.getServer();

        config.put("port", server.getPort());
        config.put("contextPath", server.getContextPath());
        config.put("servletPath", server.getServletPath());
        config.put("maxHttpHeaderSize", server.getMaxHttpHeaderSize());
        config.put("maxHttpPostSize", server.getMaxHttpPostSize());
        config.put("connectionTimeout", server.getConnectionTimeout());
        config.put("readTimeout", server.getReadTimeout());
        config.put("writeTimeout", server.getWriteTimeout());

        return config;
    }

    /**
     * Gets logging configuration information.
     *
     * @return Map containing logging configuration
     */
    public Map<String, Object> getLoggingConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Logging logging = appProperties.getLogging();

        config.put("level", logging.getLevel());
        config.put("pattern", logging.getPattern());
        config.put("filePath", logging.getFilePath());
        config.put("fileName", logging.getFileName());
        config.put("errorFileName", logging.getErrorFileName());
        config.put("maxFileSize", logging.getMaxFileSize());
        config.put("maxHistory", logging.getMaxHistory());
        config.put("queueSize", logging.getQueueSize());
        config.put("async", logging.isAsync());

        return config;
    }

    /**
     * Gets JPA configuration information.
     *
     * @return Map containing JPA configuration
     */
    public Map<String, Object> getJpaConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Jpa jpa = appProperties.getJpa();

        config.put("hibernateDdlAuto", jpa.getHibernateDdlAuto());
        config.put("dialect", jpa.getDialect());
        config.put("showSql", jpa.isShowSql());
        config.put("formatSql", jpa.isFormatSql());
        config.put("useSqlComments", jpa.isUseSqlComments());
        config.put("deferDatasourceInitialization", jpa.isDeferDatasourceInitialization());
        config.put("namingStrategy", jpa.getNamingStrategy());

        // Hibernate properties
        AppProperties.Jpa.Properties props = jpa.getProperties();
        Map<String, Object> hibernateProps = new HashMap<>();
        hibernateProps.put("formatSql", props.getHibernateFormatSql());
        hibernateProps.put("useSqlComments", props.getHibernateUseSqlComments());
        hibernateProps.put("jdbcBatchSize", props.getHibernateJdbcBatchSize());
        hibernateProps.put("orderInserts", props.getHibernateOrderInserts());
        hibernateProps.put("orderUpdates", props.getHibernateOrderUpdates());
        hibernateProps.put("batchVersionedData", props.getHibernateBatchVersionedData());
        hibernateProps.put("jdbcFetchSize", props.getHibernateJdbcFetchSize());
        hibernateProps.put("defaultBatchFetchSize", props.getHibernateDefaultBatchFetchSize());

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
        AppProperties.Flyway flyway = appProperties.getFlyway();

        config.put("enabled", flyway.isEnabled());
        config.put("locations", flyway.getLocations());
        config.put("baselineOnMigrate", flyway.getBaselineOnMigrate());
        config.put("baselineVersion", flyway.getBaselineVersion());
        config.put("validateOnMigrate", flyway.getValidateOnMigrate());
        config.put("outOfOrder", flyway.getOutOfOrder());
        config.put("cleanDisabled", flyway.getCleanDisabled());
        config.put("cleanOnValidationError", flyway.getCleanOnValidationError());

        return config;
    }

    /**
     * Gets Actuator configuration information.
     *
     * @return Map containing Actuator configuration
     */
    public Map<String, Object> getActuatorConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Actuator actuator = appProperties.getActuator();

        config.put("enabled", actuator.isEnabled());
        config.put("endpoints", actuator.getEndpoints());
        config.put("basePath", actuator.getBasePath());
        config.put("exposeHealthDetails", actuator.isExposeHealthDetails());
        config.put("showDetails", actuator.isShowDetails());
        config.put("showComponents", actuator.isShowComponents());

        return config;
    }

    /**
     * Gets OpenAPI configuration information.
     *
     * @return Map containing OpenAPI configuration
     */
    public Map<String, Object> getOpenApiConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.OpenApi openApi = appProperties.getOpenApi();

        config.put("title", openApi.getTitle());
        config.put("description", openApi.getDescription());
        config.put("version", openApi.getVersion());
        config.put("contactName", openApi.getContactName());
        config.put("contactEmail", openApi.getContactEmail());
        config.put("contactUrl", openApi.getContactUrl());
        config.put("licenseName", openApi.getLicenseName());
        config.put("licenseUrl", openApi.getLicenseUrl());
        config.put("serverUrl", openApi.getServerUrl());
        config.put("serverDescription", openApi.getServerDescription());

        return config;
    }

    /**
     * Gets pagination configuration information.
     *
     * @return Map containing pagination configuration
     */
    public Map<String, Object> getPaginationConfig() {
        Map<String, Object> config = new HashMap<>();
        AppProperties.Pagination pagination = appProperties.getPagination();

        config.put("defaultPageSize", pagination.getDefaultPageSize());
        config.put("maxPageSize", pagination.getMaxPageSize());
        config.put("defaultSort", pagination.getDefaultSort());
        config.put("defaultDirection", pagination.getDefaultDirection());

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
