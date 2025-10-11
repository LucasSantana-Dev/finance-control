# Market Data Providers Architecture

## Overview

The Finance Control application uses a flexible, provider-based architecture for fetching market data from external APIs. This design allows easy swapping of different data sources without changing the core application logic.

## Architecture

### Core Components

1. **`MarketDataProvider` Interface**: Defines the contract for all market data providers
2. **`MarketQuote`**: Generic data structure for market quotes
3. **`HistoricalData`**: Generic data structure for historical market data
4. **`ExternalMarketDataService`**: Service that orchestrates provider selection and data fetching

### Provider Implementations

#### Brazilian Market Data Provider
- **File**: `BrazilianMarketDataProvider.java`
- **API**: Public Brazilian market API
- **Supports**: Brazilian stocks and FIIs
- **Features**: Real-time quotes, fundamental data, no API key required

#### US Market Data Provider
- **File**: `UsMarketDataProvider.java`
- **API**: Public US market API
- **Supports**: US stocks and ETFs
- **Features**: Real-time quotes, historical data, no API key required

## Usage

### Automatic Provider Selection

The system automatically selects the appropriate provider based on investment type:

```java
// Brazilian stocks/FIIs → BrazilianMarketDataProvider
Optional<MarketQuote> quote = externalMarketDataService.fetchMarketData("VALE3", InvestmentType.STOCK);

// US stocks/ETFs → UsMarketDataProvider
Optional<MarketQuote> quote = externalMarketDataService.fetchMarketData("AAPL", InvestmentType.STOCK);
```

### Batch Operations

```java
// Fetch multiple quotes at once
List<MarketQuote> quotes = externalMarketDataService.fetchMarketData(
    List.of("VALE3", "PETR4", "ITUB4"),
    InvestmentType.STOCK
);
```

## Adding New Providers

To add a new market data provider:

1. **Create Provider Class**:
   ```java
   @Component("newMarketDataProvider")
   public class NewMarketDataProvider implements MarketDataProvider {
       // Implement interface methods
   }
   ```

2. **Update Service**:
   ```java
   @Qualifier("newMarketDataProvider")
   private final MarketDataProvider newProvider;
   ```

3. **Update Provider Selection Logic**:
   ```java
   private MarketDataProvider selectProvider(Investment.InvestmentType investmentType) {
       if (newProvider.supportsInvestmentType(investmentType)) {
           return newProvider;
       }
       // ... existing logic
   }
   ```

## Benefits

- **Decoupled**: Provider implementations are independent of each other
- **Swappable**: Easy to replace APIs without changing core logic
- **Extensible**: Simple to add new providers for different markets
- **Testable**: Each provider can be tested independently
- **Maintainable**: Clear separation of concerns

## API Information

### Brazilian Market API
- **Provider**: Public Brazilian market data service
- **Coverage**: Brazilian stocks, FIIs, indices
- **Rate Limits**: Free tier available
- **Features**: Real-time quotes, fundamental data

### US Market API
- **Provider**: Public US market data service
- **Coverage**: US stocks, ETFs, indices, crypto
- **Rate Limits**: No official limits (use responsibly)
- **Features**: Real-time quotes, historical data

## Future Enhancements

- Add caching layer for improved performance
- Implement rate limiting and retry logic
- Add more providers for other markets (Europe, Asia)
- Add real-time data streaming capabilities
- Implement data validation and quality checks
