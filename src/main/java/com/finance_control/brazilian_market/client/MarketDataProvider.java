package com.finance_control.brazilian_market.client;

import com.finance_control.brazilian_market.model.Investment;

import java.util.List;
import java.util.Optional;

/**
 * Generic interface for market data providers.
 * This allows easy swapping of different API implementations.
 */
public interface MarketDataProvider {

    /**
     * Get real-time quote for a single investment
     */
    Optional<MarketQuote> getQuote(String ticker);

    /**
     * Get real-time quotes for multiple investments
     */
    List<MarketQuote> getQuotes(List<String> tickers);

    /**
     * Get historical data for an investment
     */
    Optional<HistoricalData> getHistoricalData(String ticker, String period, String interval);

    /**
     * Check if this provider supports the given investment type
     */
    boolean supportsInvestmentType(Investment.InvestmentType investmentType);

    /**
     * Get the provider name for logging and identification
     */
    String getProviderName();
}

