package com.finance_control.shared.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application properties configuration that binds environment variables to typed properties.
 * This centralizes all application configuration and provides type-safe access to environment variables.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Database database = new Database();
    private Security security = new Security();
    private Server server = new Server();
    private Logging logging = new Logging();
    private Jpa jpa = new Jpa();
    private Flyway flyway = new Flyway();
    private Actuator actuator = new Actuator();
    private OpenApi openApi = new OpenApi();
    private Pagination pagination = new Pagination();
    private Redis redis = new Redis();
    private Cache cache = new Cache();
    private RateLimit rateLimit = new RateLimit();
    private Monitoring monitoring = new Monitoring();

    @Data
    public static class Database {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private String port;
        private String name;
        private Pool pool = new Pool();

        @Data
        public static class Pool {
            private int initialSize = 5;
            private int maxSize = 20;
            private int minIdle = 5;
            private long maxLifetime = 300000;
            private long connectionTimeout = 20000;
            private long idleTimeout = 300000;
            private long leakDetectionThreshold = 60000;
        }
    }

    @Getter
    @Setter
    public static class Security {
        private Jwt jwt = new Jwt();
        private Cors cors = new Cors();
        private String[] publicEndpoints = {"/api/auth/**", "/api/users", "/api/monitoring/**", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**"};

        @Data
        public static class Jwt {
            private String secret;
            private long expirationMs = 86400000; // 24 hours
            private long refreshExpirationMs = 604800000; // 7 days
            private String issuer = "finance-control";
            private String audience = "finance-control-users";
        }

        @Data
        public static class Cors {
            private String[] allowedOrigins = {"http://localhost:3000", "http://localhost:8080"};
            private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
            private String[] allowedHeaders = {"*"};
            private boolean allowCredentials = true;
            private long maxAge = 3600;
        }
    }

    @Data
    public static class Server {
        private int port = 8080;
        private String contextPath = "";
        private String servletPath = "/";
        private int maxHttpHeaderSize = 8192;
        private int maxHttpPostSize = 2097152;
        private int connectionTimeout = 20000;
        private int readTimeout = 30000;
        private int writeTimeout = 30000;
    }

    @Data
    public static class Logging {
        private String level = "INFO";
        private String pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
        private String filePath = "logs";
        private String fileName = "finance-control.log";
        private String errorFileName = "finance-control-error.log";
        private int maxFileSize = 10485760; // 10MB
        private int maxHistory = 30;
        private int queueSize = 512;
        private boolean async = true;
    }

    @Data
    public static class Jpa {
        private String hibernateDdlAuto = "validate";
        private String dialect = "org.hibernate.dialect.PostgreSQLDialect";
        private boolean showSql = false;
        private boolean formatSql = false;
        private boolean useSqlComments = false;
        private String namingStrategy = "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl";
        private boolean deferDatasourceInitialization = false;
        private Properties properties = new Properties();

        @Data
        public static class Properties {
            private String hibernateFormatSql = "false";
            private String hibernateUseSqlComments = "false";
            private String hibernateJdbcBatchSize = "20";
            private String hibernateOrderInserts = "true";
            private String hibernateOrderUpdates = "true";
            private String hibernateBatchVersionedData = "true";
            private String hibernateJdbcFetchSize = "20";
            private String hibernateDefaultBatchFetchSize = "16";
        }
    }

    @Data
    public static class Flyway {
        private boolean enabled = true;
        private String[] locations = {"classpath:db/migration"};
        private String baselineOnMigrate = "false";
        private String baselineVersion = "0";
        private String validateOnMigrate = "true";
        private String outOfOrder = "false";
        private String cleanDisabled = "true";
        private String cleanOnValidationError = "false";
    }

    @Data
    public static class Actuator {
        private boolean enabled = true;
        private String[] endpoints = {"health", "info", "metrics", "env"};
        private String basePath = "/actuator";
        private boolean exposeHealthDetails = true;
        private boolean showDetails = true;
        private boolean showComponents = true;
    }

    @Data
    public static class OpenApi {
        private String title = "Finance Control API";
        private String description = "API for managing personal finances";
        private String version = "1.0.0";
        private String contactName = "Finance Control Team";
        private String contactEmail = "support@finance-control.com";
        private String contactUrl = "https://github.com/LucasSantana/finance-control";
        private String licenseName = "MIT License";
        private String licenseUrl = "https://opensource.org/licenses/MIT";
        private String serverUrl = "http://localhost:8080";
        private String serverDescription = "Development server";
    }

    @Data
    public static class Pagination {
        private int defaultPageSize = 10;
        private int maxPageSize = 100;
        private String defaultSort = "id";
        private String defaultDirection = "ASC";
    }

    @Data
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private int database = 0;
        private long timeout = 2000;
        private Pool pool = new Pool();

        @Data
        public static class Pool {
            private int maxActive = 8;
            private int maxIdle = 8;
            private int minIdle = 0;
            private long maxWait = -1;
        }
    }

    @Data
    public static class Cache {
        private boolean enabled = true;
        private long ttlDashboard = 900000; // 15 minutes
        private long ttlMarketData = 300000; // 5 minutes
        private long ttlUserData = 1800000; // 30 minutes
    }

    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private int requestsPerMinute = 100;
        private int burstCapacity = 200;
        private int refreshPeriod = 60; // seconds
    }

    @Data
    public static class Monitoring {
        private boolean enabled = true;
        private Sentry sentry = new Sentry();
        private HealthCheck healthCheck = new HealthCheck();

        @Data
        public static class Sentry {
            private boolean enabled = true;
            private String dsn = "";
            private String environment = "dev";
            private String release = "1.0.0";
            private double sampleRate = 0.1;
            private double tracesSampleRate = 0.1;
            private boolean sendDefaultPii = false;
            private boolean attachStacktrace = true;
            private boolean enableTracing = true;
        }

        @Data
        public static class HealthCheck {
            private int interval = 30; // seconds
            private boolean detailed = true;
        }
    }
}
