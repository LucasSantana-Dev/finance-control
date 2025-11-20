package com.finance_control.unit.brazilian_market.client;

import com.finance_control.brazilian_market.client.BrazilianMarketDataProvider;
import com.finance_control.brazilian_market.client.MarketQuote;
import com.finance_control.brazilian_market.client.UsMarketDataProvider;
import com.finance_control.brazilian_market.client.model.ApiResponse;
import com.finance_control.brazilian_market.client.model.QuoteResponse;
import com.finance_control.brazilian_market.model.InvestmentType;
import com.finance_control.shared.monitoring.SentryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for Market Data Providers.
 * Tests the market data fetching logic for both Brazilian and US markets.
 */
@ExtendWith(MockitoExtension.class)
class MarketDataProviderTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SentryService sentryService;

    @InjectMocks
    private BrazilianMarketDataProvider brazilianMarketDataProvider;

    private UsMarketDataProvider usMarketDataProvider;

    @BeforeEach
    void setUp() {
        // Setup method - no shared test data needed
        // UsMarketDataProvider requires SentryService in constructor, so we need to create it manually
        usMarketDataProvider = new UsMarketDataProvider(restTemplate, sentryService);
        // Mock SentryService to avoid NPE - use lenient since not all tests will trigger exceptions
        lenient().doNothing().when(sentryService).captureException(any(Exception.class), anyMap());
    }

    @Test
    void brazilianMarketDataProvider_ShouldReturnQuoteWhenApiCallSucceeds() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(createMockBrapiResponse());

        // When
        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("PETR4");
        assertThat(result.get().getCurrentPrice()).isNotNull();
    }

    @Test
    void brazilianMarketDataProvider_ShouldReturnEmptyWhenApiCallFails() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void usMarketDataProvider_ShouldReturnQuoteWhenApiCallSucceeds() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(createMockYahooResponse());

        // When
        Optional<MarketQuote> result = usMarketDataProvider.getQuote("AAPL");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("AAPL");
        assertThat(result.get().getCurrentPrice()).isNotNull();
    }

    @Test
    void usMarketDataProvider_ShouldReturnEmptyWhenApiCallFails() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        Optional<MarketQuote> result = usMarketDataProvider.getQuote("AAPL");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_ShouldHandleNullResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(null);

        // When
        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void usMarketDataProvider_ShouldHandleNullResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(null);

        // When
        Optional<MarketQuote> result = usMarketDataProvider.getQuote("AAPL");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_ShouldHandleEmptyResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(createEmptyBrapiResponse());

        // When
        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void usMarketDataProvider_ShouldHandleEmptyResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(createEmptyYahooResponse());

        // When
        Optional<MarketQuote> result = usMarketDataProvider.getQuote("AAPL");

        // Then
        assertThat(result).isEmpty();
    }

    private BrazilianMarketDataProvider.ApiResponse createMockBrapiResponse() {
        // Mock Brazilian Market API response structure
        BrazilianMarketDataProvider.ApiResponse response = new BrazilianMarketDataProvider.ApiResponse();
        List<BrazilianMarketDataProvider.QuoteResponse> results = new ArrayList<>();

        BrazilianMarketDataProvider.QuoteResponse quote = new BrazilianMarketDataProvider.QuoteResponse();
        quote.setSymbol("PETR4");
        quote.setShortName("Petrobras PN");
        quote.setRegularMarketPrice(26.00);
        quote.setPreviousClose(25.50);
        quote.setRegularMarketVolume(1000000L);

        results.add(quote);
        response.setResults(results);

        return response;
    }

    private BrazilianMarketDataProvider.ApiResponse createEmptyBrapiResponse() {
        BrazilianMarketDataProvider.ApiResponse response = new BrazilianMarketDataProvider.ApiResponse();
        response.setResults(new ArrayList<>());
        return response;
    }

    private ApiResponse createMockYahooResponse() {
        // Mock US Market API response structure
        ApiResponse response = new ApiResponse();
        ApiResponse.QuoteResponseWrapper wrapper = new ApiResponse.QuoteResponseWrapper();
        List<QuoteResponse> resultList = new ArrayList<>();

        QuoteResponse quote = new QuoteResponse();
        quote.setSymbol("AAPL");
        quote.setShortName("Apple Inc.");
        quote.setRegularMarketPrice(150.00);
        quote.setPreviousClose(147.50);
        quote.setRegularMarketVolume(50000000L);

        resultList.add(quote);
        wrapper.setResult(resultList);
        response.setQuoteResponse(wrapper);

        return response;
    }

    private ApiResponse createEmptyYahooResponse() {
        ApiResponse response = new ApiResponse();
        ApiResponse.QuoteResponseWrapper wrapper = new ApiResponse.QuoteResponseWrapper();
        wrapper.setResult(new ArrayList<>());
        response.setQuoteResponse(wrapper);
        return response;
    }

    @Test
    void brazilianMarketDataProvider_GetQuotes_ShouldReturnMultipleQuotes() {
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(createMockBrapiMultiResponse());

        List<MarketQuote> result = brazilianMarketDataProvider.getQuotes(List.of("PETR4", "VALE3"));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSymbol()).isEqualTo("PETR4");
        assertThat(result.get(1).getSymbol()).isEqualTo("VALE3");
    }

    @Test
    void brazilianMarketDataProvider_GetQuotes_WithNullResponse_ShouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(null);

        List<MarketQuote> result = brazilianMarketDataProvider.getQuotes(List.of("PETR4"));

        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_GetQuotes_WithNullResults_ShouldReturnEmptyList() {
        BrazilianMarketDataProvider.ApiResponse response = new BrazilianMarketDataProvider.ApiResponse();
        response.setResults(null);
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(response);

        List<MarketQuote> result = brazilianMarketDataProvider.getQuotes(List.of("PETR4"));

        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_GetQuotes_WithEmptyResults_ShouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(createEmptyBrapiResponse());

        List<MarketQuote> result = brazilianMarketDataProvider.getQuotes(List.of("PETR4"));

        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_GetQuotes_WithException_ShouldReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new RuntimeException("API Error"));

        List<MarketQuote> result = brazilianMarketDataProvider.getQuotes(List.of("PETR4"));

        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_GetQuotes_WithEmptyTickerList_ShouldReturnEmptyList() {
        List<MarketQuote> result = brazilianMarketDataProvider.getQuotes(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_GetHistoricalData_ShouldReturnEmpty() {
        Optional<com.finance_control.brazilian_market.client.HistoricalData> result =
                brazilianMarketDataProvider.getHistoricalData("PETR4", "1mo", "1d");

        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_SupportsInvestmentType_Stock_ShouldReturnTrue() {
        boolean result = brazilianMarketDataProvider.supportsInvestmentType(InvestmentType.STOCK);

        assertThat(result).isTrue();
    }

    @Test
    void brazilianMarketDataProvider_SupportsInvestmentType_FII_ShouldReturnTrue() {
        boolean result = brazilianMarketDataProvider.supportsInvestmentType(InvestmentType.FII);

        assertThat(result).isTrue();
    }

    @Test
    void brazilianMarketDataProvider_SupportsInvestmentType_Other_ShouldReturnFalse() {
        boolean result = brazilianMarketDataProvider.supportsInvestmentType(InvestmentType.CRYPTO);

        assertThat(result).isFalse();
    }

    @Test
    void brazilianMarketDataProvider_GetProviderName_ShouldReturnCorrectName() {
        String result = brazilianMarketDataProvider.getProviderName();

        assertThat(result).isEqualTo("Brazilian Market API");
    }

    @Test
    void brazilianMarketDataProvider_GetQuote_WithNullPriceInResponse_ShouldReturnEmpty() {
        BrazilianMarketDataProvider.ApiResponse response = new BrazilianMarketDataProvider.ApiResponse();
        List<BrazilianMarketDataProvider.QuoteResponse> results = new ArrayList<>();
        BrazilianMarketDataProvider.QuoteResponse quote = new BrazilianMarketDataProvider.QuoteResponse();
        quote.setSymbol("PETR4");
        quote.setRegularMarketPrice(null);
        results.add(quote);
        response.setResults(results);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(response);

        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_GetQuote_WithNullPreviousClose_ShouldUseCurrentPrice() {
        BrazilianMarketDataProvider.ApiResponse response = new BrazilianMarketDataProvider.ApiResponse();
        List<BrazilianMarketDataProvider.QuoteResponse> results = new ArrayList<>();
        BrazilianMarketDataProvider.QuoteResponse quote = new BrazilianMarketDataProvider.QuoteResponse();
        quote.setSymbol("PETR4");
        quote.setRegularMarketPrice(26.00);
        quote.setPreviousClose(null);
        results.add(quote);
        response.setResults(results);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(response);

        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        assertThat(result).isPresent();
        assertThat(result.get().getPreviousClose()).isEqualByComparingTo(BigDecimal.valueOf(26.00));
    }

    @Test
    void brazilianMarketDataProvider_GetQuote_WithZeroPreviousClose_ShouldReturnZeroPercentChange() {
        BrazilianMarketDataProvider.ApiResponse response = new BrazilianMarketDataProvider.ApiResponse();
        List<BrazilianMarketDataProvider.QuoteResponse> results = new ArrayList<>();
        BrazilianMarketDataProvider.QuoteResponse quote = new BrazilianMarketDataProvider.QuoteResponse();
        quote.setSymbol("PETR4");
        quote.setRegularMarketPrice(26.00);
        quote.setPreviousClose(0.0);
        results.add(quote);
        response.setResults(results);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(response);

        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        assertThat(result).isPresent();
        assertThat(result.get().getDayChangePercent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void brazilianMarketDataProvider_ConvertToMarketData_WithNullQuote_ShouldReturnEmpty() {
        InvestmentType type = InvestmentType.STOCK;

        Optional<BrazilianMarketDataProvider.MarketData> result =
                brazilianMarketDataProvider.convertToMarketData(null, type);

        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_ConvertToMarketData_WithNullPrice_ShouldReturnEmpty() {
        BrazilianMarketDataProvider.QuoteResponse quote = new BrazilianMarketDataProvider.QuoteResponse();
        quote.setSymbol("PETR4");
        quote.setRegularMarketPrice(null);
        InvestmentType type = InvestmentType.STOCK;

        Optional<BrazilianMarketDataProvider.MarketData> result =
                brazilianMarketDataProvider.convertToMarketData(quote, type);

        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_GetQuote_WithNullResults_ShouldReturnEmpty() {
        BrazilianMarketDataProvider.ApiResponse response = new BrazilianMarketDataProvider.ApiResponse();
        response.setResults(null);
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(response);

        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        assertThat(result).isEmpty();
    }


    private BrazilianMarketDataProvider.ApiResponse createMockBrapiMultiResponse() {
        BrazilianMarketDataProvider.ApiResponse response = new BrazilianMarketDataProvider.ApiResponse();
        List<BrazilianMarketDataProvider.QuoteResponse> results = new ArrayList<>();

        BrazilianMarketDataProvider.QuoteResponse quote1 = new BrazilianMarketDataProvider.QuoteResponse();
        quote1.setSymbol("PETR4");
        quote1.setShortName("Petrobras PN");
        quote1.setRegularMarketPrice(26.00);
        quote1.setPreviousClose(25.50);

        BrazilianMarketDataProvider.QuoteResponse quote2 = new BrazilianMarketDataProvider.QuoteResponse();
        quote2.setSymbol("VALE3");
        quote2.setShortName("Vale ON");
        quote2.setRegularMarketPrice(68.50);
        quote2.setPreviousClose(68.00);

        results.add(quote1);
        results.add(quote2);
        response.setResults(results);

        return response;
    }
}
