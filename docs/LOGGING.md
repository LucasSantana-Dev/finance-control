# Logging Configuration and Implementation

This document provides comprehensive information about logging in the Finance Control application, covering configuration, implementation patterns, and log organization.

## Overview

The application uses Logback as the logging framework with a comprehensive configuration that provides:

- **Console logging** with colored output for development
- **File logging** with rotation and size limits
- **Error-only logging** for critical issues
- **Profile-specific configurations** for different environments
- **Performance optimization** with async logging
- **Base class logging** through Lombok's `@Slf4j` annotation

## Configuration Files

### 1. `logback-spring.xml`

The main logging configuration file that defines:

- **Appenders**: Console, File, Error File, and Async File
- **Loggers**: Package-specific log levels
- **Profiles**: Environment-specific configurations

### 2. `application.properties`

Contains basic logging level overrides that can be easily modified:

```properties
# Logging Configuration
logging.level.root=INFO
logging.level.com.finance_control=DEBUG
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Log Levels

### Application Logs (`com.finance_control`)
- **Level**: DEBUG
- **Purpose**: Detailed application logic, method entry/exit, business operations
- **Use cases**: Debugging business logic, tracking user actions, performance monitoring

### Spring Security (`org.springframework.security`)
- **Level**: DEBUG
- **Purpose**: Authentication and authorization events
- **Use cases**: Debugging security issues, tracking login attempts, permission checks

### Spring Web (`org.springframework.web`)
- **Level**: DEBUG
- **Purpose**: HTTP request/response handling, controller operations
- **Use cases**: Debugging REST API issues, tracking request flow

### Hibernate SQL (`org.hibernate.SQL`)
- **Level**: WARN
- **Purpose**: SQL query execution
- **Use cases**: Performance monitoring, query optimization

### Hibernate Parameter Binding (`org.hibernate.type.descriptor.sql.BasicBinder`)
- **Level**: WARN
- **Purpose**: SQL parameter values
- **Use cases**: Debugging data issues, security auditing

## Log Output

### Console Output
- **Format**: `HH:mm:ss.SSS [thread] LEVEL logger - message`
- **Features**: Colored output for better readability
- **Example**: `14:30:25.123 [http-nio-${APPLICATION_PORT}-exec-1] DEBUG c.f.t.s.TransactionService - Processing transaction for user: 123`

### File Output
- **Location**: `logs/finance-control.log`
- **Rotation**: Daily with 10MB size limit
- **Retention**: 30 days
- **Format**: `yyyy-MM-dd HH:mm:ss.SSS [thread] LEVEL logger - message`

### Error File
- **Location**: `logs/finance-control-error.log`
- **Content**: Only ERROR level messages
- **Purpose**: Quick access to critical issues

## Environment Profiles

### Development (`dev`)
- Application logs: DEBUG
- Spring Web: DEBUG
- Spring Security: DEBUG
- Console output: Enabled
- File output: Enabled

### Production (`prod`)
- Application logs: INFO
- Spring Web: WARN
- Spring Security: WARN
- Root level: WARN
- Console output: Enabled
- File output: Enabled

### Test (`test`)
- Application logs: DEBUG
- Spring Test: DEBUG
- Console output: Enabled
- File output: Disabled

## Base Class Logging Implementation

The application implements SLF4J logging through Lombok's `@Slf4j` annotation in the base classes to avoid code duplication and provide consistent logging across all services and controllers.

### Base Classes with Logging

#### 1. BaseService<T, I, D>

**Location:** `src/main/java/com/finance_control/shared/service/BaseService.java`

**Annotation:** `@Slf4j`

**Purpose:** Provides logging functionality to all service classes that extend it.

**Key Logging Points:**
- **findAll()**: Logs search parameters, filters, and result counts
- **findById()**: Logs entity lookup attempts and results
- **create()**: Logs entity creation process and success
- **update()**: Logs entity update process and success
- **delete()**: Logs entity deletion process and success
- **User-aware operations**: Logs user context validation and ownership checks

**Log Levels Used:**
- `DEBUG`: Method entry, parameter values, intermediate steps
- `INFO`: Successful operations (create, update, delete)
- `WARN`: Entity not found, access denied scenarios
- `ERROR`: User context not available, critical failures

#### 2. BaseController<T, I, D>

**Location:** `src/main/java/com/finance_control/shared/controller/BaseController.java`

**Annotation:** `@Slf4j`

**Purpose:** Provides logging functionality to all controller classes that extend it.

**Key Logging Points:**
- **findAll()**: Logs request parameters and result counts
- **findById()**: Logs entity lookup requests and results
- **create()**: Logs entity creation requests and success
- **update()**: Logs entity update requests and success
- **delete()**: Logs entity deletion requests and success

**Log Levels Used:**
- `DEBUG`: Request details, parameter values, response information
- `INFO`: Successful operations

#### 3. GlobalExceptionHandler

**Location:** `src/main/java/com/finance_control/shared/exception/GlobalExceptionHandler.java`

**Annotation:** `@Slf4j`

**Purpose:** Centralized exception handling with logging.

**Key Logging Points:**
- **EntityNotFoundException**: Logs as WARN
- **IllegalArgumentException**: Logs as WARN
- **MethodArgumentNotValidException**: Logs as WARN
- **Generic Exception**: Logs as ERROR with stack trace

## Benefits of Base Class Logging

### 1. Code Reuse
- All services and controllers automatically get logging without additional code
- Consistent logging patterns across the application
- No need to add `@Slf4j` to individual service/controller classes

### 2. Consistent Logging
- Standardized log messages and levels
- Uniform approach to logging user-aware operations
- Consistent error handling and logging

### 3. Maintainability
- Centralized logging configuration
- Easy to modify logging behavior for all services/controllers
- Reduced code duplication

## Log Organization by Type and Script

Project logs have been organized into specific subdirectories within `/logs` to facilitate location, maintenance, and analysis of different types of logs and reports.

### Directory Structure

```
finance-control/
├── logs/                          # Root logs directory
│   ├── application/               # Spring Boot application logs
│   │   ├── finance-control.log    # Main application log
│   │   └── finance-control-error.log # Application error log
│   │
│   ├── checkstyle/                # Code style verification logs
│   │   ├── checkstyle_*.log       # Style verifications
│   │   ├── checkstyle_summary_*.txt # Verification summaries
│   │   ├── fix-checkstyle_*.log   # Automatic fixes
│   │   ├── fix-checkstyle_summary_*.txt # Fix summaries
│   │   ├── fix-remaining-checkstyle_*.log # Manual fixes
│   │   └── fix-remaining-checkstyle_summary_*.txt # Manual fix summaries
│   │
│   ├── quality/                   # Code quality verification logs
│   │   ├── quality-check_*.log    # Complete quality verifications
│   │   └── quality-check-summary_*.txt # Verification summaries
│   │
│   ├── gradle/                    # Gradle task logs
│   │   ├── gradle_*_*.log         # Detailed task logs
│   │   └── gradle_*_summary_*.txt # Execution summaries
│   │
│   ├── docker/                    # Docker operation logs
│   │   ├── docker-run_*.log       # Container execution
│   │   └── docker-build-test_*.log # Docker build tests
│   │
│   └── environment/               # Environment test logs
│       ├── gradle-test_*.log      # Gradle tests
│       ├── gradle-build-test_*.log # Build tests
│       └── app-startup-test_*.log # Startup tests
```

### Categorization by Type

#### 1. Application Logs (`logs/application/`)
**Purpose**: Logs generated by the running Spring Boot application
- **Files**: `finance-control.log`, `finance-control-error.log`
- **Content**: Application logs, HTTP requests, database operations
- **Generation**: Automatic by Spring Boot application
- **Configuration**: `logback-spring.xml`

#### 2. Checkstyle Logs (`logs/checkstyle/`)
**Purpose**: Code style verification and fix logs
- **Scripts**: `checkstyle-log.sh`, `fix-checkstyle.sh`, `fix-remaining-checkstyle.sh`
- **Files**:
  - `checkstyle_*.log` - Style verifications
  - `fix-checkstyle_*.log` - Automatic fixes
  - `fix-remaining-checkstyle_*.log` - Manual fixes
  - `*_summary_*.txt` - Execution summaries

#### 3. Quality Logs (`logs/quality/`)
**Purpose**: Complete code quality verification logs
- **Script**: `quality-check.sh`
- **Files**: `quality-check_*.log`, `quality-check-summary_*.txt`
- **Content**: Checkstyle, PMD, SpotBugs verifications, tests

#### 4. Gradle Logs (`logs/gradle/`)
**Purpose**: Gradle task logs executed via script
- **Script**: `gradle-with-logs.sh`
- **Files**: `gradle_{task}_{timestamp}.log`, `gradle_{task}_summary_{timestamp}.txt`
- **Examples**: `gradle_build_20250702_013000.log`, `gradle_test_20250702_013000.log`

#### 5. Docker Logs (`logs/docker/`)
**Purpose**: Docker operation logs
- **Scripts**: `docker-run.sh`, `test-fixes.sh` (docker-build-test)
- **Files**: `docker-run_*.log`, `docker-build-test_*.log`
- **Content**: Container execution, Docker builds

#### 6. Environment Logs (`logs/environment/`)
**Purpose**: Environment test and verification logs
- **Scripts**: `fix-environment.sh`, `test-fixes.sh`
- **Files**:
  - `gradle-test_*.log` - Gradle tests
  - `gradle-build-test_*.log` - Build tests
  - `app-startup-test_*.log` - Startup tests

## Usage Examples

### Adding Logging to Your Code

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public void processTransaction(TransactionDTO transaction) {
        logger.debug("Processing transaction: {}", transaction.getId());

        try {
            // Business logic
            logger.info("Transaction processed successfully: {}", transaction.getId());
        } catch (Exception e) {
            logger.error("Failed to process transaction: {}", transaction.getId(), e);
            throw e;
        }
    }
}
```

### Service Classes (Using Base Classes)
```java
@Service
public class TransactionService extends BaseService<Transaction, Long, TransactionDTO> {

    public TransactionService(TransactionRepository repository) {
        super(repository);
    }

    // All CRUD operations automatically have logging
    // No need to add @Slf4j or logging statements
}
```

### Controller Classes (Using Base Classes)
```java
@RestController
@RequestMapping("/api/transactions")
public class TransactionController extends BaseController<Transaction, Long, TransactionDTO> {

    public TransactionController(TransactionService service) {
        super(service);
    }

    // All REST endpoints automatically have logging
    // No need to add @Slf4j or logging statements
}
```

## Log Output Examples

### Service Logs
```
DEBUG - Finding all entities with search: 'transfer', filters: {}, sortBy: 'createdAt', sortDirection: 'desc', page: 0
DEBUG - Using repository findAll with search
DEBUG - Found 15 entities
INFO - Entity created successfully with ID: 123
WARN - Entity not found for update with ID: 999
ERROR - User context not available for user-aware service
```

### Controller Logs
```
DEBUG - GET request to list entities - search: 'transfer', sortBy: 'createdAt', sortDirection: 'desc', page: 0
DEBUG - Returning 15 entities out of 25 total
DEBUG - POST request to create entity: TransactionDTO{...}
INFO - Entity created successfully
DEBUG - DELETE request to delete entity with ID: 123
INFO - Entity deleted successfully with ID: 123
```

## Logging Best Practices

1. **Use appropriate log levels**:
   - `DEBUG`: Detailed information for debugging
   - `INFO`: General information about application flow
   - `WARN`: Warning conditions that don't stop execution
   - `ERROR`: Error conditions that need attention

2. **Use parameterized logging**:
   ```java
   // Good
   logger.debug("Processing user: {}", userId);

   // Avoid
   logger.debug("Processing user: " + userId);
   ```

3. **Include context in log messages**:
   ```java
   logger.info("User {} created transaction {} with amount {}",
              userId, transactionId, amount);
   ```

4. **Log exceptions properly**:
   ```java
   try {
       // risky operation
   } catch (Exception e) {
       logger.error("Failed to process request for user: {}", userId, e);
       throw e;
   }
   ```

5. **Use Base Classes**: Always extend `BaseService` and `BaseController` for new services and controllers
6. **Leverage Existing Logging**: Don't add redundant logging statements for standard CRUD operations
7. **Add Domain-Specific Logging**: Only add additional logging for business-specific operations
8. **Use Appropriate Log Levels**: Follow the established pattern (DEBUG for details, INFO for success, WARN for issues, ERROR for failures)
9. **Include Context**: Log relevant IDs, user information, and operation details

## Monitoring and Maintenance

### Log File Management
- Log files are automatically rotated daily
- Maximum file size: 10MB
- Retention period: 30 days
- Old files are automatically deleted

### Performance Considerations
- File logging uses async appenders for better performance
- Console logging is synchronous for immediate feedback
- Queue size for async logging: 512 messages

### Log Analysis
- Use tools like ELK Stack (Elasticsearch, Logstash, Kibana) for production
- Consider log aggregation services for distributed deployments
- Monitor log file sizes and disk usage

### Viewing Logs by Category

#### Application Logs
```bash
# View main logs
tail -f logs/application/finance-control.log

# View error logs
tail -f logs/application/finance-control-error.log
```

#### Checkstyle Logs
```bash
# View recent verifications
ls -la logs/checkstyle/checkstyle_*.log | tail -5

# View automatic fixes
ls -la logs/checkstyle/fix-checkstyle_*.log | tail -5

# View summaries
cat logs/checkstyle/*_summary_*.txt
```

#### Quality Logs
```bash
# View quality verifications
ls -la logs/quality/quality-check_*.log | tail -5

# View summaries
cat logs/quality/*_summary_*.txt
```

#### Gradle Logs
```bash
# View recent builds
ls -la logs/gradle/gradle_build_*.log | tail -5

# View recent tests
ls -la logs/gradle/gradle_test_*.log | tail -5
```

#### Docker Logs
```bash
# View Docker executions
ls -la logs/docker/docker-run_*.log | tail -5

# View Docker builds
ls -la logs/docker/docker-build-test_*.log | tail -5
```

#### Environment Logs
```bash
# View environment tests
ls -la logs/environment/gradle-test_*.log | tail -5

# View startup tests
ls -la logs/environment/app-startup-test_*.log | tail -5
```

### Log Analysis

#### Error Search
```bash
# Search for errors in all logs
grep -i error logs/*/*.log

# Search for errors in specific category
grep -i error logs/checkstyle/*.log
```

#### Size Analysis
```bash
# Check size by category
du -h logs/*/

# Check for large files
find logs/ -name "*.log" -size +1M
```

#### Old Logs Cleanup
```bash
# Remove logs older than 30 days
find logs/ -name "*.log" -mtime +30 -delete

# Remove summaries older than 7 days
find logs/ -name "*_summary_*.txt" -mtime +7 -delete
```

## Benefits of Organization

### 1. Easy Location
- Logs organized by type and purpose
- Intuitive navigation by category
- Efficient search for specific problems

### 2. Simplified Maintenance
- Selective cleanup by category
- Specific backup by type
- Focused analysis by area

### 3. Improved Debugging
- Isolation of problems by category
- Organized history of executions
- Easy comparison between different types of logs

### 4. Improved Observability
- Clear view of each area's state
- Trend tracking by category
- Quick identification of patterns

## Troubleshooting

### Common Issues

1. **Logs not appearing**:
   - Check log level configuration
   - Verify logger name matches package structure
   - Ensure appenders are properly configured

2. **Performance issues**:
   - Reduce log level in production
   - Use async appenders for file logging
   - Monitor log file sizes

3. **Disk space issues**:
   - Check log rotation settings
   - Verify retention period
   - Monitor log file growth

### Debugging Log Configuration
- Enable debug logging for Logback: `logging.level.ch.qos.logback=DEBUG`
- Check for configuration errors in startup logs
- Verify profile-specific settings are applied

## Customization

### Adding New Loggers
Add to `logback-spring.xml`:
```xml
<logger name="com.finance_control.your.package" level="DEBUG"/>
```

### Modifying Log Levels
Update in `application.properties`:
```properties
logging.level.com.finance_control.your.package=DEBUG
```

### Custom Appenders
Extend the configuration in `logback-spring.xml` to add:
- Database appenders for centralized logging
- Network appenders for remote logging
- Custom formatters for specific requirements

## Extending Logging

If you need additional logging in a specific service or controller:

```java
@Service
public class CustomService extends BaseService<Entity, Long, EntityDTO> {

    public void customBusinessMethod() {
        log.debug("Starting custom business method");
        // Business logic
        log.info("Custom business method completed successfully");
    }
}
```

The base class logging will handle all standard CRUD operations, while you can add domain-specific logging as needed.

## Docker Integration

### Mounted Volumes
```yaml
volumes:
  - ./logs:/app/logs
```

### Access to Organized Logs
```bash
# Inside container
ls -la /app/logs/application/
ls -la /app/logs/checkstyle/

# From host
ls -la logs/application/
ls -la logs/checkstyle/
```

## Maintenance and Cleanup

### Automated Cleanup Script
```bash
#!/bin/bash
# clean-logs.sh

# Clean old logs by category
find logs/application/ -name "*.log" -mtime +30 -delete
find logs/checkstyle/ -name "*.log" -mtime +7 -delete
find logs/quality/ -name "*.log" -mtime +7 -delete
find logs/gradle/ -name "*.log" -mtime +7 -delete
find logs/docker/ -name "*.log" -mtime +7 -delete
find logs/environment/ -name "*.log" -mtime +7 -delete

# Clean old summaries
find logs/ -name "*_summary_*.txt" -mtime +3 -delete
```

### Backup by Category
```bash
#!/bin/bash
# backup-logs.sh

timestamp=$(date +"%Y%m%d_%H%M%S")

# Backup by category
tar -czf logs-backup-application-${timestamp}.tar.gz logs/application/
tar -czf logs-backup-checkstyle-${timestamp}.tar.gz logs/checkstyle/
tar -czf logs-backup-quality-${timestamp}.tar.gz logs/quality/
tar -czf logs-backup-gradle-${timestamp}.tar.gz logs/gradle/
tar -czf logs-backup-docker-${timestamp}.tar.gz logs/docker/
tar -czf logs-backup-environment-${timestamp}.tar.gz logs/environment/

# Complete backup
tar -czf logs-backup-complete-${timestamp}.tar.gz logs/
```

## Conclusion

Organizing logs in subdirectories by type and script provides:

- **Clear Organization**: Each type of log has its specific location
- **Simplified Maintenance**: Selective cleanup and backup
- **Improved Debugging**: Quick problem location
- **Improved Observability**: Organized system view
- **Scalability**: Structure that grows with the project

All scripts were updated to use the new structure, maintaining existing functionality while significantly improving organization and ease of use.
