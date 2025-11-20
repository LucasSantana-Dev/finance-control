# Postman Collection Testing Guide

## Overview

This guide explains how to test the Finance Control API using the comprehensive Postman collection that has been created with 50+ endpoints organized by feature modules.

## Collection File Location

The comprehensive Postman collection has been created at:
```
postman/FinanceControl-Comprehensive.postman_collection.json
```

## Prerequisites

1. **Postman Desktop Application** installed
2. **Application Running** on `http://localhost:8080`
3. **Database Access** - Supabase is configured as the database

## Starting the Application

### Option 1: Using Gradle (Recommended for Testing)

```bash
cd /Users/lucassantana/Desenvolvimento/finance-control
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Option 2: Using Docker Compose

```bash
cd /Users/lucassantana/Desenvolvimento/finance-control
docker compose up app -d
```

### Verify Application is Running

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## Importing the Postman Collection

1. Open Postman
2. Click **Import** button (top left)
3. Select **File** tab
4. Navigate to: `postman/FinanceControl-Comprehensive.postman_collection.json`
5. Click **Import**

## Collection Structure

The collection is organized into the following folders:

1. **Authentication** - Register, Login, Validate Token, Change Password
2. **Users** - User CRUD operations and management
3. **Dashboard** - Financial metrics, summaries, predictions
4. **Transactions** - Transaction management and bank statement import
5. **Financial Goals** - Goal tracking and progress updates
6. **Brazilian Market** - Economic indicators (Selic, CDI, IPCA)
7. **Monitoring** - Health checks, alerts, metrics
8. **Data Export** - Export user data in CSV/JSON formats

## Collection Variables

The collection includes pre-configured variables:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `baseUrl` | `http://localhost:8080` | API base URL |
| `authToken` | (auto-populated) | JWT authentication token |
| `userId` | (auto-populated) | Authenticated user ID |
| `testEmail` | `test@example.com` | Test user email |
| `testPassword` | `TestPassword123!` | Test user password |
| `transactionId` | (auto-populated) | Created transaction ID |
| `goalId` | (auto-populated) | Created financial goal ID |
| `timestamp` | (auto-populated) | Current timestamp |

## Sequential Testing Flow

### 1. Authentication Flow

Execute requests in this order:

#### 1.1 Register User
- **Request**: `POST /auth/register`
- **Body**:
```json
{
  "email": "{{testEmail}}",
  "password": "{{testPassword}}",
  "isActive": true
}
```
- **Expected**: HTTP 200, user ID auto-saved to `userId` variable

#### 1.2 Login
- **Request**: `POST /auth/login`
- **Body**:
```json
{
  "email": "{{testEmail}}",
  "password": "{{testPassword}}"
}
```
- **Expected**: HTTP 200, token and userId auto-saved
- **Post-test Script**: Automatically extracts token and saves to collection variables

#### 1.3 Validate Token
- **Request**: `POST /auth/validate`
- **Headers**: `Authorization: Bearer {{authToken}}`
- **Expected**: HTTP 200, validates the JWT token

### 2. User Management

#### 2.1 List Users
- **Request**: `GET /users?page=0&size=20`
- **Expected**: HTTP 200, paginated list of users

#### 2.2 Get User by ID
- **Request**: `GET /users/{{userId}}`
- **Expected**: HTTP 200, user details

#### 2.3 Get User by Email
- **Request**: `GET /users/email/{{testEmail}}`
- **Expected**: HTTP 200, user details

### 3. Dashboard Metrics

#### 3.1 Get Dashboard Summary
- **Request**: `GET /dashboard/summary`
- **Expected**: HTTP 200, comprehensive financial summary

#### 3.2 Get Financial Metrics
- **Request**: `GET /dashboard/metrics?startDate=2024-01-01&endDate=2024-12-31`
- **Expected**: HTTP 200, detailed financial metrics

#### 3.3 Get Top Spending Categories
- **Request**: `GET /dashboard/spending-categories?limit=10`
- **Expected**: HTTP 200, top categories with amounts

### 4. Transaction Management

#### 4.1 Create Transaction
- **Request**: `POST /transactions`
- **Body**:
```json
{
  "type": "EXPENSE",
  "subtype": "VARIABLE",
  "source": "CREDIT_CARD",
  "description": "Test transaction",
  "amount": 100.00,
  "date": "2024-11-15T10:00:00",
  "categoryId": 1,
  "userId": {{userId}},
  "responsibilities": [
    {
      "responsibleId": {{userId}},
      "percentage": 100
    }
  ]
}
```
- **Expected**: HTTP 200/201, transaction ID auto-saved

#### 4.2 Get Transaction by ID
- **Request**: `GET /transactions/{{transactionId}}`
- **Expected**: HTTP 200, transaction details

#### 4.3 List Transactions
- **Request**: `GET /transactions?page=0&size=20`
- **Expected**: HTTP 200, paginated transactions

#### 4.4 Get Filtered Transactions
- **Request**: `GET /transactions/filtered?page=0&size=20&sortBy=date&sortDirection=desc`
- **Expected**: HTTP 200, sorted and filtered transactions

### 5. Financial Goals

#### 5.1 Create Financial Goal
- **Request**: `POST /financial-goals`
- **Body**:
```json
{
  "name": "Emergency Fund",
  "description": "Build emergency reserve fund",
  "goalType": "SAVINGS",
  "targetAmount": 10000.00,
  "currentAmount": 0,
  "isActive": true,
  "deadline": "2025-12-31T23:59:59"
}
```
- **Expected**: HTTP 200/201, goal ID auto-saved

#### 5.2 Get Active Goals
- **Request**: `GET /financial-goals/active`
- **Expected**: HTTP 200, list of active goals

#### 5.3 Update Goal Progress
- **Request**: `POST /financial-goals/{{goalId}}/progress?amount=500.00`
- **Expected**: HTTP 200, updated goal with new current amount

#### 5.4 Mark Goal as Completed
- **Request**: `POST /financial-goals/{{goalId}}/complete`
- **Expected**: HTTP 200, goal marked as completed

### 6. Brazilian Market Data

#### 6.1 Get Selic Rate
- **Request**: `GET /brazilian-market/indicators/selic`
- **Expected**: HTTP 200, current Selic rate

#### 6.2 Get CDI Rate
- **Request**: `GET /brazilian-market/indicators/cdi`
- **Expected**: HTTP 200, current CDI rate

#### 6.3 Get IPCA
- **Request**: `GET /brazilian-market/indicators/ipca`
- **Expected**: HTTP 200, current IPCA inflation rate

#### 6.4 Get All Indicators
- **Request**: `GET /brazilian-market/indicators`
- **Expected**: HTTP 200, all key economic indicators

### 7. Monitoring

#### 7.1 Get Health Status
- **Request**: `GET /monitoring/health`
- **Expected**: HTTP 200, health status information

#### 7.2 Get Active Alerts
- **Request**: `GET /monitoring/alerts`
- **Expected**: HTTP 200, list of active alerts

#### 7.3 Get Metrics Summary
- **Request**: `GET /monitoring/metrics/summary`
- **Expected**: HTTP 200, application metrics summary

### 8. Data Export

#### 8.1 Export All Data as JSON
- **Request**: `GET /export/all/json`
- **Expected**: HTTP 200, JSON file with all user data

#### 8.2 Export Transactions as CSV
- **Request**: `GET /export/transactions/csv`
- **Expected**: HTTP 200, CSV file with transactions

#### 8.3 Export Goals as CSV
- **Request**: `GET /export/goals/csv`
- **Expected**: HTTP 200, CSV file with financial goals

## Running Collection with Newman (CLI)

You can also run the entire collection from the command line using Newman:

### Install Newman

```bash
npm install -g newman
```

### Run Collection

```bash
newman run postman/FinanceControl-Comprehensive.postman_collection.json \
  --environment postman/dev-environment.json \
  --reporters cli,html \
  --reporter-html-export newman-report.html
```

## Automated Test Scripts

The collection includes automated test scripts that:

1. **Auto-extract tokens** from login responses
2. **Auto-save IDs** for created resources (transactions, goals)
3. **Validate response status codes**
4. **Validate response structure**

### Example Test Script (Login Request)

```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.collectionVariables.set("authToken", jsonData.token);
    pm.collectionVariables.set("userId", jsonData.userId);
    console.log("Token saved: " + jsonData.token.substring(0, 20) + "...");
}
```

## Common Issues and Solutions

### Issue 1: Authentication Token Expired

**Solution**: Re-run the Login request to get a new token.

### Issue 2: User Already Exists

**Solution**: Either:
- Delete the existing user first
- Change `testEmail` variable to a different email
- Use Login instead of Register

### Issue 3: Category/Subcategory Not Found

**Solution**: Ensure you have seed data populated in the database, or create categories first.

### Issue 4: Connection Refused

**Solution**: Verify the application is running:
```bash
curl http://localhost:8080/actuator/health
```

If not running, start the application using one of the methods in "Starting the Application" section.

## Expected Test Results

When running the entire collection sequentially, you should see:

- ✅ **Authentication**: 4 requests, all passing
- ✅ **Users**: 7 requests, all passing
- ✅ **Dashboard**: 7 requests, all passing
- ✅ **Transactions**: 7 requests, all passing
- ✅ **Financial Goals**: 9 requests, all passing
- ✅ **Brazilian Market**: 5 requests, all passing
- ✅ **Monitoring**: 5 requests, all passing
- ✅ **Data Export**: 4 requests, all passing

**Total**: 48+ requests

## Advanced Testing

### Testing with Different Environments

Create environment-specific variable files:

- `dev-environment.json` - Development environment
- `staging-environment.json` - Staging environment
- `prod-environment.json` - Production environment

### Continuous Integration

Integrate Newman with your CI/CD pipeline:

```yaml
# .github/workflows/api-tests.yml
name: API Tests

on: [push, pull_request]

jobs:
  api-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Run API Tests
        run: |
          npm install -g newman
          newman run postman/FinanceControl-Comprehensive.postman_collection.json
```

## Documentation and Support

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **GitHub Repository**: https://github.com/LucasSantana/finance-control

## Next Steps

1. Import the collection into Postman
2. Start the application
3. Run the Authentication folder requests first
4. Proceed sequentially through other folders
5. Monitor responses and verify expected results
6. Document any issues or failures
7. Update collection as needed based on actual API behavior

## Notes

- All requests require authentication except:
  - `POST /auth/register`
  - `POST /auth/login`
  - `POST /monitoring/frontend-errors`
  - `GET /monitoring/health`

- The collection uses automatic variable extraction, so tokens and IDs are managed automatically

- Test data cleanup may be needed between test runs to ensure consistent results
