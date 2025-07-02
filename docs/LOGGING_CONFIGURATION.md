# Logging Configuration

This document describes the logging configuration for the Finance Control application.

## Overview

The application uses Logback as the logging framework with a comprehensive configuration that provides:

- **Console logging** with colored output for development
- **File logging** with rotation and size limits
- **Error-only logging** for critical issues
- **Profile-specific configurations** for different environments
- **Performance optimization** with async logging

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

### Logging Best Practices

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