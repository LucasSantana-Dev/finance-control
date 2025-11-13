package com.finance_control.unit.brazilian_market.service;

import com.finance_control.brazilian_market.client.MarketDataProvider;
import com.finance_control.brazilian_market.client.MarketQuote;
import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.service.ExternalMarketDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExternalMarketDataService.
 * Tests the market data fetching logic and provider selection.
 */
@ExtendWith(MockitoExtension.class)
class ExternalMarketDataServiceTest {

    @Mock
    private MarketDataProvider brazilianMarketProvider;

    @Mock
    private MarketDataProvider usMarketProvider;

    private ExternalMarketDataService externalMarketDataService;

    private Investment brazilianStock;
    private Investment usStock;
    private Investment fii;
    private Investment etf;
    private Investment usEtf;
    private MarketQuote marketQuote;

    @BeforeEach
    void setUp() {
        // Manually create the service instance with mocked providers
        externalMarketDataService = new ExternalMarketDataService(brazilianMarketProvider, usMarketProvider);

        brazilianStock = new Investment();
        brazilianStock.setTicker("PETR4");
        brazilianStock.setName("Petrobras");
        brazilianStock.setInvestmentType(Investment.InvestmentType.STOCK);
        brazilianStock.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        brazilianStock.setCurrentPrice(BigDecimal.valueOf(25.50));

        usStock = new Investment();
        usStock.setTicker("AAPL");
        usStock.setName("Apple Inc.");
        usStock.setInvestmentType(Investment.InvestmentType.STOCK);
        usStock.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        usStock.setCurrentPrice(BigDecimal.valueOf(150.00));

        fii = new Investment();
        fii.setTicker("HGLG11");
        fii.setName("CSHG Logística");
        fii.setInvestmentType(Investment.InvestmentType.FII);
        fii.setInvestmentSubtype(Investment.InvestmentSubtype.TIJOLO);
        fii.setCurrentPrice(BigDecimal.valueOf(100.00));

        etf = new Investment();
        etf.setTicker("BOVA11");
        etf.setName("iShares Ibovespa");
        etf.setInvestmentType(Investment.InvestmentType.ETF);
        etf.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        etf.setCurrentPrice(BigDecimal.valueOf(80.00));

        usEtf = new Investment();
        usEtf.setTicker("SPY");
        usEtf.setName("SPDR S&P 500 ETF");
        usEtf.setInvestmentType(Investment.InvestmentType.ETF);
        usEtf.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        usEtf.setCurrentPrice(BigDecimal.valueOf(400.00));

        marketQuote = MarketQuote.builder()
                .symbol("PETR4")
                .currentPrice(BigDecimal.valueOf(26.00))
                .previousClose(BigDecimal.valueOf(25.50))
                .dayChange(BigDecimal.valueOf(0.50))
                .dayChangePercent(BigDecimal.valueOf(1.96))
                .volume(1000000L)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    void fetchMarketData_ShouldUseBrazilianProviderForBrazilianStocks() {
        // Given
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.STOCK)).thenReturn(true);
        when(brazilianMarketProvider.getQuote("PETR4")).thenReturn(Optional.of(marketQuote));

        // When
        Optional<MarketQuote> result = externalMarketDataService.fetchMarketData("PETR4", Investment.InvestmentType.STOCK);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("PETR4");
        assertThat(result.get().getCurrentPrice()).isEqualTo(BigDecimal.valueOf(26.00));

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.STOCK);
        verify(brazilianMarketProvider).getQuote("PETR4");
        verify(usMarketProvider, never()).getQuote(anyString());
    }

    @Test
    void fetchMarketData_ShouldUseUSProviderForUSStocks() {
        // Given
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.STOCK)).thenReturn(false);
        when(usMarketProvider.supportsInvestmentType(Investment.InvestmentType.STOCK)).thenReturn(true);
        when(usMarketProvider.getQuote("AAPL")).thenReturn(Optional.of(marketQuote));

        // When
        Optional<MarketQuote> result = externalMarketDataService.fetchMarketData("AAPL", Investment.InvestmentType.STOCK);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("PETR4");

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.STOCK);
        verify(usMarketProvider).supportsInvestmentType(Investment.InvestmentType.STOCK);
        verify(usMarketProvider).getQuote("AAPL");
        verify(brazilianMarketProvider, never()).getQuote(anyString());
    }

    @Test
    void fetchMarketData_ShouldReturnEmptyWhenNoProviderSupportsInvestmentType() {
        // Given
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.CRYPTO)).thenReturn(false);
        when(usMarketProvider.supportsInvestmentType(Investment.InvestmentType.CRYPTO)).thenReturn(false);

        // When
        Optional<MarketQuote> result = externalMarketDataService.fetchMarketData("BTC", Investment.InvestmentType.CRYPTO);

        // Then
        assertThat(result).isEmpty();

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.CRYPTO);
        verify(usMarketProvider).supportsInvestmentType(Investment.InvestmentType.CRYPTO);
        verify(brazilianMarketProvider, never()).getQuote(anyString());
        verify(usMarketProvider, never()).getQuote(anyString());
    }

    @Test
    void fetchMarketData_ShouldReturnEmptyWhenProviderThrowsException() {
        // Given
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.STOCK)).thenReturn(true);
        when(brazilianMarketProvider.getQuote("INVALID")).thenThrow(new RuntimeException("API Error"));

        // When
        Optional<MarketQuote> result = externalMarketDataService.fetchMarketData("INVALID", Investment.InvestmentType.STOCK);

        // Then
        assertThat(result).isEmpty();

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.STOCK);
        verify(brazilianMarketProvider).getQuote("INVALID");
    }

    @Test
    void fetchMarketData_ShouldReturnEmptyWhenProviderReturnsEmpty() {
        // Given
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.STOCK)).thenReturn(true);
        when(brazilianMarketProvider.getQuote("UNKNOWN")).thenReturn(Optional.empty());

        // When
        Optional<MarketQuote> result = externalMarketDataService.fetchMarketData("UNKNOWN", Investment.InvestmentType.STOCK);

        // Then
        assertThat(result).isEmpty();

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.STOCK);
        verify(brazilianMarketProvider).getQuote("UNKNOWN");
    }

    @Test
    void fetchMarketData_WithMultipleTickers_ShouldUseCorrectProvider() {
        // Given
        List<String> tickers = List.of("PETR4", "VALE3");
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.STOCK)).thenReturn(true);
        when(brazilianMarketProvider.getQuotes(tickers)).thenReturn(List.of(marketQuote));

        // When
        List<MarketQuote> result = externalMarketDataService.fetchMarketData(tickers, Investment.InvestmentType.STOCK);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSymbol()).isEqualTo("PETR4");

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.STOCK);
        verify(brazilianMarketProvider).getQuotes(tickers);
        verify(usMarketProvider, never()).getQuotes(any());
    }

    @Test
    void fetchMarketData_WithMultipleTickers_ShouldReturnEmptyWhenNoProviderSupports() {
        // Given
        List<String> tickers = List.of("BTC", "ETH");
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.CRYPTO)).thenReturn(false);
        when(usMarketProvider.supportsInvestmentType(Investment.InvestmentType.CRYPTO)).thenReturn(false);

        // When
        List<MarketQuote> result = externalMarketDataService.fetchMarketData(tickers, Investment.InvestmentType.CRYPTO);

        // Then
        assertThat(result).isEmpty();

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.CRYPTO);
        verify(usMarketProvider).supportsInvestmentType(Investment.InvestmentType.CRYPTO);
        verify(brazilianMarketProvider, never()).getQuotes(any());
        verify(usMarketProvider, never()).getQuotes(any());
    }

    @Test
    void fetchHistoricalData_ShouldUseCorrectProvider() {
        // Given
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.STOCK)).thenReturn(true);
        when(brazilianMarketProvider.getHistoricalData("PETR4", "1d", "1h"))
                .thenReturn(Optional.empty());

        // When
        Optional<com.finance_control.brazilian_market.client.HistoricalData> result =
                externalMarketDataService.fetchHistoricalData("PETR4", Investment.InvestmentType.STOCK, "1d", "1h");

        // Then
        assertThat(result).isEmpty();

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.STOCK);
        verify(brazilianMarketProvider).getHistoricalData("PETR4", "1d", "1h");
        verify(usMarketProvider, never()).getHistoricalData(anyString(), anyString(), anyString());
    }

    @Test
    void needsUpdate_ShouldReturnTrueWhenLastUpdatedIsNull() {
        // When
        boolean result = externalMarketDataService.needsUpdate(null);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void needsUpdate_ShouldReturnTrueWhenLastUpdatedIsOlderThan15Minutes() {
        // Given
        LocalDateTime oldTime = LocalDateTime.now().minusMinutes(20);

        // When
        boolean result = externalMarketDataService.needsUpdate(oldTime);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void needsUpdate_ShouldReturnFalseWhenLastUpdatedIsRecent() {
        // Given
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(5);

        // When
        boolean result = externalMarketDataService.needsUpdate(recentTime);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getSupportedExchanges_ShouldReturnSupportedExchanges() {
        // When
        var exchanges = externalMarketDataService.getSupportedExchanges();

        // Then
        assertThat(exchanges).containsKey("B3");
        assertThat(exchanges).containsKey("NYSE");
        assertThat(exchanges).containsKey("NASDAQ");
        assertThat(exchanges).containsKey("LSE");
        assertThat(exchanges.get("B3")).isEqualTo("Brasil Bolsa Balcão (Brazil)");
    }

    @Test
    void getSupportedCurrencies_ShouldReturnSupportedCurrencies() {
        // When
        var currencies = externalMarketDataService.getSupportedCurrencies();

        // Then
        assertThat(currencies).containsKey("BRL");
        assertThat(currencies).containsKey("USD");
        assertThat(currencies).containsKey("EUR");
        assertThat(currencies).containsKey("GBP");
        assertThat(currencies.get("BRL")).isEqualTo("Brazilian Real");
    }

    @Test
    void getProviderInfo_ShouldReturnProviderInformation() {
        // Given
        when(brazilianMarketProvider.getProviderName()).thenReturn("Brazilian Market Provider");
        when(usMarketProvider.getProviderName()).thenReturn("US Market Provider");

        // When
        var providers = externalMarketDataService.getProviderInfo();

        // Then
        assertThat(providers).containsKey("Brazilian Market");
        assertThat(providers).containsKey("US Market");
        assertThat(providers.get("Brazilian Market")).isEqualTo("Brazilian Market Provider");
        assertThat(providers.get("US Market")).isEqualTo("US Market Provider");

        verify(brazilianMarketProvider).getProviderName();
        verify(usMarketProvider).getProviderName();
    }

    @Test
    void fetchHistoricalData_ShouldReturnEmptyWhenNoProviderSupports() {
        // Given
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.CRYPTO)).thenReturn(false);
        when(usMarketProvider.supportsInvestmentType(Investment.InvestmentType.CRYPTO)).thenReturn(false);

        // When
        Optional<com.finance_control.brazilian_market.client.HistoricalData> result =
                externalMarketDataService.fetchHistoricalData("BTC", Investment.InvestmentType.CRYPTO, "1d", "1h");

        // Then
        assertThat(result).isEmpty();

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.CRYPTO);
        verify(usMarketProvider).supportsInvestmentType(Investment.InvestmentType.CRYPTO);
        verify(brazilianMarketProvider, never()).getHistoricalData(anyString(), anyString(), anyString());
        verify(usMarketProvider, never()).getHistoricalData(anyString(), anyString(), anyString());
    }

    @Test
    void fetchHistoricalData_ShouldReturnEmptyWhenProviderThrowsException() {
        // Given
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.STOCK)).thenReturn(true);
        when(brazilianMarketProvider.getHistoricalData("INVALID", "1d", "1h"))
                .thenThrow(new RuntimeException("API Error"));

        // When
        Optional<com.finance_control.brazilian_market.client.HistoricalData> result =
                externalMarketDataService.fetchHistoricalData("INVALID", Investment.InvestmentType.STOCK, "1d", "1h");

        // Then
        assertThat(result).isEmpty();

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.STOCK);
        verify(brazilianMarketProvider).getHistoricalData("INVALID", "1d", "1h");
    }

    @Test
    void fetchMarketData_WithMultipleTickers_ShouldReturnEmptyWhenProviderThrowsException() {
        // Given
        List<String> tickers = List.of("PETR4", "VALE3");
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.STOCK)).thenReturn(true);
        when(brazilianMarketProvider.getQuotes(tickers)).thenThrow(new RuntimeException("API Error"));

        // When
        List<MarketQuote> result = externalMarketDataService.fetchMarketData(tickers, Investment.InvestmentType.STOCK);

        // Then
        assertThat(result).isEmpty();

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.STOCK);
        verify(brazilianMarketProvider).getQuotes(tickers);
    }

    @Test
    void fetchHistoricalData_ShouldUseUSProviderWhenUSMarketSupports() {
        // Given
        when(brazilianMarketProvider.supportsInvestmentType(Investment.InvestmentType.ETF)).thenReturn(false);
        when(usMarketProvider.supportsInvestmentType(Investment.InvestmentType.ETF)).thenReturn(true);
        com.finance_control.brazilian_market.client.HistoricalData historicalData =
                com.finance_control.brazilian_market.client.HistoricalData.builder().build();
        when(usMarketProvider.getHistoricalData("SPY", "1d", "1h"))
                .thenReturn(Optional.of(historicalData));

        // When
        Optional<com.finance_control.brazilian_market.client.HistoricalData> result =
                externalMarketDataService.fetchHistoricalData("SPY", Investment.InvestmentType.ETF, "1d", "1h");

        // Then
        assertThat(result).isPresent();

        verify(brazilianMarketProvider).supportsInvestmentType(Investment.InvestmentType.ETF);
        verify(usMarketProvider).supportsInvestmentType(Investment.InvestmentType.ETF);
        verify(usMarketProvider).getHistoricalData("SPY", "1d", "1h");
        verify(brazilianMarketProvider, never()).getHistoricalData(anyString(), anyString(), anyString());
    }

    @Test
    void needsUpdate_ShouldReturnTrueWhenLastUpdatedIsExactly15MinutesAgo() {
        // Given
        LocalDateTime exactly15MinutesAgo = LocalDateTime.now().minusMinutes(15);

        // When
        boolean result = externalMarketDataService.needsUpdate(exactly15MinutesAgo);

        // Then - Should return true because it's before (now - 15 minutes)
        assertThat(result).isTrue();
    }
}
