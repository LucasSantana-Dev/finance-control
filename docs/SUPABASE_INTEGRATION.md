# Supabase Realtime Integration Guide

This document provides comprehensive information about the Supabase Realtime integration in the Finance Control application, including setup, configuration, usage, and API documentation.

## Overview

The Finance Control application integrates Supabase Realtime features alongside the existing PostgreSQL database and JWT authentication system. This integration enables:

- **Realtime Updates**: Real-time dashboard updates, transaction notifications, and goal progress updates
- **Live Notifications**: Instant alerts for financial events and goal milestones
- **Seamless Integration**: Works alongside existing database and authentication systems

## Architecture

### Integration Points

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │    Supabase     │    │     Redis       │
│   (Transactions)│    │    Realtime     │    │   (Cache)       │
│   (Goals)       │    │   Messaging     │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Spring Boot   │
                    │  Application   │
                    └─────────────────┘
```

### Components

1. **SupabaseConfig**: Configuration class for Supabase realtime client beans
2. **SupabaseRealtimeService**: Service for realtime messaging and subscriptions
3. **RealtimeController**: REST API for realtime subscription management

## Setup and Configuration

### Prerequisites

1. **Supabase Project**: Create a Supabase project at [supabase.com](https://supabase.com)
2. **API Keys**: Obtain your project URL and API keys from the Supabase dashboard

### Environment Variables

Add the following environment variables to your `.env` file:

Or for Docker deployment in `docker.env`:

```env
# Supabase Configuration
SUPABASE_ENABLED=true
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=${SUPABASE_ANON_KEY}
SUPABASE_REALTIME_ENABLED=true
```

### Supabase Project Setup

1. **Enable Realtime**:
   - Realtime is enabled by default in Supabase
   - Configure realtime channels as needed

## Configuration Details

### AppProperties Configuration

The application uses typed configuration properties:

```java
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Supabase supabase = new Supabase();

    @Data
    public static class Supabase {
        private boolean enabled = true;
        private String url = "";
        private String anonKey = "";
        private Realtime realtime = new Realtime();

        @Data
        public static class Realtime {
            private boolean enabled = true;
            private String[] channels = {"transactions", "dashboard", "goals"};
            private int reconnectDelay = 1000; // milliseconds
            private int maxReconnectAttempts = 5;
        }
    }
}
```

### Security Considerations

- **API Keys**: Never commit API keys to version control
- **Channel Access**: Only configured realtime channels are accessible
- **User Authentication**: All realtime operations require valid JWT authentication

## API Usage

### Realtime API

#### Get Status

```http
GET /api/realtime/status
```

**Response**:
```json
{
  "connected": true,
  "subscriptionCounts": {
    "transactions": 5,
    "dashboard": 3,
    "goals": 1
  },
  "timestamp": 1642156800000
}
```

#### Subscribe to Channel

```http
POST /api/realtime/subscribe/{channelName}?userId=123
```

**Response**:
```json
{
  "success": true,
  "channel": "transactions",
  "userId": 123,
  "message": "Subscribed to channel successfully",
  "timestamp": 1642156800000
}
```

#### Unsubscribe from Channel

```http
DELETE /api/realtime/unsubscribe/{channelName}?userId=123
```

**Response**:
```json
{
  "success": true,
  "channel": "transactions",
  "userId": 123,
  "message": "Unsubscribed from channel successfully",
  "timestamp": 1642156800000
}
```

#### Broadcast Message

```http
POST /api/realtime/broadcast/{channelName}
Content-Type: application/json

{
  "type": "custom_event",
  "data": {
    "message": "Custom broadcast message"
  }
}
```

#### Notify Transaction Update

```http
POST /api/realtime/notify/transaction?userId=123
Content-Type: application/json

{
  "id": 456,
  "amount": 100.50,
  "description": "Grocery shopping"
}
```

#### Notify Dashboard Update

```http
POST /api/realtime/notify/dashboard?userId=123
Content-Type: application/json

{
  "totalBalance": 5000.00,
  "monthlyIncome": 3000.00,
  "monthlyExpenses": 2000.00
}
```

#### Notify Goal Update

```http
POST /api/realtime/notify/goal?userId=123
Content-Type: application/json

{
  "id": 789,
  "progress": 75.5,
  "targetAmount": 10000.00
}
```

## WebSocket Integration

The realtime service integrates with Spring WebSocket for client-side realtime updates:

### Client Subscription

```javascript
// Subscribe to user-specific channel
stompClient.subscribe('/topic/transactions/user/123', function(message) {
    const data = JSON.parse(message.body);
    console.log('Transaction update:', data);
});

// Subscribe to broadcast channel
stompClient.subscribe('/topic/dashboard', function(message) {
    const data = JSON.parse(message.body);
    console.log('Dashboard update:', data);
});
```

### Message Format

All realtime messages follow this format:

```json
{
  "type": "transaction_update",
  "userId": 123,
  "data": {
    // Event-specific data
  },
  "timestamp": 1642156800000
}
```

## Service Integration

### Storage Service Usage

```java
@Service
public class TransactionService {

    private final SupabaseStorageService storageService;

    public void saveTransactionWithReceipt(Transaction transaction, MultipartFile receipt) {
        // Save transaction to database first
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Upload receipt to storage
        String fileName = "receipt-" + savedTransaction.getId() + ".jpg";
        UploadFileResponse response = storageService.uploadFile(
            "receipts",
            fileName,
            receipt.getBytes(),
            receipt.getContentType()
        );

        // Store file reference in database if needed
        // ...
    }
}
```

### Realtime Service Usage

```java
@Service
public class DashboardService {

    private final SupabaseRealtimeService realtimeService;

    public void updateDashboardMetrics(Long userId) {
        // Calculate new metrics
        DashboardMetrics metrics = calculateMetrics(userId);

        // Notify subscribers about dashboard update
        realtimeService.notifyDashboardUpdate(userId, metrics);
    }
}
```

## Error Handling

### Storage Errors

- **400 Bad Request**: Invalid file type, size, or bucket name
- **403 Forbidden**: Bucket access not allowed
- **413 Payload Too Large**: File exceeds maximum size limit
- **500 Internal Server Error**: Supabase service errors

### Realtime Errors

- **400 Bad Request**: Invalid channel name
- **403 Forbidden**: Insufficient permissions
- **503 Service Unavailable**: Realtime service disconnected

## Testing

### Unit Tests

Run Supabase-related unit tests:

```bash
./gradlew test --tests "*SupabaseStorageServiceTest"
./gradlew test --tests "*SupabaseRealtimeServiceTest"
```

### Integration Tests

For integration testing with actual Supabase services, ensure:

1. Test Supabase project is configured
2. API keys are available in test environment
3. Test buckets exist in Supabase project

### Mock Testing

For unit testing without Supabase connectivity:

```java
@Mock
private StorageClient storageClient;

@Mock
private SupabaseClient supabaseClient;

// Mock the client responses
when(storageClient.from(anyString())).thenReturn(storageFileAPI);
when(storageFileAPI.upload(anyString(), any(byte[].class), any()))
    .thenReturn(CompletableFuture.completedFuture(uploadResponse));
```

## Monitoring and Logging

### Metrics

The integration provides metrics for:

- File upload/download/delete operations
- Realtime connection status
- Subscription counts per channel
- Error rates for Supabase operations

### Logging

Logs are written at appropriate levels:

- **INFO**: Successful operations, connection status
- **WARN**: Configuration issues, recoverable errors
- **ERROR**: Failed operations, connection failures

## Troubleshooting

### Common Issues

1. **Connection Failed**: Check API keys and project URL
2. **Bucket Not Found**: Ensure buckets exist in Supabase project
3. **File Upload Failed**: Check file size and type restrictions
4. **Realtime Not Working**: Verify WebSocket configuration

### Debug Mode

Enable debug logging for Supabase operations:

```properties
logging.level.com.finance_control.shared.service.SupabaseStorageService=DEBUG
logging.level.com.finance_control.shared.service.SupabaseRealtimeService=DEBUG
```

### Health Checks

Check Supabase integration health:

```bash
curl http://localhost:8080/api/realtime/status
curl http://localhost:8080/actuator/health
```

## Security Best Practices

1. **API Key Management**: Store keys in environment variables, never in code
2. **File Validation**: Always validate file types and sizes before upload
3. **Access Control**: Use Row Level Security (RLS) in Supabase
4. **Rate Limiting**: Implement rate limiting for file operations
5. **Audit Logging**: Log all file operations for security auditing

## Performance Considerations

1. **File Size Limits**: Configure appropriate maximum file sizes
2. **Caching**: Use Redis caching for frequently accessed files
3. **Connection Pooling**: Supabase clients handle connection pooling internally
4. **Async Operations**: All Supabase operations are asynchronous
5. **Batch Operations**: Use batch operations when possible

## Future Enhancements

### Planned Features

1. **File Versioning**: Track file versions in Supabase Storage
2. **Advanced Realtime**: Implement presence and typing indicators
3. **File Compression**: Automatic image compression on upload
4. **CDN Integration**: Use Supabase CDN for faster file delivery
5. **Backup Integration**: Automated backup of files to external storage

### Integration Points

1. **Transaction Receipts**: Link uploaded receipts to transactions
2. **Document Management**: Store financial documents and reports
3. **User Avatars**: Store user profile pictures
4. **Export Files**: Store generated CSV/JSON export files
5. **Audit Trails**: Store audit logs and system backups

## Support and Resources

### Documentation Links

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Storage Docs](https://supabase.com/docs/guides/storage)
- [Supabase Realtime Docs](https://supabase.com/docs/guides/realtime)
- [Supabase Java Client](https://github.com/supabase-community/storage-java)

### Community Resources

- [Supabase Discord](https://discord.supabase.com)
- [Supabase GitHub](https://github.com/supabase/supabase)
- [Supabase Community Forums](https://github.com/supabase-community)

---

**Last Updated**: November 11, 2025
**Version**: 1.0.0
