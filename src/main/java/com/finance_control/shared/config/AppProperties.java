package com.finance_control.shared.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Application properties configuration that binds environment variables to typed properties.
 * This centralizes all application configuration and provides type-safe access to environment variables.
 * Uses immutable records with constructor binding for thread safety and no EI_EXPOSE_REP warnings.
 */
@Component
@ConfigurationProperties(prefix = "app")
@SuppressFBWarnings("EI_EXPOSE_REP2") // False positive: Spring Boot handles List injection safely in @ConfigurationProperties
public record AppProperties(
    Database database,
    Security security,
    Server server,
    Logging logging,
    Jpa jpa,
    Flyway flyway,
    Actuator actuator,
    OpenApi openApi,
    Pagination pagination,
    Redis redis,
    Cache cache,
    RateLimit rateLimit,
    Supabase supabase,
    Monitoring monitoring
) {

    public AppProperties() {
        this(new Database(), new Security(), new Server(), new Logging(), new Jpa(),
             new Flyway(), new Actuator(), new OpenApi(), new Pagination(),
             new Redis(), new Cache(), new RateLimit(), new Supabase(), new Monitoring());
    }

    public record Database(
        String url,
        String username,
        String password,
        String driverClassName,
        String port,
        String name,
        Pool pool
    ) {
        public Database() {
            this("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                 "sa", "", "org.h2.Driver", "5432", "finance_control", new Pool());
        }
    }

    public record Pool(
        int initialSize,
        int maxSize,
        int minIdle,
        long maxLifetime,
        long connectionTimeout,
        long idleTimeout,
        long leakDetectionThreshold
    ) {
        public Pool() {
            this(5, 20, 5, 300000, 20000, 300000, 60000);
        }
    }

    public record Security(
        Jwt jwt,
        Cors cors,
        List<String> publicEndpoints
    ) {
        public Security() {
            this(new Jwt(), new Cors(), List.of("/api/auth/**", "/api/users", "/api/monitoring/**", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**"));
        }
    }

    public record Jwt(
        String secret,
        long expirationMs,
        long refreshExpirationMs,
        String issuer,
        String audience
    ) {
        public Jwt() {
            this(null, 86400000, 604800000, "finance-control", "finance-control-users");
        }
    }

    public record Cors(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        boolean allowCredentials,
        long maxAge
    ) {
        public Cors() {
            this(List.of("http://localhost:3000", "http://localhost:8080"),
                 List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"),
                 List.of("*"),
                 true,
                 3600);
        }
    }
    public record Server(
        int port,
        String contextPath,
        String servletPath,
        int maxHttpHeaderSize,
        int maxHttpPostSize,
        int connectionTimeout,
        int readTimeout,
        int writeTimeout
    ) {
        public Server() {
            this(8080, "", "/", 8192, 2097152, 20000, 30000, 30000);
        }
    }

    public record Logging(
        String level,
        String pattern,
        String filePath,
        String fileName,
        String errorFileName,
        int maxFileSize,
        int maxHistory,
        int queueSize,
        boolean async
    ) {
        public Logging() {
            this("INFO",
                 "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n",
                 "logs",
                 "finance-control.log",
                 "finance-control-error.log",
                 10485760,
                 30,
                 512,
                 true);
        }
    }

    public record Jpa(
        String hibernateDdlAuto,
        String dialect,
        boolean showSql,
        boolean formatSql,
        boolean useSqlComments,
        String namingStrategy,
        boolean deferDatasourceInitialization,
        Properties properties
    ) {
        public Jpa() {
            this("validate",
                 "org.hibernate.dialect.PostgreSQLDialect",
                 false,
                 false,
                 false,
                 "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl",
                 false,
                 new Properties());
        }
    }

    public record Properties(
        String hibernateFormatSql,
        String hibernateUseSqlComments,
        String hibernateJdbcBatchSize,
        String hibernateOrderInserts,
        String hibernateOrderUpdates,
        String hibernateBatchVersionedData,
        String hibernateJdbcFetchSize,
        String hibernateDefaultBatchFetchSize
    ) {
        public Properties() {
            this("false", "false", "20", "true", "true", "true", "20", "16");
        }
    }

    public record Flyway(
        boolean enabled,
        List<String> locations,
        String baselineOnMigrate,
        String baselineVersion,
        String validateOnMigrate,
        String outOfOrder,
        String cleanDisabled,
        String cleanOnValidationError
    ) {
        public Flyway() {
            this(true,
                 List.of("classpath:db/migration"),
                 "false",
                 "0",
                 "true",
                 "false",
                 "true",
                 "false");
        }
    }

    public record Actuator(
        boolean enabled,
        List<String> endpoints,
        String basePath,
        boolean exposeHealthDetails,
        boolean showDetails,
        boolean showComponents
    ) {
        public Actuator() {
            this(true,
                 List.of("health", "info", "metrics", "env"),
                 "/actuator",
                 true,
                 true,
                 true);
        }
    }

    public record OpenApi(
        String title,
        String description,
        String version,
        String contactName,
        String contactEmail,
        String contactUrl,
        String licenseName,
        String licenseUrl,
        String serverUrl,
        String serverDescription
    ) {
        public OpenApi() {
            this("Finance Control API",
                 "API for managing personal finances",
                 "1.0.0",
                 "Finance Control Team",
                 "support@finance-control.com",
                 "https://github.com/LucasSantana/finance-control",
                 "MIT License",
                 "https://opensource.org/licenses/MIT",
                 "http://localhost:8080",
                 "Development server");
        }
    }

    public record Pagination(
        int defaultPageSize,
        int maxPageSize,
        String defaultSort,
        String defaultDirection
    ) {
        public Pagination() {
            this(10, 100, "id", "ASC");
        }
    }

    public record Redis(
        String host,
        int port,
        String password,
        int database,
        long timeout,
        RedisPool pool
    ) {
        public Redis() {
            this("localhost", 6379, "", 0, 2000, new RedisPool());
        }
    }

    public record RedisPool(
        int maxActive,
        int maxIdle,
        int minIdle,
        long maxWait
    ) {
        public RedisPool() {
            this(8, 8, 0, -1);
        }
    }

    public record Cache(
        boolean enabled,
        long ttlDashboard,
        long ttlMarketData,
        long ttlUserData
    ) {
        public Cache() {
            this(true, 900000, 300000, 1800000);
        }
    }

    public record RateLimit(
        boolean enabled,
        int requestsPerMinute,
        int burstCapacity,
        int refreshPeriod
    ) {
        public RateLimit() {
            this(true, 100, 200, 60);
        }
    }

    public record Supabase(
        boolean enabled,
        String url,
        String anonKey,
        Realtime realtime
    ) {
        public Supabase() {
            this(false, "", "", new Realtime());
        }
    }

    public record Realtime(
        boolean enabled,
        List<String> channels
    ) {
        public Realtime() {
            this(false, List.of("transactions", "dashboard", "goals"));
        }
    }

    public record Monitoring(
        boolean enabled,
        Sentry sentry,
        HealthCheck healthCheck
    ) {
        public Monitoring() {
            this(true, new Sentry(), new HealthCheck());
        }
    }

    public record Sentry(
        boolean enabled,
        String dsn,
        String environment,
        String release,
        double sampleRate,
        double tracesSampleRate,
        boolean sendDefaultPii,
        boolean attachStacktrace,
        boolean enableTracing
    ) {
        public Sentry() {
            this(true, "", "dev", "1.0.0", 0.1, 0.1, false, true, true);
        }
    }

    public record HealthCheck(
        int interval,
        boolean detailed
    ) {
        public HealthCheck() {
            this(30, true);
        }
    }
}
