package com.finance_control.unit.shared.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.EnvironmentConfig;
import com.finance_control.shared.service.ConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ConfigurationService Unit Tests")
class ConfigurationServiceTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private EnvironmentConfig.EnvironmentInfo environmentInfo;

    @InjectMocks
    private ConfigurationService configurationService;

    private AppProperties.Database database;
    private AppProperties.Database.Pool pool;
    private AppProperties.Security security;
    private AppProperties.Security.Jwt jwt;
    private AppProperties.Security.Cors cors;
    private AppProperties.Server server;
    private AppProperties.Logging logging;
    private AppProperties.Jpa jpa;
    private AppProperties.Jpa.Properties jpaProperties;
    private AppProperties.Flyway flyway;
    private AppProperties.Actuator actuator;
    private AppProperties.OpenApi openApi;
    private AppProperties.Pagination pagination;

    @BeforeEach
    void setUp() {
        // Setup Database
        pool = new AppProperties.Database.Pool();
        pool.setInitialSize(5);
        pool.setMaxSize(20);
        pool.setMinIdle(5);
        pool.setMaxLifetime(300000L);
        pool.setConnectionTimeout(20000L);
        pool.setIdleTimeout(300000L);
        pool.setLeakDetectionThreshold(60000L);

        database = new AppProperties.Database();
        database.setUrl("jdbc:postgresql://localhost:5432/testdb");
        database.setPort("5432");
        database.setName("testdb");
        database.setUsername("testuser");
        database.setDriverClassName("org.postgresql.Driver");
        database.setPool(pool);

        // Setup Security
        jwt = new AppProperties.Security.Jwt();
        jwt.setSecret("test-secret-key");
        jwt.setExpirationMs(86400000L);
        jwt.setRefreshExpirationMs(604800000L);
        jwt.setIssuer("finance-control");
        jwt.setAudience("finance-control-users");

        cors = new AppProperties.Security.Cors();
        cors.setAllowedOrigins(new String[]{"http://localhost:3000"});
        cors.setAllowedMethods(new String[]{"GET", "POST"});
        cors.setAllowedHeaders(new String[]{"*"});
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);

        security = new AppProperties.Security();
        security.setJwt(jwt);
        security.setCors(cors);
        security.setPublicEndpoints(new String[]{"/api/auth/**"});

        // Setup Server
        server = new AppProperties.Server();
        server.setPort(8080);
        server.setContextPath("");
        server.setServletPath("/");
        server.setMaxHttpHeaderSize(8192);
        server.setMaxHttpPostSize(2097152);
        server.setConnectionTimeout(20000);
        server.setReadTimeout(30000);
        server.setWriteTimeout(30000);

        // Setup Logging
        logging = new AppProperties.Logging();
        logging.setLevel("INFO");
        logging.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        logging.setFilePath("logs");
        logging.setFileName("finance-control.log");
        logging.setErrorFileName("finance-control-error.log");
        logging.setMaxFileSize(10485760);
        logging.setMaxHistory(30);
        logging.setQueueSize(512);
        logging.setAsync(true);

        // Setup JPA
        jpaProperties = new AppProperties.Jpa.Properties();
        jpaProperties.setHibernateFormatSql("false");
        jpaProperties.setHibernateUseSqlComments("false");
        jpaProperties.setHibernateJdbcBatchSize("20");
        jpaProperties.setHibernateOrderInserts("true");
        jpaProperties.setHibernateOrderUpdates("true");
        jpaProperties.setHibernateBatchVersionedData("true");
        jpaProperties.setHibernateJdbcFetchSize("20");
        jpaProperties.setHibernateDefaultBatchFetchSize("16");

        jpa = new AppProperties.Jpa();
        jpa.setHibernateDdlAuto("validate");
        jpa.setDialect("org.hibernate.dialect.PostgreSQLDialect");
        jpa.setShowSql(false);
        jpa.setFormatSql(false);
        jpa.setUseSqlComments(false);
        jpa.setNamingStrategy("org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        jpa.setDeferDatasourceInitialization(false);
        jpa.setProperties(jpaProperties);

        // Setup Flyway
        flyway = new AppProperties.Flyway();
        flyway.setEnabled(true);
        flyway.setLocations(new String[]{"classpath:db/migration"});
        flyway.setBaselineOnMigrate("false");
        flyway.setBaselineVersion("0");
        flyway.setValidateOnMigrate("true");
        flyway.setOutOfOrder("false");
        flyway.setCleanDisabled("true");
        flyway.setCleanOnValidationError("false");

        // Setup Actuator
        actuator = new AppProperties.Actuator();
        actuator.setEnabled(true);
        actuator.setEndpoints(new String[]{"health", "info"});
        actuator.setBasePath("/actuator");
        actuator.setExposeHealthDetails(true);
        actuator.setShowDetails(true);
        actuator.setShowComponents(true);

        // Setup OpenAPI
        openApi = new AppProperties.OpenApi();
        openApi.setTitle("Finance Control API");
        openApi.setDescription("API for managing personal finances");
        openApi.setVersion("1.0.0");
        openApi.setContactName("Finance Control Team");
        openApi.setContactEmail("support@finance-control.com");
        openApi.setContactUrl("https://github.com/LucasSantana/finance-control");
        openApi.setLicenseName("MIT License");
        openApi.setLicenseUrl("https://opensource.org/licenses/MIT");
        openApi.setServerUrl("http://localhost:8080");
        openApi.setServerDescription("Development server");

        // Setup Pagination
        pagination = new AppProperties.Pagination();
        pagination.setDefaultPageSize(10);
        pagination.setMaxPageSize(100);
        pagination.setDefaultSort("id");
        pagination.setDefaultDirection("ASC");

        // Mock AppProperties
        when(appProperties.getDatabase()).thenReturn(database);
        when(appProperties.getSecurity()).thenReturn(security);
        when(appProperties.getServer()).thenReturn(server);
        when(appProperties.getLogging()).thenReturn(logging);
        when(appProperties.getJpa()).thenReturn(jpa);
        when(appProperties.getFlyway()).thenReturn(flyway);
        when(appProperties.getActuator()).thenReturn(actuator);
        when(appProperties.getOpenApi()).thenReturn(openApi);
        when(appProperties.getPagination()).thenReturn(pagination);

        // Mock EnvironmentInfo
        when(environmentInfo.isDevelopment()).thenReturn(false);
        when(environmentInfo.isProduction()).thenReturn(false);
        when(environmentInfo.isTest()).thenReturn(true);
        when(environmentInfo.getDatabaseUrl()).thenReturn("jdbc:postgresql://localhost:5432/testdb");
        when(environmentInfo.getServerPort()).thenReturn(8080);
        when(environmentInfo.getJwtSecret()).thenReturn("test-secret-key");
    }

    @Test
    @DisplayName("getDatabaseConfig_ShouldReturnDatabaseConfiguration")
    void getDatabaseConfig_ShouldReturnDatabaseConfiguration() {
        Map<String, Object> result = configurationService.getDatabaseConfig();

        assertThat(result).isNotNull();
        assertThat(result.get("url")).isEqualTo("jdbc:postgresql://localhost:5432/testdb");
        assertThat(result.get("port")).isEqualTo("5432");
        assertThat(result.get("name")).isEqualTo("testdb");
        assertThat(result.get("username")).isEqualTo("testuser");
        assertThat(result.get("driverClassName")).isEqualTo("org.postgresql.Driver");
        assertThat(result).containsKey("pool");

        @SuppressWarnings("unchecked")
        Map<String, Object> poolConfig = (Map<String, Object>) result.get("pool");
        assertThat(poolConfig.get("initialSize")).isEqualTo(5);
        assertThat(poolConfig.get("maxSize")).isEqualTo(20);
        assertThat(poolConfig.get("minIdle")).isEqualTo(5);

        verify(appProperties).getDatabase();
    }

    @Test
    @DisplayName("getSecurityConfig_ShouldReturnSecurityConfiguration")
    void getSecurityConfig_ShouldReturnSecurityConfiguration() {
        Map<String, Object> result = configurationService.getSecurityConfig();

        assertThat(result).isNotNull();
        assertThat(result).containsKey("jwt");
        assertThat(result).containsKey("cors");
        assertThat(result).containsKey("publicEndpoints");

        @SuppressWarnings("unchecked")
        Map<String, Object> jwtConfig = (Map<String, Object>) result.get("jwt");
        assertThat(jwtConfig.get("secretConfigured")).isEqualTo(true);
        assertThat(jwtConfig.get("expirationMs")).isEqualTo(86400000L);
        assertThat(jwtConfig.get("refreshExpirationMs")).isEqualTo(604800000L);
        assertThat(jwtConfig.get("issuer")).isEqualTo("finance-control");
        assertThat(jwtConfig.get("audience")).isEqualTo("finance-control-users");

        @SuppressWarnings("unchecked")
        Map<String, Object> corsConfig = (Map<String, Object>) result.get("cors");
        assertThat(corsConfig.get("allowCredentials")).isEqualTo(true);
        assertThat(corsConfig.get("maxAge")).isEqualTo(3600L);

        verify(appProperties).getSecurity();
    }

    @Test
    @DisplayName("getServerConfig_ShouldReturnServerConfiguration")
    void getServerConfig_ShouldReturnServerConfiguration() {
        Map<String, Object> result = configurationService.getServerConfig();

        assertThat(result).isNotNull();
        assertThat(result.get("port")).isEqualTo(8080);
        assertThat(result.get("contextPath")).isEqualTo("");
        assertThat(result.get("servletPath")).isEqualTo("/");
        assertThat(result.get("maxHttpHeaderSize")).isEqualTo(8192);
        assertThat(result.get("maxHttpPostSize")).isEqualTo(2097152);
        assertThat(result.get("connectionTimeout")).isEqualTo(20000);
        assertThat(result.get("readTimeout")).isEqualTo(30000);
        assertThat(result.get("writeTimeout")).isEqualTo(30000);

        verify(appProperties).getServer();
    }

    @Test
    @DisplayName("getLoggingConfig_ShouldReturnLoggingConfiguration")
    void getLoggingConfig_ShouldReturnLoggingConfiguration() {
        Map<String, Object> result = configurationService.getLoggingConfig();

        assertThat(result).isNotNull();
        assertThat(result.get("level")).isEqualTo("INFO");
        assertThat((String) result.get("pattern")).contains("%d{yyyy-MM-dd HH:mm:ss.SSS}");
        assertThat(result.get("filePath")).isEqualTo("logs");
        assertThat(result.get("fileName")).isEqualTo("finance-control.log");
        assertThat(result.get("errorFileName")).isEqualTo("finance-control-error.log");
        assertThat(result.get("maxFileSize")).isEqualTo(10485760);
        assertThat(result.get("maxHistory")).isEqualTo(30);
        assertThat(result.get("queueSize")).isEqualTo(512);
        assertThat(result.get("async")).isEqualTo(true);

        verify(appProperties).getLogging();
    }

    @Test
    @DisplayName("getJpaConfig_ShouldReturnJpaConfiguration")
    void getJpaConfig_ShouldReturnJpaConfiguration() {
        Map<String, Object> result = configurationService.getJpaConfig();

        assertThat(result).isNotNull();
        assertThat(result.get("hibernateDdlAuto")).isEqualTo("validate");
        assertThat(result.get("dialect")).isEqualTo("org.hibernate.dialect.PostgreSQLDialect");
        assertThat(result.get("showSql")).isEqualTo(false);
        assertThat(result.get("formatSql")).isEqualTo(false);
        assertThat(result.get("useSqlComments")).isEqualTo(false);
        assertThat(result).containsKey("properties");

        @SuppressWarnings("unchecked")
        Map<String, Object> hibernateProps = (Map<String, Object>) result.get("properties");
        assertThat(hibernateProps.get("jdbcBatchSize")).isEqualTo("20");
        assertThat(hibernateProps.get("orderInserts")).isEqualTo("true");
        assertThat(hibernateProps.get("orderUpdates")).isEqualTo("true");

        verify(appProperties).getJpa();
    }

    @Test
    @DisplayName("getFlywayConfig_ShouldReturnFlywayConfiguration")
    void getFlywayConfig_ShouldReturnFlywayConfiguration() {
        Map<String, Object> result = configurationService.getFlywayConfig();

        assertThat(result).isNotNull();
        assertThat(result.get("enabled")).isEqualTo(true);
        assertThat(result.get("baselineOnMigrate")).isEqualTo("false");
        assertThat(result.get("baselineVersion")).isEqualTo("0");
        assertThat(result.get("validateOnMigrate")).isEqualTo("true");
        assertThat(result.get("outOfOrder")).isEqualTo("false");
        assertThat(result.get("cleanDisabled")).isEqualTo("true");
        assertThat(result.get("cleanOnValidationError")).isEqualTo("false");

        verify(appProperties).getFlyway();
    }

    @Test
    @DisplayName("getActuatorConfig_ShouldReturnActuatorConfiguration")
    void getActuatorConfig_ShouldReturnActuatorConfiguration() {
        Map<String, Object> result = configurationService.getActuatorConfig();

        assertThat(result).isNotNull();
        assertThat(result.get("enabled")).isEqualTo(true);
        assertThat(result.get("basePath")).isEqualTo("/actuator");
        assertThat(result.get("exposeHealthDetails")).isEqualTo(true);
        assertThat(result.get("showDetails")).isEqualTo(true);
        assertThat(result.get("showComponents")).isEqualTo(true);

        verify(appProperties).getActuator();
    }

    @Test
    @DisplayName("getOpenApiConfig_ShouldReturnOpenApiConfiguration")
    void getOpenApiConfig_ShouldReturnOpenApiConfiguration() {
        Map<String, Object> result = configurationService.getOpenApiConfig();

        assertThat(result).isNotNull();
        assertThat(result.get("title")).isEqualTo("Finance Control API");
        assertThat(result.get("description")).isEqualTo("API for managing personal finances");
        assertThat(result.get("version")).isEqualTo("1.0.0");
        assertThat(result.get("contactName")).isEqualTo("Finance Control Team");
        assertThat(result.get("contactEmail")).isEqualTo("support@finance-control.com");
        assertThat(result.get("licenseName")).isEqualTo("MIT License");
        assertThat(result.get("serverUrl")).isEqualTo("http://localhost:8080");

        verify(appProperties).getOpenApi();
    }

    @Test
    @DisplayName("getPaginationConfig_ShouldReturnPaginationConfiguration")
    void getPaginationConfig_ShouldReturnPaginationConfiguration() {
        Map<String, Object> result = configurationService.getPaginationConfig();

        assertThat(result).isNotNull();
        assertThat(result.get("defaultPageSize")).isEqualTo(10);
        assertThat(result.get("maxPageSize")).isEqualTo(100);
        assertThat(result.get("defaultSort")).isEqualTo("id");
        assertThat(result.get("defaultDirection")).isEqualTo("ASC");

        verify(appProperties).getPagination();
    }

    @Test
    @DisplayName("getEnvironmentInfo_ShouldReturnEnvironmentInfo")
    void getEnvironmentInfo_ShouldReturnEnvironmentInfo() {
        Map<String, Object> result = configurationService.getEnvironmentInfo();

        assertThat(result).isNotNull();
        assertThat(result.get("isDevelopment")).isEqualTo(false);
        assertThat(result.get("isProduction")).isEqualTo(false);
        assertThat(result.get("isTest")).isEqualTo(true);
        assertThat(result.get("databaseUrl")).isEqualTo("jdbc:postgresql://localhost:5432/testdb");
        assertThat(result.get("serverPort")).isEqualTo(8080);
        assertThat(result.get("jwtSecretConfigured")).isEqualTo(true);

        verify(environmentInfo).isDevelopment();
        verify(environmentInfo).isProduction();
        verify(environmentInfo).isTest();
        verify(environmentInfo).getDatabaseUrl();
        verify(environmentInfo).getServerPort();
        verify(environmentInfo).getJwtSecret();
    }

    @Test
    @DisplayName("getAllConfig_ShouldReturnAllConfigurations")
    void getAllConfig_ShouldReturnAllConfigurations() {
        Map<String, Object> result = configurationService.getAllConfig();

        assertThat(result).isNotNull();
        assertThat(result).containsKey("database");
        assertThat(result).containsKey("security");
        assertThat(result).containsKey("server");
        assertThat(result).containsKey("logging");
        assertThat(result).containsKey("jpa");
        assertThat(result).containsKey("flyway");
        assertThat(result).containsKey("actuator");
        assertThat(result).containsKey("openApi");
        assertThat(result).containsKey("pagination");
        assertThat(result).containsKey("environment");

        verify(appProperties, atLeastOnce()).getDatabase();
        verify(appProperties, atLeastOnce()).getSecurity();
        verify(appProperties, atLeastOnce()).getServer();
        verify(appProperties, atLeastOnce()).getLogging();
        verify(appProperties, atLeastOnce()).getJpa();
        verify(appProperties, atLeastOnce()).getFlyway();
        verify(appProperties, atLeastOnce()).getActuator();
        verify(appProperties, atLeastOnce()).getOpenApi();
        verify(appProperties, atLeastOnce()).getPagination();
    }

    @Test
    @DisplayName("logAllConfiguration_ShouldLogAllConfigs")
    void logAllConfiguration_ShouldLogAllConfigs() {
        Logger logger = (Logger) LoggerFactory.getLogger(ConfigurationService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.INFO);

        configurationService.logAllConfiguration();

        assertThat(listAppender.list).isNotEmpty();
        assertThat(listAppender.list.stream()
                .anyMatch(event -> event.getMessage().contains("Configuration Service - All Configuration")))
                .isTrue();
        assertThat(listAppender.list.stream()
                .anyMatch(event -> event.getMessage().contains("Configuration Service - End")))
                .isTrue();

        logger.detachAppender(listAppender);
    }

    @Test
    @DisplayName("getSecurityConfig_WithNullSecret_ShouldReturnFalseForSecretConfigured")
    void getSecurityConfig_WithNullSecret_ShouldReturnFalseForSecretConfigured() {
        jwt.setSecret(null);

        Map<String, Object> result = configurationService.getSecurityConfig();

        @SuppressWarnings("unchecked")
        Map<String, Object> jwtConfig = (Map<String, Object>) result.get("jwt");
        assertThat(jwtConfig.get("secretConfigured")).isEqualTo(false);
    }

    @Test
    @DisplayName("getEnvironmentInfo_WithNullJwtSecret_ShouldReturnFalseForJwtSecretConfigured")
    void getEnvironmentInfo_WithNullJwtSecret_ShouldReturnFalseForJwtSecretConfigured() {
        when(environmentInfo.getJwtSecret()).thenReturn(null);

        Map<String, Object> result = configurationService.getEnvironmentInfo();

        assertThat(result.get("jwtSecretConfigured")).isEqualTo(false);
    }
}
