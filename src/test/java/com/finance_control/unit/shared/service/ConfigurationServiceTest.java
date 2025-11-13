package com.finance_control.unit.shared.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.EnvironmentConfig;
import com.finance_control.shared.service.ConfigurationService;
import java.util.List;
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

    private AppProperties appPropertiesInstance;

    @BeforeEach
    void setUp() {
        // Create AppProperties instance using constructor binding
        appPropertiesInstance = new AppProperties(
            new AppProperties.Database(
                "jdbc:postgresql://localhost:5432/testdb",
                "testuser",
                "testpass",
                "org.postgresql.Driver",
                "5432",
                "testdb",
                new AppProperties.Pool(5, 20, 5, 300000, 20000, 300000, 60000)
            ),
            new AppProperties.Security(
                new AppProperties.Jwt(
                    "test-secret-key",
                    86400000L,
                    604800000L,
                    "finance-control",
                    "finance-control-users"
                ),
                new AppProperties.Cors(
                    List.of("http://localhost:3000"),
                    List.of("GET", "POST"),
                    List.of("*"),
                    true,
                    3600
                ),
                List.of("/api/auth/**")
            ),
            new AppProperties.Server(
                8080, "", "/", 8192, 2097152, 20000, 30000, 30000
            ),
            new AppProperties.Logging(
                "INFO",
                "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n",
                "logs",
                "finance-control.log",
                "finance-control-error.log",
                10485760,
                30,
                512,
                true
            ),
            new AppProperties.Jpa(
                "validate",
                "org.hibernate.dialect.PostgreSQLDialect",
                false,
                false,
                false,
                "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl",
                false,
                new AppProperties.Properties(
                    "false", "false", "20", "true", "true", "true", "20", "16"
                )
            ),
            new AppProperties.Flyway(
                true,
                List.of("classpath:db/migration"),
                "false",
                "0",
                "true",
                "false",
                "true",
                "false"
            ),
            new AppProperties.Actuator(
                true,
                List.of("health", "info"),
                "/actuator",
                true,
                true,
                true
            ),
            new AppProperties.OpenApi(
                "Finance Control API",
                "API for managing personal finances",
                "1.0.0",
                "Finance Control Team",
                "support@finance-control.com",
                "https://github.com/LucasSantana/finance-control",
                "MIT License",
                "https://opensource.org/licenses/MIT",
                "http://localhost:8080",
                "Development server"
            ),
            new AppProperties.Pagination(10, 100, "id", "ASC"),
            new AppProperties.Redis("localhost", 6379, "", 0, 2000, new AppProperties.RedisPool(8, 8, 0, -1)),
            new AppProperties.Cache(true, 900000, 300000, 1800000),
            new AppProperties.RateLimit(true, 100, 200, 60),
            new AppProperties.Ai(),
            new AppProperties.Supabase(false, "", "", "", "", new AppProperties.SupabaseDatabase(), new AppProperties.Storage(), new AppProperties.Realtime(false, List.of("transactions", "dashboard", "goals"))),
            new AppProperties.Monitoring(true, new AppProperties.Sentry(true, "", "dev", "1.0.0", 0.1, 0.1, false, true, true), new AppProperties.HealthCheck(30, true))
        );

        // Mock AppProperties using record accessors
        when(appProperties.database()).thenReturn(appPropertiesInstance.database());
        when(appProperties.security()).thenReturn(appPropertiesInstance.security());
        when(appProperties.server()).thenReturn(appPropertiesInstance.server());
        when(appProperties.logging()).thenReturn(appPropertiesInstance.logging());
        when(appProperties.jpa()).thenReturn(appPropertiesInstance.jpa());
        when(appProperties.flyway()).thenReturn(appPropertiesInstance.flyway());
        when(appProperties.actuator()).thenReturn(appPropertiesInstance.actuator());
        when(appProperties.openApi()).thenReturn(appPropertiesInstance.openApi());
        when(appProperties.pagination()).thenReturn(appPropertiesInstance.pagination());
        when(appProperties.redis()).thenReturn(appPropertiesInstance.redis());
        when(appProperties.cache()).thenReturn(appPropertiesInstance.cache());
        when(appProperties.rateLimit()).thenReturn(appPropertiesInstance.rateLimit());
        when(appProperties.ai()).thenReturn(appPropertiesInstance.ai());
        when(appProperties.supabase()).thenReturn(appPropertiesInstance.supabase());
        when(appProperties.monitoring()).thenReturn(appPropertiesInstance.monitoring());

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

        verify(appProperties).database();
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

        verify(appProperties).security();
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

        verify(appProperties).server();
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

        verify(appProperties).logging();
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

        verify(appProperties).jpa();
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

        verify(appProperties).flyway();
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

        verify(appProperties).actuator();
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

        verify(appProperties).openApi();
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

        verify(appProperties).pagination();
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

        verify(appProperties, atLeastOnce()).database();
        verify(appProperties, atLeastOnce()).security();
        verify(appProperties, atLeastOnce()).server();
        verify(appProperties, atLeastOnce()).logging();
        verify(appProperties, atLeastOnce()).jpa();
        verify(appProperties, atLeastOnce()).flyway();
        verify(appProperties, atLeastOnce()).actuator();
        verify(appProperties, atLeastOnce()).openApi();
        verify(appProperties, atLeastOnce()).pagination();
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
        // Create a new JWT with null secret and create new security config
        AppProperties.Jwt nullSecretJwt = new AppProperties.Jwt(
            null, 86400000L, 604800000L, "finance-control", "finance-control-users"
        );
        AppProperties.Security nullSecretSecurity = new AppProperties.Security(
            nullSecretJwt,
            appPropertiesInstance.security().cors(),
            appPropertiesInstance.security().publicEndpoints()
        );

        // Create new AppProperties with the null secret security
        AppProperties nullSecretAppProperties = new AppProperties(
            appPropertiesInstance.database(),
            nullSecretSecurity,
            appPropertiesInstance.server(),
            appPropertiesInstance.logging(),
            appPropertiesInstance.jpa(),
            appPropertiesInstance.flyway(),
            appPropertiesInstance.actuator(),
            appPropertiesInstance.openApi(),
            appPropertiesInstance.pagination(),
            appPropertiesInstance.redis(),
            appPropertiesInstance.cache(),
            appPropertiesInstance.rateLimit(),
            appPropertiesInstance.ai(),
            appPropertiesInstance.supabase(),
            appPropertiesInstance.monitoring()
        );

        configurationService = new ConfigurationService(nullSecretAppProperties, environmentInfo);

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
