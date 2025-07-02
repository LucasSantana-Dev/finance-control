# Environment Variables Configuration

This document describes the comprehensive environment variable system implemented in the Finance Control application.

## Overview

The application uses a centralized configuration system based on `@ConfigurationProperties` that binds environment variables to typed Java properties. This provides:

- **Type Safety**: All configuration values are strongly typed
- **Centralized Management**: All configuration is managed through `AppProperties`
- **Environment Flexibility**: Easy configuration for different environments
- **Validation**: Built-in validation and default values
- **Documentation**: Self-documenting configuration structure

## Configuration Structure

### Core Configuration Classes

1. **`AppProperties`**: Main configuration class with nested properties
2. **`ConfigurationPropertiesConfig`**: Enables `@ConfigurationProperties`
3. **`EnvironmentConfig`**: Provides utility methods and logging
4. **Specialized Config Classes**: Database, Security, Logging, etc.

### Property Categories

The configuration is organized into logical categories:

- **Database**: Connection, pool, and JPA settings
- **Security**: JWT, CORS, and authentication settings
- **Server**: Port, context path, and timeout settings
- **Logging**: Levels, patterns, and file settings
- **JPA**: Hibernate and persistence settings
- **Flyway**: Database migration settings
- **Actuator**: Spring Boot monitoring settings
- **OpenAPI**: Swagger documentation settings
- **Pagination**: Default page sizes and sorting

## Environment Variables Reference

### Database Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost` | Database connection URL |
| `DB_PORT` | `5432` | Database port |
| `DB_NAME` | `finance_control` | Database name |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `DB_POOL_INITIAL_SIZE` | `5` | Initial connection pool size |
| `DB_POOL_MAX_SIZE` | `20` | Maximum connection pool size |
| `DB_POOL_MIN_IDLE` | `5` | Minimum idle connections |
| `DB_POOL_MAX_LIFETIME` | `300000` | Maximum connection lifetime (ms) |
| `DB_POOL_CONNECTION_TIMEOUT` | `20000` | Connection timeout (ms) |
| `DB_POOL_IDLE_TIMEOUT` | `300000` | Idle timeout (ms) |
| `DB_POOL_LEAK_DETECTION_THRESHOLD` | `60000` | Leak detection threshold (ms) |

### JPA/Hibernate Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `JPA_DDL_AUTO` | `validate` | Hibernate DDL auto mode |
| `JPA_SHOW_SQL` | `false` | Show SQL queries |
| `JPA_FORMAT_SQL` | `false` | Format SQL queries |
| `JPA_USE_SQL_COMMENTS` | `false` | Use SQL comments |
| `JPA_DEFER_DATASOURCE_INIT` | `false` | Defer datasource initialization |
| `HIBERNATE_FORMAT_SQL` | `false` | Hibernate SQL formatting |
| `HIBERNATE_USE_SQL_COMMENTS` | `false` | Hibernate SQL comments |
| `HIBERNATE_JDBC_BATCH_SIZE` | `20` | JDBC batch size |
| `HIBERNATE_ORDER_INSERTS` | `true` | Order inserts |
| `HIBERNATE_ORDER_UPDATES` | `true` | Order updates |
| `HIBERNATE_BATCH_VERSIONED_DATA` | `true` | Batch versioned data |
| `HIBERNATE_JDBC_FETCH_SIZE` | `20` | JDBC fetch size |
| `HIBERNATE_DEFAULT_BATCH_FETCH_SIZE` | `16` | Default batch fetch size |

### Security Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | `defaultSecretKeyForDevelopmentOnly` | JWT secret key |
| `JWT_EXPIRATION_MS` | `86400000` | JWT expiration time (ms) |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` | Refresh token expiration (ms) |
| `JWT_ISSUER` | `finance-control` | JWT issuer |
| `JWT_AUDIENCE` | `finance-control-users` | JWT audience |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:8080` | Allowed CORS origins |
| `CORS_ALLOWED_METHODS` | `GET,POST,PUT,DELETE,OPTIONS` | Allowed CORS methods |
| `CORS_ALLOWED_HEADERS` | `*` | Allowed CORS headers |
| `CORS_ALLOW_CREDENTIALS` | `true` | Allow CORS credentials |
| `CORS_MAX_AGE` | `3600` | CORS max age (seconds) |

### Server Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | Server port |
| `SERVER_CONTEXT_PATH` | `` | Server context path |
| `SERVER_SERVLET_PATH` | `/` | Servlet path |
| `SERVER_MAX_HTTP_HEADER_SIZE` | `8192` | Max HTTP header size |
| `SERVER_MAX_HTTP_POST_SIZE` | `2097152` | Max HTTP post size |
| `SERVER_CONNECTION_TIMEOUT` | `20000` | Connection timeout (ms) |
| `SERVER_READ_TIMEOUT` | `30000` | Read timeout (ms) |
| `SERVER_WRITE_TIMEOUT` | `30000` | Write timeout (ms) |

### Logging Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `LOGGING_LEVEL` | `INFO` | Root logging level |
| `LOGGING_PATTERN` | `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n` | Log pattern |
| `LOGGING_FILE_PATH` | `logs` | Log file directory |
| `LOGGING_FILE_NAME` | `finance-control.log` | Main log file name |
| `LOGGING_ERROR_FILE_NAME` | `finance-control-error.log` | Error log file name |
| `LOGGING_MAX_FILE_SIZE` | `10485760` | Max log file size (bytes) |
| `LOGGING_MAX_HISTORY` | `30` | Max log history (days) |
| `LOGGING_QUEUE_SIZE` | `512` | Async logging queue size |
| `LOGGING_ASYNC` | `true` | Enable async logging |

### Flyway Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `FLYWAY_ENABLED` | `true` | Enable Flyway migrations |
| `FLYWAY_LOCATIONS` | `classpath:db/migration` | Migration locations |
| `FLYWAY_BASELINE_ON_MIGRATE` | `false` | Baseline on migrate |
| `FLYWAY_BASELINE_VERSION` | `0` | Baseline version |
| `FLYWAY_VALIDATE_ON_MIGRATE` | `true` | Validate on migrate |
| `FLYWAY_OUT_OF_ORDER` | `false` | Allow out of order migrations |
| `FLYWAY_CLEAN_DISABLED` | `true` | Disable clean command |
| `FLYWAY_CLEAN_ON_VALIDATION_ERROR` | `false` | Clean on validation error |

### Actuator Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `ACTUATOR_ENABLED` | `true` | Enable Spring Boot Actuator |
| `ACTUATOR_ENDPOINTS` | `health,info,metrics,env` | Exposed endpoints |
| `ACTUATOR_BASE_PATH` | `/actuator` | Actuator base path |
| `ACTUATOR_EXPOSE_HEALTH_DETAILS` | `true` | Expose health details |
| `ACTUATOR_SHOW_DETAILS` | `when-authorized` | Show details policy |
| `ACTUATOR_SHOW_COMPONENTS` | `when-authorized` | Show components policy |

### OpenAPI Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `OPENAPI_TITLE` | `Finance Control API` | API title |
| `OPENAPI_DESCRIPTION` | `API for managing personal finances` | API description |
| `OPENAPI_VERSION` | `1.0.0` | API version |
| `OPENAPI_CONTACT_NAME` | `Finance Control Team` | Contact name |
| `OPENAPI_CONTACT_EMAIL` | `support@finance-control.com` | Contact email |
| `OPENAPI_CONTACT_URL` | `https://github.com/LucasSantana-Dev/finance-control` | Contact URL |
| `OPENAPI_LICENSE_NAME` | `MIT License` | License name |
| `OPENAPI_LICENSE_URL` | `https://opensource.org/licenses/MIT` | License URL |
| `OPENAPI_SERVER_URL` | `http://localhost:8080` | Server URL |
| `OPENAPI_SERVER_DESCRIPTION` | `Development server` | Server description |

### Pagination Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `PAGINATION_DEFAULT_PAGE_SIZE` | `10` | Default page size |
| `PAGINATION_MAX_PAGE_SIZE` | `100` | Maximum page size |
| `PAGINATION_DEFAULT_SORT` | `id` | Default sort field |
| `PAGINATION_DEFAULT_DIRECTION` | `ASC` | Default sort direction |

## Usage Examples

### Setting Environment Variables

#### Linux/macOS
```bash
export DB_URL=jdbc:postgresql://localhost
export DB_PORT=5432
export DB_NAME=finance_control
export DB_USERNAME=postgres
export DB_PASSWORD=mysecretpassword
export JWT_SECRET=my-super-secret-jwt-key
export SERVER_PORT=8080
```

#### Windows (PowerShell)
```powershell
$env:DB_URL="jdbc:postgresql://localhost"
$env:DB_PORT="5432"
$env:DB_NAME="finance_control"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="mysecretpassword"
$env:JWT_SECRET="my-super-secret-jwt-key"
$env:SERVER_PORT="8080"
```

#### Windows (CMD)
```cmd
set DB_URL=jdbc:postgresql://localhost
set DB_PORT=5432
set DB_NAME=finance_control
set DB_USERNAME=postgres
set DB_PASSWORD=mysecretpassword
set JWT_SECRET=my-super-secret-jwt-key
set SERVER_PORT=8080
```

### Using .env File

1. Copy the template:
```bash
cp env-template.txt .env
```

2. Edit the `.env` file with your values:
```env
DB_URL=jdbc:postgresql://localhost
DB_PORT=5432
DB_NAME=finance_control
DB_USERNAME=postgres
DB_PASSWORD=mysecretpassword
JWT_SECRET=my-super-secret-jwt-key
SERVER_PORT=8080
```

3. Load the environment variables:
```bash
# Linux/macOS
source .env

# Windows (PowerShell)
. .env

# Windows (CMD)
set /p < .env
```

### Docker Compose

The application includes Docker Compose configuration that automatically uses environment variables:

```yaml
services:
  app:
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod}
      SPRING_DATASOURCE_URL: ${DB_URL}
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
```

## Configuration Classes

### AppProperties

The main configuration class that binds all environment variables:

```java
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Database database = new Database();
    private Security security = new Security();
    private Server server = new Server();
    // ... other properties
}
```

### Using Configuration in Code

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final AppProperties appProperties;
    
    public void doSomething() {
        String dbUrl = appProperties.getDatabase().getUrl();
        int serverPort = appProperties.getServer().getPort();
        String jwtSecret = appProperties.getSecurity().getJwt().getSecret();
    }
}
```

### EnvironmentInfo Utility

```java
@Component
@RequiredArgsConstructor
public class MyComponent {
    private final EnvironmentConfig.EnvironmentInfo envInfo;
    
    public void checkEnvironment() {
        if (envInfo.isDevelopment()) {
            // Development-specific logic
        }
        
        if (envInfo.isProduction()) {
            // Production-specific logic
        }
        
        String dbUrl = envInfo.getDatabaseUrl();
        String jwtSecret = envInfo.getJwtSecret();
    }
}
```

## Environment-Specific Configuration

### Development Environment

```env
SPRING_PROFILES_ACTIVE=dev
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
LOGGING_LEVEL=DEBUG
ACTUATOR_ENABLED=true
```

### Production Environment

```env
SPRING_PROFILES_ACTIVE=prod
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false
LOGGING_LEVEL=WARN
ACTUATOR_ENABLED=true
ACTUATOR_SHOW_DETAILS=never
```

### Test Environment

```env
SPRING_PROFILES_ACTIVE=test
JPA_DDL_AUTO=create-drop
FLYWAY_ENABLED=false
ACTUATOR_ENABLED=false
```

## Validation and Default Values

All configuration properties have sensible default values and validation:

- **Database**: Defaults to local PostgreSQL
- **Security**: Default JWT secret (change in production)
- **Server**: Default port 8080
- **Logging**: Default INFO level with file rotation
- **JPA**: Default validation mode
- **Flyway**: Default migration settings

## Security Considerations

### Production Security

1. **Change JWT Secret**: Always change `JWT_SECRET` in production
2. **Database Credentials**: Use strong passwords
3. **CORS Origins**: Restrict to specific domains
4. **Actuator Security**: Configure appropriate access controls
5. **Logging**: Avoid logging sensitive information

### Environment Variable Security

```env
# Production example
JWT_SECRET=your-super-secret-production-jwt-key-here
DB_PASSWORD=your-strong-database-password
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://api.yourdomain.com
ACTUATOR_SHOW_DETAILS=never
```

## Monitoring and Debugging

### Configuration Logging

The application logs all configuration values at startup:

```
=== Finance Control Application Configuration ===
Database URL: jdbc:postgresql://localhost:5432/finance_control
Database Username: postgres
Database Pool - Max: 20, Min: 5, Timeout: 20000ms
Server Port: 8080
JWT Secret configured: YES
JWT Expiration: 86400000ms
CORS Origins: http://localhost:3000, http://localhost:8080
Logging Level: INFO
Log File: logs/finance-control.log
JPA DDL Auto: validate
JPA Show SQL: false
Flyway Enabled: true
Actuator Enabled: true
OpenAPI Title: Finance Control API
Pagination - Default: 10, Max: 100
Active Profiles: dev
=== Configuration Logging Complete ===
```

### Actuator Endpoints

Access configuration through Actuator endpoints:

- `/actuator/env`: Environment variables
- `/actuator/configprops`: Configuration properties
- `/actuator/health`: Application health

### Environment Info Bean

```java
@Autowired
private EnvironmentConfig.EnvironmentInfo envInfo;

public void debugConfiguration() {
    log.info("Database URL: {}", envInfo.getDatabaseUrl());
    log.info("Server Port: {}", envInfo.getServerPort());
    log.info("Is Development: {}", envInfo.isDevelopment());
    log.info("Is Production: {}", envInfo.isProduction());
}
```

## Best Practices

1. **Use Environment Variables**: Always use environment variables for configuration
2. **Default Values**: Provide sensible defaults for all properties
3. **Validation**: Validate critical configuration values
4. **Documentation**: Document all configuration options
5. **Security**: Never commit secrets to version control
6. **Profiles**: Use Spring profiles for environment-specific configuration
7. **Monitoring**: Log configuration values for debugging
8. **Type Safety**: Use typed properties instead of raw strings

## Troubleshooting

### Common Issues

1. **Configuration Not Loading**: Check `@EnableConfigurationProperties` annotation
2. **Environment Variables Not Found**: Verify variable names and casing
3. **Default Values Not Applied**: Check property binding configuration
4. **Validation Errors**: Review property validation annotations

### Debug Configuration

```java
@PostConstruct
public void debugConfig() {
    log.info("AppProperties: {}", appProperties);
    log.info("Environment: {}", Arrays.toString(environment.getActiveProfiles()));
}
```

This comprehensive environment variable system provides a robust, type-safe, and flexible configuration management solution for the Finance Control application. 