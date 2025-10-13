# Unified Endpoints Guide

This document describes the unified endpoint approach implemented across all modules in the Finance Control application. Instead of having multiple specific endpoints for each type of operation, we now have flexible, query-parameter-based endpoints that can handle various use cases.

## Overview

The unified endpoint approach provides:

1. **Flexibility**: Single endpoint can handle multiple use cases including metadata
2. **Consistency**: Uniform parameter naming and response format
3. **Extensibility**: Easy to add new sorting/filtering options
4. **Maintainability**: Less code duplication
5. **API Discoverability**: Clear parameter documentation
6. **Simplicity**: No need for separate metadata endpoints

## Module Endpoints

### 1. Transactions Module

#### Transactions Endpoint
**`GET /transactions`**

Retrieve transactions with flexible filtering, sorting, and pagination options, or metadata.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `userId` | Long | No | User ID for filtering | `1` |
| `type` | TransactionType | No | Transaction type filter | `INCOME`, `EXPENSE` |
| `categoryId` | Long | No | Category ID filter | `1` |
| `subcategoryId` | Long | No | Subcategory ID filter | `1` |
| `sourceEntityId` | Long | No | Source entity ID filter | `1` |
| `search` | String | No | Search term for description | `grocery` |
| `sortBy` | String | No | Sort field | `createdAt`, `amount`, `date` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |
| `minAmount` | BigDecimal | No | Minimum amount filter | `100.00` |
| `maxAmount` | BigDecimal | No | Maximum amount filter | `1000.00` |
| `startDate` | LocalDate | No | Start date filter | `2024-01-01` |
| `endDate` | LocalDate | No | End date filter | `2024-12-31` |

**Examples:**
```bash
# Get all transactions for user 1
GET /transactions?userId=1

# Get expense transactions sorted by amount descending
GET /transactions?userId=1&type=EXPENSE&sortBy=amount&sortDirection=desc

# Get transactions in a date range
GET /transactions?userId=1&startDate=2024-01-01&endDate=2024-01-31

# Search for transactions containing "grocery"
GET /transactions?userId=1&search=grocery

# Get transaction categories (metadata)
GET /transactions?userId=1&data=categories

# Get transaction types (metadata)
GET /transactions?data=types

# Get monthly summary (metadata)
GET /transactions?userId=1&data=monthly-summary&startDate=2024-01-01&endDate=2024-12-31
```

### 2. Financial Goals Module

#### Financial Goals Endpoint
**`GET /financial-goals`**

Retrieve financial goals with flexible filtering, sorting, and pagination options, or metadata.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `userId` | Long | No | User ID for filtering | `1` |
| `goalType` | GoalType | No | Goal type filter | `SAVINGS`, `INVESTMENT`, `DEBT_PAYOFF` |
| `status` | String | No | Status filter | `active`, `completed` |
| `search` | String | No | Search term for name or description | `vacation` |
| `sortBy` | String | No | Sort field | `deadline`, `name`, `targetAmount` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |
| `minTargetAmount` | BigDecimal | No | Minimum target amount filter | `1000.00` |
| `maxTargetAmount` | BigDecimal | No | Maximum target amount filter | `50000.00` |
| `deadlineStart` | LocalDate | No | Deadline start date filter | `2024-01-01` |
| `deadlineEnd` | LocalDate | No | Deadline end date filter | `2024-12-31` |
| `data` | String | No | Type of data to retrieve (metadata types: types, status-summary, progress-summary, deadline-alerts, completion-rate, average-completion-time) | `types`, `status-summary` |

**Examples:**
```bash
# Get all financial goals for user 1
GET /financial-goals?userId=1

# Get active savings goals sorted by deadline
GET /financial-goals?userId=1&goalType=SAVINGS&status=active&sortBy=deadline&sortDirection=asc

# Get goal types (metadata)
GET /financial-goals?data=types

# Get status summary (metadata)
GET /financial-goals?userId=1&data=status-summary
```

### 2. Financial Goals Module

#### Unified Financial Goals Endpoint
**`GET /financial-goals`**

Retrieve financial goals with flexible filtering, sorting, and pagination options.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `userId` | Long | No | User ID for filtering | `1` |
| `goalType` | GoalType | No | Goal type filter | `SAVINGS`, `INVESTMENT`, `DEBT_PAYOFF` |
| `status` | String | No | Status filter | `active`, `completed` |
| `search` | String | No | Search term for name or description | `vacation` |
| `sortBy` | String | No | Sort field | `deadline`, `targetAmount`, `currentAmount` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |
| `minTargetAmount` | BigDecimal | No | Minimum target amount filter | `1000.00` |
| `maxTargetAmount` | BigDecimal | No | Maximum target amount filter | `10000.00` |
| `deadlineStart` | LocalDate | No | Deadline start date filter | `2024-01-01` |
| `deadlineEnd` | LocalDate | No | Deadline end date filter | `2024-12-31` |

**Examples:**
```bash
# Get all active goals for user
GET /financial-goals?userId=1&status=active

# Get savings goals sorted by deadline
GET /financial-goals?userId=1&goalType=SAVINGS&sortBy=deadline&sortDirection=asc

# Get goals with target amount between 1000 and 10000
GET /financial-goals?userId=1&minTargetAmount=1000&maxTargetAmount=10000
```

#### Unified Financial Goals Metadata Endpoint
**`GET /financial-goals/metadata`**

Retrieve financial goals metadata and analytics data.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `userId` | Long | No | User ID for filtering | `1` |
| `data` | String | Yes | Type of metadata to retrieve | `types`, `status-summary`, `progress-summary`, `deadline-alerts`, `completion-rate`, `average-completion-time` |
| `goalType` | GoalType | No | Goal type filter | `SAVINGS`, `INVESTMENT` |
| `status` | String | No | Status filter | `active`, `completed` |

**Examples:**
```bash
# Get all goal types
GET /financial-goals/metadata?data=types

# Get status summary for user
GET /financial-goals/metadata?userId=1&data=status-summary

# Get deadline alerts for user
GET /financial-goals/metadata?userId=1&data=deadline-alerts
```

### 3. Investments Module

#### Investments Endpoint
**`GET /investments`**

Retrieve investments with flexible filtering, sorting, and pagination options, or metadata.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `type` | InvestmentType | No | Investment type filter | `STOCK`, `FII`, `BOND`, `ETF`, `CRYPTO` |
| `subtype` | InvestmentSubtype | No | Investment subtype filter | `ORDINARY`, `PREFERRED`, `TIJOLO`, `PAPEL` |
| `sector` | String | No | Sector filter | `Energy`, `Technology`, `Finance` |
| `industry` | String | No | Industry filter | `Oil & Gas`, `Software`, `Banking` |
| `exchange` | String | No | Exchange filter | `B3`, `NYSE`, `NASDAQ` |
| `search` | String | No | Search term for ticker or name | `PETR4`, `VALE3` |
| `sortBy` | String | No | Sort field | `createdAt`, `currentPrice`, `dayChangePercent`, `dividendYield` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |
| `minPrice` | BigDecimal | No | Minimum current price filter | `10.00` |
| `maxPrice` | BigDecimal | No | Maximum current price filter | `100.00` |
| `minDividendYield` | BigDecimal | No | Minimum dividend yield filter | `5.0` |
| `maxDividendYield` | BigDecimal | No | Maximum dividend yield filter | `15.0` |
| `data` | String | No | Type of data to retrieve (metadata) | `sectors`, `industries`, `types`, `subtypes`, `exchanges`, `top-performers`, `worst-performers`, `top-dividend-yield`, `portfolio-summary` |

**Examples:**
```bash
# Get all investments for authenticated user
GET /investments

# Get stock investments sorted by current price descending
GET /investments?type=STOCK&sortBy=currentPrice&sortDirection=desc

# Get investments with high dividend yield
GET /investments?minDividendYield=8.0&sortBy=dividendYield&sortDirection=desc

# Search for specific ticker
GET /investments?search=PETR4

# Get investments by sector with price range
GET /investments?sector=Energy&minPrice=20.00&maxPrice=50.00

# Get portfolio summary metadata
GET /investments?data=portfolio-summary

# Get top performing investments
GET /investments?data=top-performers&page=0&size=10

# Get available sectors
GET /investments?data=sectors

# Get investment subtypes for stocks
GET /investments?data=subtypes&type=STOCK

# Get supported exchanges
GET /investments?data=exchanges
```

**Metadata Types:**
- `sectors`: Returns list of available sectors
- `industries`: Returns list of available industries
- `types`: Returns list of available investment types
- `subtypes`: Returns list of available subtypes (requires `type` parameter)
- `exchanges`: Returns map of supported exchanges
- `top-performers`: Returns investments with highest day change
- `worst-performers`: Returns investments with lowest day change
- `top-dividend-yield`: Returns investments with highest dividend yield
- `portfolio-summary`: Returns portfolio summary with total market value and breakdown

### 4. Dashboard Module

#### Unified Dashboard Endpoint
**`GET /dashboard`**

Retrieve dashboard data with flexible filtering and date range options.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `data` | String | Yes | Type of dashboard data to retrieve | `summary`, `metrics`, `spending-categories`, `monthly-trends`, `current-month-metrics`, `year-to-date-metrics` |
| `startDate` | LocalDate | No | Start date (required for metrics) | `2024-01-01` |
| `endDate` | LocalDate | No | End date (required for metrics) | `2024-12-31` |
| `limit` | Integer | No | Number of items to return (for lists) | `5`, `10`, `20` |
| `months` | Integer | No | Number of months for trends | `6`, `12`, `24` |

**Examples:**
```bash
# Get dashboard summary
GET /dashboard?data=summary

# Get financial metrics for date range
GET /dashboard?data=metrics&startDate=2024-01-01&endDate=2024-12-31

# Get top 10 spending categories
GET /dashboard?data=spending-categories&limit=10

# Get monthly trends for last 6 months
GET /dashboard?data=monthly-trends&months=6
```

### 5. Transaction Categories Module

#### Unified Transaction Categories Endpoint
**`GET /transaction-categories`**

Retrieve transaction categories with flexible filtering, sorting, and pagination options.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `search` | String | No | Search term for name | `food` |
| `sortBy` | String | No | Sort field | `name`, `createdAt` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |

**Examples:**
```bash
# Get all categories sorted by name
GET /transaction-categories?sortBy=name&sortDirection=asc

# Search for categories containing "food"
GET /transaction-categories?search=food
```

#### Unified Transaction Categories Metadata Endpoint
**`GET /transaction-categories/metadata`**

Retrieve transaction categories metadata and analytics data.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `data` | String | Yes | Type of metadata to retrieve | `all`, `count`, `usage-stats` |

**Examples:**
```bash
# Get all active categories
GET /transaction-categories/metadata?data=all

# Get total count of categories
GET /transaction-categories/metadata?data=count

# Get usage statistics
GET /transaction-categories/metadata?data=usage-stats
```

### 6. Transaction Subcategories Module

#### Unified Transaction Subcategories Endpoint
**`GET /transaction-subcategories`**

Retrieve transaction subcategories with flexible filtering, sorting, and pagination options.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `categoryId` | Long | No | Category ID filter | `1` |
| `search` | String | No | Search term for name | `restaurant` |
| `sortBy` | String | No | Sort field | `name`, `createdAt` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |
| `sortByUsage` | Boolean | No | Sort by usage (true/false) | `true`, `false` |

**Examples:**
```bash
# Get all subcategories for category 1
GET /transaction-subcategories?categoryId=1

# Get subcategories sorted by usage
GET /transaction-subcategories?categoryId=1&sortByUsage=true

# Search for subcategories containing "restaurant"
GET /transaction-subcategories?search=restaurant
```

#### Unified Transaction Subcategories Metadata Endpoint
**`GET /transaction-subcategories/metadata`**

Retrieve transaction subcategories metadata and analytics data.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `data` | String | Yes | Type of metadata to retrieve | `all`, `by-category`, `by-category-usage`, `count`, `count-by-category` |
| `categoryId` | Long | No | Required for category-specific data | `1` |

**Examples:**
```bash
# Get all active subcategories
GET /transaction-subcategories/metadata?data=all

# Get subcategories for category 1
GET /transaction-subcategories/metadata?data=by-category&categoryId=1

# Get subcategories for category 1 ordered by usage
GET /transaction-subcategories/metadata?data=by-category-usage&categoryId=1
```

## Response Formats

### Paginated Response
```json
{
  "content": [...],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 20,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false
  }
}
```

### Metadata Response Examples

#### Categories List
```json
[
  {
    "id": 1,
    "name": "Food & Dining",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
]
```

#### Usage Statistics
```json
{
  "totalCategories": 10,
  "usedCategories": 8
}
```

#### Status Summary
```json
{
  "activeCount": 5,
  "completedCount": 3,
  "totalCount": 8
}
```

## Migration Guide

### Old Approach
```bash
# Multiple specific endpoints
GET /transactions/performance/top?userId=1
GET /transactions/performance/worst?userId=1
GET /transactions/dividend-yield/top?userId=1
GET /transactions/sectors?userId=1
GET /financial-goals/active
GET /financial-goals/completed
GET /dashboard/summary
GET /dashboard/metrics?startDate=2024-01-01&endDate=2024-12-31
```

### New Approach
```bash
# Single unified endpoints
GET /transactions?userId=1&sort=performance&order=top
GET /transactions?userId=1&sort=performance&order=worst
GET /transactions?userId=1&sort=dividend-yield
GET /transactions/metadata?userId=1&data=sectors
GET /financial-goals?status=active
GET /financial-goals?status=completed
GET /dashboard?data=summary
GET /dashboard?data=metrics&startDate=2024-01-01&endDate=2024-12-31
```

## Error Handling

The unified endpoints provide clear error messages for invalid parameters:

```json
{
  "error": "Invalid data type: invalid-type"
}
```

```json
{
  "error": "Category ID is required for subcategories data"
}
```

```json
{
  "error": "Start date and end date are required for metrics data"
}
```

## Backward Compatibility

The individual endpoints are still available for backward compatibility:

- `/transactions/performance/top`
- `/transactions/performance/worst`
- `/transactions/dividend-yield/top`
- `/financial-goals/active`
- `/financial-goals/completed`
- `/dashboard/summary`
- `/dashboard/metrics`
- `/transaction-categories`
- `/transaction-subcategories/category/{categoryId}`

## Best Practices

1. **Use query parameters** instead of path parameters for filtering
2. **Provide default values** for optional parameters
3. **Use consistent naming** across all modules
4. **Document all parameters** with clear descriptions
5. **Handle validation errors** gracefully
6. **Support pagination** for list endpoints
7. **Use appropriate HTTP status codes**
8. **Provide meaningful error messages**

## Testing

All unified endpoints should be tested with:

1. **Valid parameters** - Ensure correct responses
2. **Invalid parameters** - Ensure proper error handling
3. **Edge cases** - Empty results, large datasets
4. **Pagination** - Different page sizes and numbers
5. **Sorting** - All sort fields and directions
6. **Filtering** - All filter combinations
7. **Performance** - Response times with large datasets

## Future Enhancements

1. **Caching** - Add response caching for frequently accessed data
2. **Rate limiting** - Implement rate limiting for API endpoints
3. **Bulk operations** - Support bulk create/update/delete operations
4. **Export functionality** - Add data export capabilities
5. **Real-time updates** - WebSocket support for real-time data
6. **Advanced filtering** - Support for complex filter expressions
7. **GraphQL support** - Alternative query language for complex queries
