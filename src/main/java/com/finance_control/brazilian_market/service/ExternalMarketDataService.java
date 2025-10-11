package com.finance_control.brazilian_market.service;

import com.finance_control.brazilian_market.model.Investment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for fetching market data from external APIs.
 * Currently supports Alpha Vantage API for Brazilian market data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalMarketDataService {

    private final RestTemplate restTemplate;

    @Value("${app.market-data.alpha-vantage.api-key:demo}")
    private String alphaVantageApiKey;

    @Value("${app.market-data.alpha-vantage.base-url:https://www.alphavantage.co/query}")
    private String alphaVantageBaseUrl;

    @Value("${app.market-data.update-interval-minutes:15}")
    private int updateIntervalMinutes;

    /**
     * Fetch current market data for an investment from Alpha Vantage API
     */
    public Optional<MarketData> fetchMarketData(String ticker, Investment.InvestmentType investmentType) {
        try {
            log.debug("Fetching market data for ticker: {} of type: {}", ticker, investmentType);
            
            // Format ticker for Alpha Vantage (Brazilian stocks need .SAO suffix)
            String formattedTicker = formatTickerForAlphaVantage(ticker, investmentType);
            
            // Build API URL
            String url = buildAlphaVantageUrl(formattedTicker);
            
            // Make API call
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null || response.containsKey("Error Message")) {
                log.warn("Alpha Vantage API returned error for ticker: {}", ticker);
                return Optional.empty();
            }
            
            // Parse response based on investment type
            return parseAlphaVantageResponse(response, ticker, investmentType);
            
        } catch (RestClientException e) {
            log.error("Error fetching market data for ticker: {}", ticker, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error fetching market data for ticker: {}", ticker, e);
            return Optional.empty();
        }
    }

    /**
     * Format ticker for Alpha Vantage API
     * Brazilian stocks need .SAO suffix, others might need different suffixes
     */
    private String formatTickerForAlphaVantage(String ticker, Investment.InvestmentType investmentType) {
        // Remove any existing suffix
        String baseTicker = ticker.split("\\.")[0];
        
        switch (investmentType) {
            case STOCK:
            case FII:
                // Brazilian stocks and FIIs use .SAO suffix
                return baseTicker + ".SAO";
            case BOND:
                // Bonds might need different handling
                return baseTicker;
            default:
                return baseTicker;
        }
    }

    /**
     * Build Alpha Vantage API URL
     */
    private String buildAlphaVantageUrl(String formattedTicker) {
        return String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s", 
                           alphaVantageBaseUrl, formattedTicker, alphaVantageApiKey);
    }

    /**
     * Parse Alpha Vantage API response
     */
    private Optional<MarketData> parseAlphaVantageResponse(Map<String, Object> response, String originalTicker, Investment.InvestmentType investmentType) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> quote = (Map<String, String>) response.get("Global Quote");
            
            if (quote == null || quote.isEmpty()) {
                log.warn("No quote data found in Alpha Vantage response for ticker: {}", originalTicker);
                return Optional.empty();
            }

            MarketData marketData = MarketData.builder()
                    .ticker(originalTicker)
                    .currentPrice(parseDecimal(quote.get("05. price")))
                    .previousClose(parseDecimal(quote.get("08. previous close")))
                    .dayChange(parseDecimal(quote.get("09. change")))
                    .dayChangePercent(parseDecimal(quote.get("10. change percent").replace("%", "")))
                    .volume(parseLong(quote.get("06. volume")))
                    .lastUpdated(LocalDateTime.now())
                    .build();

            log.debug("Successfully parsed market data for ticker: {}", originalTicker);
            return Optional.of(marketData);
            
        } catch (Exception e) {
            log.error("Error parsing Alpha Vantage response for ticker: {}", originalTicker, e);
            return Optional.empty();
        }
    }

    /**
     * Parse decimal value from string
     */
    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty() || "N/A".equals(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Could not parse decimal value: {}", value);
            return null;
        }
    }

    /**
     * Parse long value from string
     */
    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty() || "N/A".equals(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Could not parse long value: {}", value);
            return null;
        }
    }

    /**
     * Check if market data needs to be updated
     */
    public boolean needsUpdate(LocalDateTime lastUpdated) {
        if (lastUpdated == null) {
            return true;
        }
        return lastUpdated.isBefore(LocalDateTime.now().minusMinutes(updateIntervalMinutes));
    }

    /**
     * Market data DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class MarketData {
        private String ticker;
        private BigDecimal currentPrice;
        private BigDecimal previousClose;
        private BigDecimal dayChange;
        private BigDecimal dayChangePercent;
        private Long volume;
        private LocalDateTime lastUpdated;
    }

    /**
     * Get supported exchanges
     */
    public Map<String, String> getSupportedExchanges() {
        Map<String, String> exchanges = new HashMap<>();
        exchanges.put("B3", "Brasil Bolsa Balcão (Brazil)");
        exchanges.put("NYSE", "New York Stock Exchange");
        exchanges.put("NASDAQ", "NASDAQ");
        exchanges.put("LSE", "London Stock Exchange");
        return exchanges;
    }

    /**
     * Get supported currencies
     */
    public Map<String, String> getSupportedCurrencies() {
        Map<String, String> currencies = new HashMap<>();
        currencies.put("BRL", "Brazilian Real");
        currencies.put("USD", "US Dollar");
        currencies.put("EUR", "Euro");
        currencies.put("GBP", "British Pound");
        return currencies;
    }
}
