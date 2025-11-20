package com.finance_control.unit.brazilian_market.client;

import com.finance_control.brazilian_market.client.HistoricalData;
import com.finance_control.brazilian_market.client.UsMarketDataProvider;
import com.finance_control.brazilian_market.client.model.ChartResponse;
import com.finance_control.brazilian_market.client.model.Meta;
import com.finance_control.brazilian_market.client.model.Indicators;
import com.finance_control.brazilian_market.client.model.Quote;
import com.finance_control.shared.monitoring.SentryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for UsMarketDataProvider historical data edge cases.
 * Tests exception, null, and minimal valid responses for getHistoricalData.
 */
@ExtendWith(MockitoExtension.class)
class UsMarketDataProviderHistoricalTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SentryService sentryService;

    private UsMarketDataProvider usMarketDataProvider;

    @BeforeEach
    void setUp() {
        // Setup method - no shared test data needed
        // UsMarketDataProvider requires SentryService in constructor, so we need to create it manually
        usMarketDataProvider = new UsMarketDataProvider(restTemplate, sentryService);
        // Mock SentryService to avoid NPE - use lenient since not all tests will trigger exceptions
        lenient().doNothing().when(sentryService).captureException(any(Exception.class), anyMap());
    }

    /**
     * Helper method to get the ChartResponse class type safely without unchecked conversion.
     */
    private Class<ChartResponse> getChartResponseClass() {
        return ChartResponse.class;
    }

    @Test
    void getHistoricalData_WithException_ShouldReturnEmpty() {
        // Given
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenThrow(new RuntimeException("Network error"));

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullResponse_ShouldReturnEmpty() {
        // Given
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(null);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNonOkStatus_ShouldReturnEmpty() {
        // Given - getForObject doesn't return status, so this test doesn't apply
        // Instead, we'll test with null response which simulates a failed request
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(null);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullChartResponse_ShouldReturnEmpty() {
        // Given
        ChartResponse chartResponse = new ChartResponse();
        chartResponse.setChart(null);
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithEmptyResultList_ShouldReturnEmpty() {
        // Given
        ChartResponse chartResponse = new ChartResponse();
        ChartResponse.ChartResult chartResult = new ChartResponse.ChartResult();
        chartResult.setResult(new ArrayList<>());
        chartResponse.setChart(chartResult);
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullMeta_ShouldReturnEmpty() {
        // Given
        ChartResponse chartResponse = createMockChartResponse();
        ChartResponse.ChartResult chartResult = chartResponse.getChart();
        List<ChartResponse.ChartResultItem> results = chartResult.getResult();
        results.get(0).setMeta(null);
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullIndicators_ShouldReturnEmpty() {
        // Given
        ChartResponse chartResponse = createMockChartResponse();
        ChartResponse.ChartResult chartResult = chartResponse.getChart();
        List<ChartResponse.ChartResultItem> results = chartResult.getResult();
        results.get(0).setIndicators(null);
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullQuote_ShouldReturnEmpty() {
        // Given
        ChartResponse chartResponse = createMockChartResponse();
        ChartResponse.ChartResult chartResult = chartResponse.getChart();
        List<ChartResponse.ChartResultItem> results = chartResult.getResult();
        results.get(0).getIndicators().setQuote(null);
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithEmptyQuoteList_ShouldReturnEmpty() {
        // Given
        ChartResponse chartResponse = createMockChartResponse();
        ChartResponse.ChartResult chartResult = chartResponse.getChart();
        List<ChartResponse.ChartResultItem> results = chartResult.getResult();
        results.get(0).getIndicators().setQuote(new ArrayList<>());
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullTimestamps_ShouldReturnEmpty() {
        // Given
        ChartResponse chartResponse = createMockChartResponse();
        ChartResponse.ChartResult chartResult = chartResponse.getChart();
        List<ChartResponse.ChartResultItem> results = chartResult.getResult();
        results.get(0).setTimestamp(null);
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithEmptyTimestamps_ShouldReturnEmpty() {
        // Given
        ChartResponse chartResponse = createMockChartResponse();
        ChartResponse.ChartResult chartResult = chartResponse.getChart();
        List<ChartResponse.ChartResultItem> results = chartResult.getResult();
        results.get(0).setTimestamp(new ArrayList<>());
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullOpenPrices_ShouldReturnEmpty() {
        // Given
        ChartResponse chartResponse = createMockChartResponse();
        ChartResponse.ChartResult chartResult = chartResponse.getChart();
        List<ChartResponse.ChartResultItem> results = chartResult.getResult();
        results.get(0).getIndicators().getQuote().get(0).setOpen(null);
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithEmptyOpenPrices_ShouldReturnEmpty() {
        // Given
        ChartResponse chartResponse = createMockChartResponse();
        ChartResponse.ChartResult chartResult = chartResponse.getChart();
        List<ChartResponse.ChartResultItem> results = chartResult.getResult();
        results.get(0).getIndicators().getQuote().get(0).setOpen(new ArrayList<>());
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithMinimalValidData_ShouldReturnHistoricalData() {
        // Given
        ChartResponse chartResponse = createMinimalValidChartResponse();
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isPresent();
        HistoricalData data = result.get();
        assertThat(data.getSymbol()).isEqualTo("AAPL");
        // Note: Data processing is now implemented via processHistoricalDataPoints()
        // Verify that data points are processed correctly
        assertThat(data.getData()).isNotEmpty();
    }

    @Test
    void getHistoricalData_WithMismatchedArraySizes_ShouldHandleGracefully() {
        // Given
        ChartResponse chartResponse = createMockChartResponse();
        ChartResponse.ChartResult chartResult = chartResponse.getChart();
        List<ChartResponse.ChartResultItem> results = chartResult.getResult();
        // Make timestamps shorter than OHLC data
        results.get(0).setTimestamp(Arrays.asList(1L));
        results.get(0).getIndicators().getQuote().get(0).setOpen(Arrays.asList(100.0, 101.0, 102.0)); // longer
        when(restTemplate.getForObject(anyString(), getChartResponseClass()))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isPresent();
        HistoricalData data = result.get();
        // Note: Data processing is now implemented via processHistoricalDataPoints()
        // With mismatched sizes, the method should handle gracefully (process up to minimum size)
        assertThat(data.getData()).isNotEmpty();
    }

    private ChartResponse createMockChartResponse() {
        ChartResponse response = new ChartResponse();
        ChartResponse.ChartResult chartResult = new ChartResponse.ChartResult();
        List<ChartResponse.ChartResultItem> results = new ArrayList<>();

        ChartResponse.ChartResultItem result = new ChartResponse.ChartResultItem();
        result.setTimestamp(Arrays.asList(1638360000L, 1638446400L, 1638532800L));

        Meta meta = new Meta();
        meta.setSymbol("AAPL");
        result.setMeta(meta);

        Indicators indicators = new Indicators();
        List<Quote> quotes = new ArrayList<>();
        Quote quote = new Quote();
        quote.setOpen(Arrays.asList(150.0, 152.0, 148.0));
        quote.setHigh(Arrays.asList(155.0, 157.0, 153.0));
        quote.setLow(Arrays.asList(149.0, 151.0, 147.0));
        quote.setClose(Arrays.asList(154.0, 156.0, 152.0));
        quote.setVolume(Arrays.asList(1000000L, 1200000L, 800000L));
        quotes.add(quote);
        indicators.setQuote(quotes);
        result.setIndicators(indicators);

        results.add(result);
        chartResult.setResult(results);
        response.setChart(chartResult);

        return response;
    }

    private ChartResponse createMinimalValidChartResponse() {
        ChartResponse response = new ChartResponse();
        ChartResponse.ChartResult chartResult = new ChartResponse.ChartResult();
        List<ChartResponse.ChartResultItem> results = new ArrayList<>();

        ChartResponse.ChartResultItem result = new ChartResponse.ChartResultItem();
        result.setTimestamp(Arrays.asList(1638360000L));

        Meta meta = new Meta();
        meta.setSymbol("AAPL");
        result.setMeta(meta);

        Indicators indicators = new Indicators();
        List<Quote> quotes = new ArrayList<>();
        Quote quote = new Quote();
        quote.setOpen(Arrays.asList(150.0));
        quote.setHigh(Arrays.asList(155.0));
        quote.setLow(Arrays.asList(149.0));
        quote.setClose(Arrays.asList(154.0));
        quote.setVolume(Arrays.asList(1000000L));
        quotes.add(quote);
        indicators.setQuote(quotes);
        result.setIndicators(indicators);

        results.add(result);
        chartResult.setResult(results);
        response.setChart(chartResult);

        return response;
    }
}
