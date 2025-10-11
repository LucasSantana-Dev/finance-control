package com.finance_control.brazilian_market.service;

import com.finance_control.brazilian_market.client.MarketDataProvider;
import com.finance_control.brazilian_market.client.MarketQuote;
import com.finance_control.brazilian_market.model.Investment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for fetching market data from external APIs.
 * Uses generic market data providers that can be easily swapped.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalMarketDataService {

    @Qualifier("brazilianMarketDataProvider")
    private final MarketDataProvider brazilianMarketProvider;

    @Qualifier("usMarketDataProvider")
    private final MarketDataProvider usMarketProvider;

    /**
     * Fetch current market data for an investment from appropriate provider
     */
    public Optional<MarketQuote> fetchMarketData(String ticker, Investment.InvestmentType investmentType) {
        try {
            log.debug("Fetching market data for ticker: {} of type: {}", ticker, investmentType);

            MarketDataProvider provider = selectProvider(investmentType);
            if (provider == null) {
                log.warn("No provider available for investment type: {}", investmentType);
                return Optional.empty();
            }

            log.debug("Using provider: {} for ticker: {}", provider.getProviderName(), ticker);
            return provider.getQuote(ticker);
        } catch (Exception e) {
            log.error("Error fetching market data for ticker: {}", ticker, e);
            return Optional.empty();
        }
    }

    /**
     * Fetch market data for multiple investments
     */
    public List<MarketQuote> fetchMarketData(List<String> tickers, Investment.InvestmentType investmentType) {
        try {
            log.debug("Fetching market data for {} tickers of type: {}", tickers.size(), investmentType);

            MarketDataProvider provider = selectProvider(investmentType);
            if (provider == null) {
                log.warn("No provider available for investment type: {}", investmentType);
                return List.of();
            }

            log.debug("Using provider: {} for {} tickers", provider.getProviderName(), tickers.size());
            return provider.getQuotes(tickers);
        } catch (Exception e) {
            log.error("Error fetching market data for tickers: {}", tickers, e);
            return List.of();
        }
    }

    /**
     * Fetch historical data for an investment
     */
    public Optional<com.finance_control.brazilian_market.client.HistoricalData> fetchHistoricalData(
            String ticker, Investment.InvestmentType investmentType, String period, String interval) {
        try {
            log.debug("Fetching historical data for ticker: {} of type: {}", ticker, investmentType);

            MarketDataProvider provider = selectProvider(investmentType);
            if (provider == null) {
                log.warn("No provider available for investment type: {}", investmentType);
                return Optional.empty();
            }

            log.debug("Using provider: {} for historical data of ticker: {}", provider.getProviderName(), ticker);
            return provider.getHistoricalData(ticker, period, interval);
        } catch (Exception e) {
            log.error("Error fetching historical data for ticker: {}", ticker, e);
            return Optional.empty();
        }
    }

    /**
     * Select the appropriate market data provider based on investment type
     */
    private MarketDataProvider selectProvider(Investment.InvestmentType investmentType) {
        if (brazilianMarketProvider.supportsInvestmentType(investmentType)) {
            return brazilianMarketProvider;
        } else if (usMarketProvider.supportsInvestmentType(investmentType)) {
            return usMarketProvider;
        }
        return null;
    }

    /**
     * Check if market data needs to be updated
     */
    public boolean needsUpdate(LocalDateTime lastUpdated) {
        if (lastUpdated == null) {
            return true;
        }
        // Update every 15 minutes by default
        return lastUpdated.isBefore(LocalDateTime.now().minusMinutes(15));
    }

    /**
     * Get supported exchanges
     */
    public java.util.Map<String, String> getSupportedExchanges() {
        java.util.Map<String, String> exchanges = new java.util.HashMap<>();
        exchanges.put("B3", "Brasil Bolsa Balcão (Brazil)");
        exchanges.put("NYSE", "New York Stock Exchange");
        exchanges.put("NASDAQ", "NASDAQ");
        exchanges.put("LSE", "London Stock Exchange");
        return exchanges;
    }

    /**
     * Get supported currencies
     */
    public java.util.Map<String, String> getSupportedCurrencies() {
        java.util.Map<String, String> currencies = new java.util.HashMap<>();
        currencies.put("BRL", "Brazilian Real");
        currencies.put("USD", "US Dollar");
        currencies.put("EUR", "Euro");
        currencies.put("GBP", "British Pound");
        return currencies;
    }

    /**
     * Get provider information
     */
    public java.util.Map<String, String> getProviderInfo() {
        java.util.Map<String, String> providers = new java.util.HashMap<>();
        providers.put("Brazilian Market", brazilianMarketProvider.getProviderName());
        providers.put("US Market", usMarketProvider.getProviderName());
        return providers;
    }
}
