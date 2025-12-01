# Finance Control - Application Features

This document provides a comprehensive overview of all features available in the Finance Control application, organized by functional area.

## Table of Contents

1. [Transaction Management](#1-transaction-management)
2. [Category Management](#2-category-management)
3. [Financial Goals](#3-financial-goals)
4. [Dashboard and Analytics](#4-dashboard-and-analytics)
5. [Feature Flags](#5-feature-flags)

---

## 1. Transaction Management

The Transaction Management system provides comprehensive tools for tracking and organizing financial transactions.

### Core Features

#### Transaction CRUD Operations
- **Create Transactions**: Record income and expenses with detailed information
- **Read Transactions**: View individual transactions or paginated lists
- **Update Transactions**: Modify existing transaction details
- **Delete Transactions**: Remove transactions from the system

#### Transaction Types
- **Income**: Money received
- **Expense**: Money spent
- **Fixed**: Recurring fixed-amount transactions
- **Variable**: Variable-amount transactions

#### Transaction Sources
- **Credit Card**: Transactions paid with credit cards
- **Debit Card**: Transactions paid with debit cards
- **Cash**: Cash transactions
- **Bank Transfer**: Direct bank transfers
- **PIX**: Brazilian instant payment system

#### Advanced Features
- **Shared Responsibilities**: Split transactions among multiple users with percentage distribution
- **Date Range Filtering**: Filter transactions by date range
- **Category Assignment**: Assign transactions to categories and subcategories
- **Search Functionality**: Search transactions by description or other criteria
- **Sorting Options**: Sort by date, amount, category, or type
- **Pagination**: Efficient handling of large transaction lists

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/transactions` | Get paginated list of transactions |
| GET | `/transactions/{id}` | Get transaction by ID |
| POST | `/transactions` | Create new transaction |
| PUT | `/transactions/{id}` | Update transaction |
| DELETE | `/transactions/{id}` | Delete transaction |
| GET | `/transactions/filtered` | Get filtered transactions with advanced criteria |

### Use Cases

1. **Personal Finance Tracking**: Track daily expenses and income
2. **Expense Splitting**: Share costs among family members or roommates
3. **Budget Monitoring**: Monitor spending by category
4. **Financial History**: Maintain complete transaction history
5. **Tax Preparation**: Organize transactions for tax reporting

---

## 2. Category Management

The Category Management system provides a hierarchical structure for organizing transactions.

### Transaction Categories

Transaction categories provide the top-level organization for transactions.

#### Features
- **Category CRUD**: Create, read, update, and delete categories
- **Unique Names**: Case-insensitive unique category names
- **Audit Tracking**: Automatic creation and update timestamps
- **Search and Filtering**: Search categories by name
- **Pagination Support**: Efficient handling of large category lists

#### Standard Categories
Common category examples include:
- Food & Dining
- Transportation
- Housing & Utilities
- Entertainment
- Healthcare
- Shopping
- Personal Care
- Education
- Travel

### Transaction Subcategories

Subcategories provide detailed classification within categories.

#### Features
- **Subcategory CRUD**: Create, read, update, and delete subcategories
- **Category Association**: Link subcategories to parent categories
- **Active Status**: Enable/disable subcategories
- **Usage Tracking**: Track which subcategories are most used
- **Descriptions**: Optional detailed descriptions

#### Examples by Category

**Food & Dining**:
- Restaurants
- Groceries
- Fast Food
- Coffee Shops

**Transportation**:
- Fuel
- Public Transit
- Ride Sharing
- Vehicle Maintenance

**Housing & Utilities**:
- Rent/Mortgage
- Electricity
- Water
- Internet

### API Endpoints

#### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/transaction-categories` | Get paginated list of categories |
| GET | `/transaction-categories/{id}` | Get category by ID |
| POST | `/transaction-categories` | Create new category |
| PUT | `/transaction-categories/{id}` | Update category |
| DELETE | `/transaction-categories/{id}` | Delete category |

#### Subcategories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/transaction-subcategories` | Get paginated list of subcategories |
| GET | `/transaction-subcategories/{id}` | Get subcategory by ID |
| POST | `/transaction-subcategories` | Create new subcategory |
| PUT | `/transaction-subcategories/{id}` | Update subcategory |
| DELETE | `/transaction-subcategories/{id}` | Delete subcategory |
| GET | `/transaction-subcategories/category/{categoryId}` | Get subcategories by category |
| GET | `/transaction-subcategories/category/{categoryId}/usage` | Get subcategories ordered by usage |
| GET | `/transaction-subcategories/category/{categoryId}/count` | Get subcategory count |

### Validation Rules

#### Category Validation
- Name is required and must be unique (case-insensitive)
- Name cannot be empty or contain only whitespace
- Maximum name length: 255 characters

#### Subcategory Validation
- Name is required and must be unique (case-insensitive)
- Description is optional (max 500 characters)
- Category relationship is required
- isActive defaults to true

### Database Schema

#### transaction_categories
```sql
CREATE TABLE transaction_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_transaction_category_name ON transaction_categories(name);
```

#### transaction_subcategories
```sql
CREATE TABLE transaction_subcategories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    category_id BIGINT REFERENCES transaction_categories(id)
);

CREATE INDEX idx_transaction_subcategory_category ON transaction_subcategories(category_id);
CREATE INDEX idx_transaction_subcategory_name ON transaction_subcategories(name);
```

### Use Cases

1. **Transaction Organization**: Systematically organize all transactions
2. **Spending Analysis**: Analyze spending patterns by category
3. **Budget Creation**: Create category-based budgets
4. **Reporting**: Generate category-based financial reports
5. **Custom Categories**: Create personalized category structures

---

## 3. Financial Goals

The Financial Goals system helps users set, track, and achieve their financial objectives.

### Core Features

#### Goal Types
- **Savings**: Build emergency funds, save for purchases
- **Debt Payoff**: Track debt reduction progress
- **Investment**: Track investment goals
- **Custom**: User-defined goal types

#### Goal Tracking
- **Target Amount**: Set the desired amount to achieve
- **Current Amount**: Track progress toward the goal
- **Deadline**: Set target completion dates
- **Priority Levels**: Prioritize goals
- **Status Tracking**: Active, completed, or paused goals

#### Progress Features
- **Automatic Calculation**: Percentage completion
- **Progress Updates**: Increment current amount
- **Goal Completion**: Mark goals as completed
- **Visual Progress**: Track progress over time

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/financial-goals` | Get paginated list of goals |
| GET | `/financial-goals/{id}` | Get goal by ID |
| POST | `/financial-goals` | Create new goal |
| PUT | `/financial-goals/{id}` | Update goal |
| DELETE | `/financial-goals/{id}` | Delete goal |
| GET | `/financial-goals/active` | Get active goals |
| POST | `/financial-goals/{id}/progress` | Update goal progress |
| POST | `/financial-goals/{id}/complete` | Mark goal as completed |

### Use Cases

1. **Emergency Fund**: Build a financial safety net
2. **Debt Elimination**: Track progress toward debt freedom
3. **Large Purchases**: Save for major expenses
4. **Retirement Planning**: Track retirement savings
5. **Education Savings**: Save for educational expenses

---

## 4. Dashboard and Analytics

The Dashboard provides comprehensive financial insights and analytics.

### Dashboard Features

#### Financial Summary
- **Total Income**: Sum of all income transactions
- **Total Expenses**: Sum of all expense transactions
- **Net Balance**: Income minus expenses
- **Transaction Count**: Total number of transactions

#### Metrics and Analytics
- **Spending by Category**: Breakdown of expenses by category
- **Income vs Expenses**: Comparison over time
- **Goal Progress**: Overview of all financial goals
- **Trend Analysis**: Spending and income trends

#### Time-Based Views
- **Daily**: Daily transaction summaries
- **Weekly**: Weekly financial overview
- **Monthly**: Monthly budget analysis
- **Yearly**: Annual financial review

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dashboard/summary` | Get comprehensive dashboard summary |
| GET | `/dashboard/metrics` | Get detailed financial metrics |
| GET | `/dashboard/spending-categories` | Get top spending categories |
| GET | `/dashboard/transactions/summary` | Get transaction summary by period |

### Use Cases

1. **Financial Overview**: Quick snapshot of financial health
2. **Budget Monitoring**: Track spending against budgets
3. **Trend Analysis**: Identify spending patterns
4. **Goal Tracking**: Monitor progress toward goals
5. **Financial Planning**: Make informed financial decisions

---

## 5. Feature Flags

The Feature Flag system provides flexible control over application features.

### Configuration

Feature flags are configured in `application.yml` and can be controlled via environment variables.

### Available Features

#### Financial Predictions (`FINANCIAL_PREDICTIONS`)
- **Status**: Enabled by default
- **Environment Variable**: `FEATURE_FINANCIAL_PREDICTIONS_ENABLED`
- **Description**: AI-powered financial predictions
- **Endpoints**: `/dashboard/predictions`

#### Brazilian Market (`BRAZILIAN_MARKET`)
- **Status**: Enabled by default
- **Environment Variable**: `FEATURE_BRAZILIAN_MARKET_ENABLED`
- **Description**: Brazilian market data integration
- **Endpoints**: `/brazilian-market/indicators/*`

#### Open Finance (`OPEN_FINANCE`)
- **Status**: Disabled by default (not yet implemented)
- **Environment Variable**: `FEATURE_OPEN_FINANCE_ENABLED`
- **Description**: Open Finance/Open Banking integration
- **Note**: Enable when ready for implementation

#### Reports (`REPORTS`)
- **Status**: Enabled by default
- **Environment Variable**: `FEATURE_REPORTS_ENABLED`
- **Description**: Financial reports generation
- **Endpoints**: `/api/reports/*`

#### Data Export (`DATA_EXPORT`)
- **Status**: Enabled by default
- **Environment Variable**: `FEATURE_DATA_EXPORT_ENABLED`
- **Description**: Export data in CSV/JSON formats
- **Endpoints**: `/export/*`

#### Real-time Notifications (`REALTIME_NOTIFICATIONS`)
- **Status**: Enabled by default
- **Environment Variable**: `FEATURE_REALTIME_NOTIFICATIONS_ENABLED`
- **Description**: Real-time notification features

#### Monitoring (`MONITORING`)
- **Status**: Enabled by default
- **Environment Variable**: `FEATURE_MONITORING_ENABLED`
- **Description**: Monitoring and observability features
- **Endpoints**: `/monitoring/*`

#### Supabase Features
- **Auth**: Supabase authentication integration
- **Storage**: Supabase storage integration
- **Realtime**: Supabase realtime integration

### Usage

#### Enabling/Disabling Features

```bash
# Disable financial predictions
export FEATURE_FINANCIAL_PREDICTIONS_ENABLED=false

# Enable Brazilian market data
export FEATURE_BRAZILIAN_MARKET_ENABLED=true
```

#### In Code

```java
@Autowired
private FeatureFlagService featureFlagService;

// Check if a feature is enabled
if (featureFlagService.isEnabled(Feature.REPORTS)) {
    // Feature is enabled
}

// Require a feature to be enabled (throws exception if disabled)
featureFlagService.requireEnabled(Feature.DATA_EXPORT);
```

#### Error Handling

When a feature is disabled and a request is made to an endpoint that requires it:
- **HTTP Status**: `503 Service Unavailable`
- **Error Message**: `"Feature 'FEATURE_NAME' is currently disabled"`

### Best Practices

1. **Default to Enabled**: Most features should be enabled by default for backward compatibility
2. **Use Type-Safe Enums**: Always use the `Feature` enum instead of string literals
3. **Check Early**: Check feature flags at the beginning of controller methods
4. **Log Feature Checks**: Feature checks are logged at DEBUG level for troubleshooting
5. **Document Changes**: Update documentation when adding new feature flags

---

## Related Documentation

- [API Patterns](API_PATTERNS.md) - REST API patterns and conventions
- [Testing Strategy](TESTING.md) - Testing guidelines and best practices
- [Development Guide](DEVELOPMENT.md) - Development guidelines and coding standards
- [Architecture](ARCHITECTURE.md) - System architecture and design patterns
- [Deployment](DEPLOYMENT.md) - CI/CD and deployment documentation
