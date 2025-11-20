# Finance Control API Testing Guide

## 🚀 Overview

This guide provides comprehensive testing instructions for all Finance Control API endpoints. The application includes both local authentication and Supabase integration features.

## 📋 Prerequisites

1. **Application Running**: Start the Spring Boot application:
   ```bash
   ./gradlew bootRun --no-configuration-cache --no-daemon
   ```

2. **Health Check**: Verify the application is running:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. **Environment Variables**: Ensure `.env` file contains:
   ```env
   SUPABASE_ENABLED=true
   SUPABASE_URL=your-supabase-url
   SUPABASE_ANON_KEY=your-anon-key
   SUPABASE_JWT_SIGNER=your-jwt-signer
   SUPABASE_SERVICE_ROLE_KEY=your-service-role-key
   SUPABASE_DATABASE_ENABLED=false  # Keep false for local testing
   ```

## 🧪 Testing Categories

### 1. Health & Monitoring

**Endpoint**: `GET /actuator/health`
```bash
curl http://localhost:8080/actuator/health
```

**Expected Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

### 2. Authentication (Local)

#### Register User
**Endpoint**: `POST /auth/register`
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

#### Login
**Endpoint**: `POST /auth/login`
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Expected Response**: Save the `token` value for authenticated requests.

### 3. Supabase Authentication

#### Supabase Signup
**Endpoint**: `POST /auth/supabase/signup`
```bash
curl -X POST http://localhost:8080/auth/supabase/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "supabase-test@example.com",
    "password": "password123"
  }'
```

#### Supabase Login
**Endpoint**: `POST /auth/supabase/login`
```bash
curl -X POST http://localhost:8080/auth/supabase/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "supabase-test@example.com",
    "password": "password123"
  }'
```

#### Get Current Supabase User
**Endpoint**: `GET /auth/supabase/me`
```bash
curl -X GET http://localhost:8080/auth/supabase/me \
  -H "Authorization: Bearer YOUR_SUPABASE_TOKEN"
```

### 4. Profile Management

#### Get Profile
**Endpoint**: `GET /profile`
```bash
curl -X GET http://localhost:8080/profile \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Update Profile
**Endpoint**: `PUT /profile`
```bash
curl -X PUT http://localhost:8080/profile \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Updated",
    "lastName": "Name",
    "phone": "+1234567890"
  }'
```

#### Upload Avatar
**Endpoint**: `POST /profile/avatar`
```bash
curl -X POST http://localhost:8080/profile/avatar \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/avatar.jpg"
```

### 5. Transaction Management

#### Get All Transactions
**Endpoint**: `GET /transactions`
```bash
curl -X GET "http://localhost:8080/transactions?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Create Transaction
**Endpoint**: `POST /transactions`
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Grocery shopping",
    "amount": 150.50,
    "type": "EXPENSE",
    "categoryId": 1,
    "date": "2024-01-15",
    "responsibles": [
      {
        "responsibleId": 1,
        "percentage": 100
      }
    ]
  }'
```

#### Get Transaction by ID
**Endpoint**: `GET /transactions/{id}`
```bash
curl -X GET http://localhost:8080/transactions/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Update Transaction
**Endpoint**: `PUT /transactions/{id}`
```bash
curl -X PUT http://localhost:8080/transactions/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated grocery shopping",
    "amount": 175.00,
    "type": "EXPENSE",
    "categoryId": 1,
    "date": "2024-01-15",
    "responsibles": [
      {
        "responsibleId": 1,
        "percentage": 100
      }
    ]
  }'
```

#### Delete Transaction
**Endpoint**: `DELETE /transactions/{id}`
```bash
curl -X DELETE http://localhost:8080/transactions/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Import Transactions (CSV)
**Endpoint**: `POST /transactions/import`
```bash
curl -X POST http://localhost:8080/transactions/import \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/transactions.csv" \
  -F 'config={
    "userId": 1,
    "defaultCategoryId": 1,
    "duplicateStrategy": "SKIP",
    "csv": {
      "containsHeader": true,
      "delimiter": ";",
      "dateColumn": "date",
      "descriptionColumn": "description",
      "amountColumn": "amount"
    }
  }'
```

### 6. Transaction Categories

#### Get All Categories
**Endpoint**: `GET /transactions/categories`
```bash
curl -X GET http://localhost:8080/transactions/categories \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Create Category
**Endpoint**: `POST /transactions/categories`
```bash
curl -X POST http://localhost:8080/transactions/categories \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Food & Dining",
    "type": "EXPENSE",
    "color": "#FF6B6B",
    "icon": "restaurant"
  }'
```

#### Update Category
**Endpoint**: `PUT /transactions/categories/{id}`
```bash
curl -X PUT http://localhost:8080/transactions/categories/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Food & Dining Updated",
    "type": "EXPENSE",
    "color": "#FF6B6B",
    "icon": "restaurant"
  }'
```

### 7. Financial Goals

#### Get All Goals
**Endpoint**: `GET /goals`
```bash
curl -X GET http://localhost:8080/goals \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Create Goal
**Endpoint**: `POST /goals`
```bash
curl -X POST http://localhost:8080/goals \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Emergency Fund",
    "description": "Save for emergencies",
    "targetAmount": 10000.00,
    "currentAmount": 2500.00,
    "targetDate": "2024-12-31",
    "category": "SAVINGS"
  }'
```

#### Update Goal Progress
**Endpoint**: `PUT /goals/{id}`
```bash
curl -X PUT http://localhost:8080/goals/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currentAmount": 3000.00
  }'
```

### 8. Dashboard & Analytics

#### Get Dashboard Data
**Endpoint**: `GET /dashboard`
```bash
curl -X GET http://localhost:8080/dashboard \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get Dashboard Predictions
**Endpoint**: `POST /dashboard/predictions`
```bash
curl -X POST http://localhost:8080/dashboard/predictions \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "horizonMonths": 6,
    "includeSavings": true,
    "includeInvestments": true,
    "context": "Planning for vacation next year"
  }'
```

#### Get Transaction Summary
**Endpoint**: `GET /dashboard/transactions/summary`
```bash
curl -X GET "http://localhost:8080/dashboard/transactions/summary?period=MONTH" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 9. Supabase Storage

#### Upload File
**Endpoint**: `POST /storage/upload`
```bash
curl -X POST http://localhost:8080/storage/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/document.pdf" \
  -F "bucket=documents"
```

#### Download File
**Endpoint**: `GET /storage/download/{filename}`
```bash
curl -X GET "http://localhost:8080/storage/download/document.pdf?bucket=documents" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output downloaded_file.pdf
```

#### Delete File
**Endpoint**: `DELETE /storage/delete/{filename}`
```bash
curl -X DELETE "http://localhost:8080/storage/delete/document.pdf?bucket=documents" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 10. Real-time Subscriptions

#### Subscribe to Channels
**Endpoint**: `POST /realtime/subscribe`
```bash
curl -X POST http://localhost:8080/realtime/subscribe \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "channels": ["transactions", "dashboard"]
  }'
```

#### Unsubscribe from Channels
**Endpoint**: `POST /realtime/unsubscribe`
```bash
curl -X POST http://localhost:8080/realtime/unsubscribe \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "channels": ["transactions"]
  }'
```

### 11. Brazilian Market Data

#### Get Market Indicators
**Endpoint**: `GET /market/indicators`
```bash
curl -X GET http://localhost:8080/market/indicators \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get FII Data
**Endpoint**: `GET /market/fii`
```bash
curl -X GET http://localhost:8080/market/fii \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get Stock Data
**Endpoint**: `GET /market/stocks`
```bash
curl -X GET http://localhost:8080/market/stocks \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 12. Supabase Database Migrations

Follow these steps whenever new scripts appear under `src/main/resources/db/migration` and Supabase is the active backend.

1. **Confirm available migrations**
   ```bash
   ls src/main/resources/db/migration
   ```
   Ensure the latest file (currently `V9__create_frontend_error_log.sql`) matches the numbering referenced in `CHANGELOG.md`.

2. **Run migrations (Method 1: Gradle tasks - Recommended)**
   ```bash
   # Export Supabase credentials
   export SUPABASE_DATABASE_ENABLED=true
   export SUPABASE_DATABASE_HOST=db.skxnqippsyskqbycqvky.supabase.co
   export SUPABASE_DATABASE_PORT=5432
   export SUPABASE_DATABASE_NAME=postgres
   export SUPABASE_DATABASE_USERNAME=postgres.skxnqippsyskqbycqvky
   export SUPABASE_DATABASE_PASSWORD='zw^7E6Q6#$zx2A3&'
   export SUPABASE_DATABASE_SSL_MODE=require

   # Check migration status
   ./gradlew flywayInfo

   # Run migrations
   ./gradlew flywayMigrate
   ```

3. **Run migrations (Method 2: Spring Boot auto-migration)**
   Spring Boot automatically runs Flyway migrations on startup when `FLYWAY_ENABLED=true`:
   ```bash
   # Set credentials in .env file
   SUPABASE_DATABASE_ENABLED=true
   SUPABASE_DATABASE_HOST=db.skxnqippsyskqbycqvky.supabase.co
   # ... other credentials

   # Start application - migrations run automatically
   ./gradlew bootRun
   ```

4. **Run migrations (Method 3: Docker Flyway CLI)**
   ```bash
   docker run --rm \
     -v "$PWD/src/main/resources/db/migration:/flyway/sql" \
     flyway/flyway:10 \
     -url="jdbc:postgresql://db.skxnqippsyskqbycqvky.supabase.co:5432/postgres?sslmode=require" \
     -user="postgres.skxnqippsyskqbycqvky" \
     -password='zw^7E6Q6#$zx2A3&' \
     -locations=filesystem:/flyway/sql \
     migrate
   ```

5. **Manual fallback (Method 4: Supabase SQL Editor)**
   - If other methods cannot be used, open the Supabase SQL editor and run the contents of `V9__create_frontend_error_log.sql`.
   - Record the execution in the Supabase change log for audit/auditability.

6. **Verify application of migrations**
   ```sql
   SELECT version, description, success, installed_on
   FROM flyway_schema_history
   ORDER BY installed_on DESC
   LIMIT 5;
   ```
   Confirm the latest row corresponds to `V9__create_frontend_error_log`.

7. **Schema validation**
   ```sql
   SELECT column_name, data_type
   FROM information_schema.columns
   WHERE table_name = 'frontend_error_log';

   SELECT COUNT(*) FROM frontend_error_log;
   ```
   These checks ensure the table exists and is available for ingestion dashboards.

Document any failures (network restrictions, credentials, etc.) so migrations can be replayed later.

## 📖 API Documentation

Access the complete API documentation at:
```
http://localhost:8080/swagger-ui.html
```

## 🔧 Troubleshooting

### Common Issues

1. **403 Forbidden**: Check your authorization token
2. **401 Unauthorized**: Token expired, login again
3. **404 Not Found**: Verify endpoint URL
4. **400 Bad Request**: Check request body format
5. **500 Internal Server Error**: Check application logs

### Database Issues

If using Supabase database:
1. Ensure `SUPABASE_DATABASE_ENABLED=true` in `.env`
2. Verify database credentials are correct
3. Check Supabase project status

### Real-time Issues

1. Ensure Supabase Realtime is enabled
2. Check WebSocket connections
3. Verify channel subscriptions

## 📝 Testing Checklist

- [ ] Health check passes
- [ ] Local authentication works (register/login)
- [ ] Supabase authentication works
- [ ] Profile CRUD operations work
- [ ] Transaction management works
- [ ] Category management works
- [ ] Financial goals work
- [ ] Dashboard data loads
- [ ] Storage operations work
- [ ] Real-time subscriptions work
- [ ] Market data loads
- [ ] All endpoints return expected status codes
- [ ] Error handling works correctly

## 🎯 Next Steps

1. **Frontend Integration**: Use these endpoints in your frontend application
2. **Load Testing**: Test with multiple concurrent users
3. **Security Testing**: Verify authentication and authorization
4. **Performance Testing**: Check response times and database performance
5. **Integration Testing**: Test end-to-end workflows
