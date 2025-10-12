# Finance Control API - Postman Testing Guide

## Overview

This document provides comprehensive testing instructions for the Finance Control API using the Postman collection. The collection includes all major endpoints for authentication, investments, market data, transactions, financial goals, and dashboard analytics.

## Collection Details

- **Collection Name**: Finance Control API - Complete Test Suite
- **Environment**: Finance Control - Local Development

## Prerequisites

1. **Finance Control API Running**: Ensure the Spring Boot application is running on `http://localhost:8080`
2. **Postman Installed**: Download and install Postman from [postman.com](https://www.postman.com/downloads/)
3. **Database Setup**: Ensure PostgreSQL database is running and migrations are applied

## Setup Instructions

### 1. Import Collection and Environment

1. Open Postman
2. Click "Import" button
3. Import the collection using the collection ID: `afd6a4f2-21d4-447b-9859-5938c0a62cf1`
4. Import the environment using the environment ID: `782e2e82-6c9b-4e88-b337-d88806a516b0`

### 2. Configure Environment Variables

The environment includes the following variables:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `base_url` | `http://localhost:8080` | Base URL for Finance Control API |
| `jwt_token` | (empty) | JWT token for authentication (auto-populated) |
| `user_id` | (empty) | Current user ID (auto-populated) |
| `test_email` | `test@example.com` | Test user email |
| `test_password` | `password123` | Test user password |

## Testing Workflow

### Step 1: Authentication Setup

1. **User Registration** (Optional)
   - Run "User Registration" request
   - Creates a new test user account
   - Use this if you don't have an existing test user

2. **User Login**
   - Run "User Login" request
   - This will automatically extract the JWT token and user ID
   - The token will be set in environment variables for subsequent requests

3. **Validate Token** (Optional)
   - Run "Validate Token" request
   - Verifies that the JWT token is valid

### Step 2: Investment Management Testing

1. **Create Investment**
   - Creates a new investment (VALE3 stock example)
   - Tests investment creation with all required fields

2. **Get All Investments**
   - Retrieves all investments for the authenticated user
   - Tests pagination with page=0&size=10

3. **Get Investment by ID**
   - Retrieves a specific investment by ID
   - Tests investment retrieval

4. **Get Investment by Ticker**
   - Retrieves investment by ticker symbol
   - Tests ticker-based lookup

5. **Update Investment**
   - Updates an existing investment
   - Tests investment modification

6. **Search Investments**
   - Searches investments by name or ticker
   - Tests search functionality

7. **Get Investments by Type**
   - Filters investments by type (STOCK, FII, ETF, etc.)
   - Tests type-based filtering

8. **Update Market Data**
   - Updates market data for a specific investment
   - Tests external API integration

9. **Get Portfolio Summary**
   - Retrieves portfolio summary with market values
   - Tests portfolio analytics

10. **Delete Investment**
    - Deletes an investment (soft delete)
    - Tests investment removal

### Step 3: Brazilian Market Data Testing

1. **Get Current Selic Rate**
   - Retrieves current Selic interest rate from BCB
   - Tests Brazilian market indicators

2. **Get Current CDI Rate**
   - Retrieves current CDI interest rate from BCB
   - Tests Brazilian market indicators

3. **Get Current IPCA**
   - Retrieves current IPCA inflation rate from BCB
   - Tests Brazilian market indicators

### Step 4: Transaction Management Testing

1. **Create Transaction**
   - Creates a new financial transaction
   - Tests transaction creation with categories and tags

2. **Get All Transactions**
   - Retrieves all transactions for the authenticated user
   - Tests pagination and transaction listing

3. **Reconcile Transaction**
   - Reconciles a transaction with bank statement
   - Tests transaction reconciliation workflow

### Step 5: Financial Goals Testing

1. **Create Financial Goal**
   - Creates a new financial goal
   - Tests goal creation with target amounts and dates

2. **Get All Financial Goals**
   - Retrieves all financial goals for the authenticated user
   - Tests goal listing and pagination

### Step 6: Dashboard Analytics Testing

1. **Get Dashboard Summary**
   - Retrieves comprehensive dashboard summary
   - Tests key financial metrics aggregation

2. **Get Financial Metrics**
   - Retrieves detailed financial metrics for a period
   - Tests date-range based analytics

3. **Get Top Spending Categories**
   - Retrieves top spending categories for visualization
   - Tests spending analysis and categorization

## Test Data Examples

### Investment Creation Example
```json
{
  "ticker": "VALE3",
  "name": "Vale S.A.",
  "type": "STOCK",
  "subtype": "COMMON",
  "sector": "Materials",
  "industry": "Metals & Mining",
  "exchange": "B3",
  "currency": "BRL",
  "quantity": 100,
  "averagePrice": 65.50
}
```

### Transaction Creation Example
```json
{
  "description": "Grocery shopping",
  "amount": 150.50,
  "type": "EXPENSE",
  "category": "Food & Dining",
  "date": "2024-10-11",
  "tags": ["groceries", "supermarket"]
}
```

### Financial Goal Creation Example
```json
{
  "name": "Emergency Fund",
  "description": "Build emergency fund for 6 months of expenses",
  "targetAmount": 50000.00,
  "currentAmount": 15000.00,
  "targetDate": "2025-12-31",
  "category": "SAVINGS"
}
```

## Expected Response Codes

| Endpoint Category | Success Codes | Error Codes |
|------------------|---------------|-------------|
| Authentication | 200, 201 | 400, 401, 409 |
| Investments | 200, 201, 204 | 400, 401, 404, 409 |
| Market Data | 200 | 400, 401, 500 |
| Transactions | 200, 201, 204 | 400, 401, 404 |
| Financial Goals | 200, 201, 204 | 400, 401, 404 |
| Dashboard | 200 | 400, 401, 500 |

## Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Ensure you've run the "User Login" request first
   - Check that the JWT token is properly set in environment variables
   - Verify the token hasn't expired

2. **404 Not Found**
   - Ensure the API is running on the correct port (8080)
   - Check that the endpoint URL is correct
   - Verify the resource ID exists

3. **500 Internal Server Error**
   - Check application logs for detailed error information
   - Ensure database is running and accessible
   - Verify external API connections (for market data)

4. **Connection Refused**
   - Ensure the Finance Control API is running
   - Check that the port 8080 is not blocked
   - Verify the base_url environment variable

### Environment Variable Issues

If environment variables are not being set automatically:

1. Manually set the `jwt_token` variable after login
2. Copy the token from the login response
3. Update the environment variable in Postman

## API Documentation

The Finance Control API includes comprehensive OpenAPI/Swagger documentation available at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## Performance Testing

For performance testing:

1. Use Postman's Collection Runner
2. Set appropriate delays between requests
3. Monitor response times and error rates
4. Test with various data volumes

## Security Testing

The collection includes security testing scenarios:

1. **Authentication Testing**
   - Test with invalid credentials
   - Test with expired tokens
   - Test with malformed tokens

2. **Authorization Testing**
   - Test access to other users' data
   - Test unauthorized operations
   - Test role-based access control

## Continuous Integration

For CI/CD integration:

1. Export the collection as JSON
2. Use Newman (Postman CLI) for automated testing
3. Integrate with your CI/CD pipeline
4. Set up automated test reports

## Support

For issues or questions:

1. Check the application logs
2. Review the API documentation
3. Verify environment configuration
4. Test individual endpoints manually

## Collection Statistics

- **Total Requests**: 24
- **Authentication**: 3 requests
- **Investments**: 10 requests
- **Market Data**: 3 requests
- **Transactions**: 3 requests
- **Financial Goals**: 2 requests
- **Dashboard**: 3 requests

This comprehensive test suite ensures all major functionality of the Finance Control API is properly tested and validated.
