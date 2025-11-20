# Feature Flags Documentation

## Overview

The Finance Control application uses a centralized feature flag system to control which features are enabled or disabled. This allows for flexible configuration and easy feature toggling without code changes.

## Configuration

Feature flags are configured in `application.yml` under the `app.feature-flags` section. Each feature can be enabled or disabled using environment variables.

### Configuration Structure

```yaml
app:
  feature-flags:
    financial-predictions:
      enabled: ${FEATURE_FINANCIAL_PREDICTIONS_ENABLED:true}
    brazilian-market:
      enabled: ${FEATURE_BRAZILIAN_MARKET_ENABLED:true}
    open-finance:
      enabled: ${FEATURE_OPEN_FINANCE_ENABLED:true}
    reports:
      enabled: ${FEATURE_REPORTS_ENABLED:true}
    data-export:
      enabled: ${FEATURE_DATA_EXPORT_ENABLED:true}
    realtime-notifications:
      enabled: ${FEATURE_REALTIME_NOTIFICATIONS_ENABLED:true}
    monitoring:
      enabled: ${FEATURE_MONITORING_ENABLED:true}
    supabase-features:
      auth: ${FEATURE_SUPABASE_AUTH_ENABLED:true}
      storage: ${FEATURE_SUPABASE_STORAGE_ENABLED:true}
      realtime: ${FEATURE_SUPABASE_REALTIME_ENABLED:true}
```

## Available Feature Flags

### Financial Predictions (`FINANCIAL_PREDICTIONS`)

Controls AI-powered financial predictions feature.

- **Default**: Enabled (`true`)
- **Environment Variable**: `FEATURE_FINANCIAL_PREDICTIONS_ENABLED`
- **Endpoints Affected**:
  - `POST /dashboard/predictions`

### Brazilian Market (`BRAZILIAN_MARKET`)

Controls Brazilian market data integration features.

- **Default**: Enabled (`true`)
- **Environment Variable**: `FEATURE_BRAZILIAN_MARKET_ENABLED`
- **Endpoints Affected**:
  - `GET /brazilian-market/indicators/*`
  - `GET /brazilian-market/investments/*`
  - `GET /brazilian-market/summary`
  - `POST /brazilian-market/indicators/*/update`

### Open Finance (`OPEN_FINANCE`)

Controls Open Finance/Open Banking features.

- **Default**: Disabled (`false`) - Not implemented at start
- **Environment Variable**: `FEATURE_OPEN_FINANCE_ENABLED`
- **Endpoints Affected**: All Open Finance endpoints
- **Note**: This feature is disabled by default as it's not implemented at the start. Enable via configuration when ready.

### Reports (`REPORTS`)

Controls financial reports generation features.

- **Default**: Enabled (`true`)
- **Environment Variable**: `FEATURE_REPORTS_ENABLED`
- **Endpoints Affected**:
  - `GET /api/reports/transactions`
  - `GET /api/reports/goals`
  - `GET /api/reports/summary`

### Data Export (`DATA_EXPORT`)

Controls data export functionality.

- **Default**: Enabled (`true`)
- **Environment Variable**: `FEATURE_DATA_EXPORT_ENABLED`
- **Endpoints Affected**:
  - `GET /export/all/csv`
  - `GET /export/all/json`
  - `GET /export/transactions/csv`
  - `GET /export/goals/csv`
  - `GET /api/data-export/transactions/csv`
  - `GET /api/data-export/goals/csv`

### Real-time Notifications (`REALTIME_NOTIFICATIONS`)

Controls real-time notification features.

- **Default**: Enabled (`true`)
- **Environment Variable**: `FEATURE_REALTIME_NOTIFICATIONS_ENABLED`
- **Endpoints Affected**: Real-time notification endpoints

### Monitoring (`MONITORING`)

Controls monitoring and observability features.

- **Default**: Enabled (`true`)
- **Environment Variable**: `FEATURE_MONITORING_ENABLED`
- **Endpoints Affected**:
  - `GET /monitoring/health`
  - `GET /monitoring/alerts`
  - `GET /monitoring/status`
  - `POST /monitoring/frontend-errors`
  - `GET /monitoring/metrics/summary`

### Supabase Features

Controls Supabase integration features.

#### Supabase Auth (`SUPABASE_AUTH`)

- **Default**: Enabled (`true`)
- **Environment Variable**: `FEATURE_SUPABASE_AUTH_ENABLED`
- **Endpoints Affected**: Supabase authentication endpoints

#### Supabase Storage (`SUPABASE_STORAGE`)

- **Default**: Enabled (`true`)
- **Environment Variable**: `FEATURE_SUPABASE_STORAGE_ENABLED`
- **Endpoints Affected**: Supabase storage endpoints

#### Supabase Realtime (`SUPABASE_REALTIME`)

- **Default**: Enabled (`true`)
- **Environment Variable**: `FEATURE_SUPABASE_REALTIME_ENABLED`
- **Endpoints Affected**: Supabase realtime endpoints

## Usage

### Enabling/Disabling Features

To enable or disable a feature, set the corresponding environment variable:

```bash
# Disable financial predictions
export FEATURE_FINANCIAL_PREDICTIONS_ENABLED=false

# Enable Brazilian market data
export FEATURE_BRAZILIAN_MARKET_ENABLED=true
```

### In Code

The `FeatureFlagService` provides methods to check feature flags:

```java
@Autowired
private FeatureFlagService featureFlagService;

// Check if a feature is enabled
if (featureFlagService.isEnabled(Feature.REPORTS)) {
    // Feature is enabled
}

// Require a feature to be enabled (throws exception if disabled)
featureFlagService.requireEnabled(Feature.DATA_EXPORT);

// Check by string name
boolean enabled = featureFlagService.isEnabled("REPORTS");
```

### Error Handling

When a feature is disabled and a request is made to an endpoint that requires it, the system returns:

- **HTTP Status**: `503 Service Unavailable`
- **Error Message**: `"Feature 'FEATURE_NAME' is currently disabled"`

## Best Practices

1. **Default to Enabled**: Most features should be enabled by default for backward compatibility
2. **Use Type-Safe Enums**: Always use the `Feature` enum instead of string literals
3. **Check Early**: Check feature flags at the beginning of controller methods
4. **Log Feature Checks**: Feature checks are logged at DEBUG level for troubleshooting
5. **Document Changes**: Update this documentation when adding new feature flags

## Adding New Feature Flags

To add a new feature flag:

1. Add the feature to the `Feature` enum in `Feature.java`
2. Add the feature properties to `FeatureFlagsProperties.java`
3. Update `application.yml` with the new feature flag configuration
4. Update `FeatureFlagService` to handle the new feature
5. Add feature flag checks to relevant controllers
6. Update this documentation

## Environment Variables Summary

| Feature | Environment Variable | Default |
|---------|---------------------|---------|
| Financial Predictions | `FEATURE_FINANCIAL_PREDICTIONS_ENABLED` | `true` |
| Brazilian Market | `FEATURE_BRAZILIAN_MARKET_ENABLED` | `true` |
| Open Finance | `FEATURE_OPEN_FINANCE_ENABLED` | `false` |
| Reports | `FEATURE_REPORTS_ENABLED` | `true` |
| Data Export | `FEATURE_DATA_EXPORT_ENABLED` | `true` |
| Real-time Notifications | `FEATURE_REALTIME_NOTIFICATIONS_ENABLED` | `true` |
| Monitoring | `FEATURE_MONITORING_ENABLED` | `true` |
| Supabase Auth | `FEATURE_SUPABASE_AUTH_ENABLED` | `true` |
| Supabase Storage | `FEATURE_SUPABASE_STORAGE_ENABLED` | `true` |
| Supabase Realtime | `FEATURE_SUPABASE_REALTIME_ENABLED` | `true` |
