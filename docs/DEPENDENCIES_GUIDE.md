# Dependencies Guide

This document explains the key dependencies used in the finance-control application and their purposes.

## Core Spring Boot Dependencies

### Spring Boot Starters
- **spring-boot-starter-web**: Web application support with embedded Tomcat
- **spring-boot-starter-data-jpa**: JPA and Hibernate support
- **spring-boot-starter-security**: Spring Security for authentication and authorization
- **spring-boot-starter-validation**: Bean Validation support
- **spring-boot-starter-actuator**: Production monitoring and metrics

### Development Tools
- **spring-boot-devtools**: Development-time features (auto-restart, live reload)

## Database & Migration

### PostgreSQL
- **postgresql**: PostgreSQL JDBC driver
- **flyway-core**: Database migration tool
- **flyway-database-postgresql**: PostgreSQL-specific Flyway support

### Testing Database
- **h2**: In-memory database for testing

## Security & Authentication

### JWT (JSON Web Tokens)
- **jjwt-api**: JWT API for creating and parsing tokens
- **jjwt-impl**: JWT implementation
- **jjwt-jackson**: Jackson integration for JWT

## API Documentation

### OpenAPI/Swagger
- **springdoc-openapi-starter-webmvc-ui**: OpenAPI 3 documentation with Swagger UI

## Testing Dependencies

### Core Testing
- **spring-boot-starter-test**: Spring Boot test support
- **spring-security-test**: Security testing utilities

### End-to-End Testing
- **selenium-java**: Selenium WebDriver for browser automation
- **selenium-chrome-driver**: Chrome WebDriver

### Container Testing
- **testcontainers**: Docker-based testing
- **testcontainers-junit-jupiter**: JUnit 5 integration
- **testcontainers-postgresql**: PostgreSQL container support

## Utilities

### Lombok
- **lombok**: Reduces boilerplate code with annotations
- **lombok-maven-plugin**: Maven integration for annotation processing

### Environment Configuration
- **spring-dotenv**: .env file support for configuration

## Build & Deployment

### Maven Plugins
- **maven-compiler-plugin**: Java compilation with Lombok support
- **spring-boot-maven-plugin**: Spring Boot application packaging
- **flyway-maven-plugin**: Database migration via Maven

## Version Information

### Spring Boot Version
- **3.5.3**: Latest stable version with Java 24 support

### Java Version
- **24**: Latest LTS version with modern features

## Dependency Management

### Spring Boot Parent
All Spring Boot dependencies are managed through the parent POM, ensuring version compatibility.

### Version Alignment
- All Spring Boot starters use the same version (3.5.3)
- JWT libraries use version 0.12.3
- OpenAPI uses version 2.5.0
- Flyway Maven plugin uses version 11.7.2

## Configuration Properties

### Database Configuration
```properties
spring.datasource.url=${DB_URL}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

### JPA Configuration
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

### Flyway Configuration
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

## Testing Configuration

### Test Profiles
- Use `@ActiveProfiles("test")` for test-specific configuration
- H2 in-memory database for unit and integration tests
- TestContainers for realistic database testing

### Test Dependencies
- **spring-boot-starter-test**: Provides testing utilities
- **spring-security-test**: Security testing support
- **h2**: In-memory database for tests
- **testcontainers**: Docker-based testing

## Security Dependencies

### Spring Security
- **spring-boot-starter-security**: Core security framework
- **spring-security-test**: Testing utilities for security

### JWT Libraries
- **jjwt-api**: JWT creation and parsing
- **jjwt-impl**: Implementation details
- **jjwt-jackson**: JSON serialization

## Monitoring Dependencies

### Spring Boot Actuator
- **spring-boot-starter-actuator**: Production monitoring
- Health checks, metrics, and management endpoints

## Development Dependencies

### Lombok
- **lombok**: Code generation for getters, setters, constructors
- Reduces boilerplate code significantly

### Spring Boot DevTools
- **spring-boot-devtools**: Development-time features
- Auto-restart on code changes
- Live reload for static resources

## Environment Configuration

### Spring Dotenv
- **spring-dotenv**: .env file support
- Loads environment variables from .env files
- Useful for local development

## Migration Dependencies

### Flyway
- **flyway-core**: Database migration framework
- **flyway-database-postgresql**: PostgreSQL-specific features
- **flyway-maven-plugin**: Maven integration

## API Documentation Dependencies

### OpenAPI/Swagger
- **springdoc-openapi-starter-webmvc-ui**: OpenAPI 3 support
- Auto-generates API documentation
- Interactive Swagger UI

## Testing Strategy Dependencies

### Unit Testing
- **spring-boot-starter-test**: Core testing support
- JUnit 5, Mockito, AssertJ included

### Integration Testing
- **spring-boot-starter-test**: Integration test support
- **h2**: In-memory database
- **testcontainers**: Real database testing

### End-to-End Testing
- **selenium-java**: Browser automation
- **selenium-chrome-driver**: Chrome WebDriver
- **testcontainers**: Application containerization

## Dependency Best Practices

### Version Management
- Use Spring Boot parent for version management
- Keep dependencies up to date
- Use specific versions for critical dependencies

### Security Considerations
- Regularly update security-related dependencies
- Use Spring Security for authentication
- Implement proper JWT handling

### Testing Strategy
- Use appropriate testing dependencies for each test type
- Maintain test isolation
- Use realistic test data

### Performance Considerations
- Use connection pooling for database connections
- Implement caching where appropriate
- Monitor application performance with Actuator 