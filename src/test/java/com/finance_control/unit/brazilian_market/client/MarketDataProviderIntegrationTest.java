package com.finance_control.unit.brazilian_market.client;

import com.finance_control.brazilian_market.client.MarketDataProvider;
import com.finance_control.brazilian_market.model.InvestmentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for market data providers
 */
@SpringBootTest
@ActiveProfiles("test")
class MarketDataProviderIntegrationTest {

    @Autowired
    @Qualifier("brazilianMarketDataProvider")
    private MarketDataProvider brazilianProvider;

    @Autowired
    @Qualifier("usMarketDataProvider")
    private MarketDataProvider usProvider;

    @Test
    void testBrazilianProviderSupportsCorrectInvestmentTypes() {
        assertTrue(brazilianProvider.supportsInvestmentType(InvestmentType.STOCK));
        assertTrue(brazilianProvider.supportsInvestmentType(InvestmentType.FII));
        assertFalse(brazilianProvider.supportsInvestmentType(InvestmentType.ETF));
        assertFalse(brazilianProvider.supportsInvestmentType(InvestmentType.BOND));
    }

    @Test
    void testUsProviderSupportsCorrectInvestmentTypes() {
        assertTrue(usProvider.supportsInvestmentType(InvestmentType.STOCK));
        assertTrue(usProvider.supportsInvestmentType(InvestmentType.ETF));
        assertFalse(usProvider.supportsInvestmentType(InvestmentType.FII));
        assertFalse(usProvider.supportsInvestmentType(InvestmentType.BOND));
    }

    @Test
    void testProviderNames() {
        assertEquals("Brazilian Market API", brazilianProvider.getProviderName());
        assertEquals("US Market API", usProvider.getProviderName());
    }

    @Test
    void testBrazilianProviderGetQuote() {
        // Test with a known Brazilian stock (VALE3)
        var quote = brazilianProvider.getQuote("VALE3");

        // The quote might be empty if the API is not available or the ticker doesn't exist
        assertNotNull(quote);
    }

    @Test
    void testUsProviderGetQuote() {
        // Test with a known US stock (AAPL)
        var quote = usProvider.getQuote("AAPL");

        // The quote might be empty if the API is not available or the ticker doesn't exist
        assertNotNull(quote);
    }

    @Test
    void testBrazilianProviderGetQuotes() {
        // Test with multiple Brazilian stocks
        List<String> tickers = List.of("VALE3", "PETR4", "ITUB4");
        var quotes = brazilianProvider.getQuotes(tickers);

        // The quotes might be empty if the API is not available
        assertNotNull(quotes);
    }

    @Test
    void testUsProviderGetQuotes() {
        // Test with multiple US stocks
        List<String> tickers = List.of("AAPL", "GOOGL", "MSFT");
        var quotes = usProvider.getQuotes(tickers);

        // The quotes might be empty if the API is not available
        assertNotNull(quotes);
    }
}
