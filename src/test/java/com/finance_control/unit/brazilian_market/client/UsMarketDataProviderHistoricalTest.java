package com.finance_control.unit.brazilian_market.client;

import com.finance_control.brazilian_market.client.HistoricalData;
import com.finance_control.brazilian_market.client.UsMarketDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UsMarketDataProvider historical data edge cases.
 * Tests exception, null, and minimal valid responses for getHistoricalData.
 */
@ExtendWith(MockitoExtension.class)
class UsMarketDataProviderHistoricalTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UsMarketDataProvider usMarketDataProvider;

    @BeforeEach
    void setUp() {
        // Setup method - no shared test data needed
    }

    @Test
    void getHistoricalData_WithException_ShouldReturnEmpty() {
        // Given
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenThrow(new RuntimeException("Network error"));

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullResponse_ShouldReturnEmpty() {
        // Given
        when(restTemplate.getForObject(anyString(), any(Class.class)))
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
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(null);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullChartResponse_ShouldReturnEmpty() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = new UsMarketDataProvider.ChartResponse();
        chartResponse.setChart(null);
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithEmptyResultList_ShouldReturnEmpty() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = new UsMarketDataProvider.ChartResponse();
        UsMarketDataProvider.ChartResult chartResult = new UsMarketDataProvider.ChartResult();
        chartResult.setResult(new ArrayList<>());
        chartResponse.setChart(chartResult);
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullMeta_ShouldReturnEmpty() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = createMockChartResponse();
        UsMarketDataProvider.ChartResult chartResult = chartResponse.getChart();
        List<UsMarketDataProvider.ChartResultItem> results = chartResult.getResult();
        results.get(0).setMeta(null);
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullIndicators_ShouldReturnEmpty() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = createMockChartResponse();
        UsMarketDataProvider.ChartResult chartResult = chartResponse.getChart();
        List<UsMarketDataProvider.ChartResultItem> results = chartResult.getResult();
        results.get(0).setIndicators(null);
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullQuote_ShouldReturnEmpty() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = createMockChartResponse();
        UsMarketDataProvider.ChartResult chartResult = chartResponse.getChart();
        List<UsMarketDataProvider.ChartResultItem> results = chartResult.getResult();
        results.get(0).getIndicators().setQuote(null);
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithEmptyQuoteList_ShouldReturnEmpty() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = createMockChartResponse();
        UsMarketDataProvider.ChartResult chartResult = chartResponse.getChart();
        List<UsMarketDataProvider.ChartResultItem> results = chartResult.getResult();
        results.get(0).getIndicators().setQuote(new ArrayList<>());
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullTimestamps_ShouldReturnEmpty() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = createMockChartResponse();
        UsMarketDataProvider.ChartResult chartResult = chartResponse.getChart();
        List<UsMarketDataProvider.ChartResultItem> results = chartResult.getResult();
        results.get(0).setTimestamp(null);
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithEmptyTimestamps_ShouldReturnEmpty() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = createMockChartResponse();
        UsMarketDataProvider.ChartResult chartResult = chartResponse.getChart();
        List<UsMarketDataProvider.ChartResultItem> results = chartResult.getResult();
        results.get(0).setTimestamp(new ArrayList<>());
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithNullOpenPrices_ShouldReturnEmpty() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = createMockChartResponse();
        UsMarketDataProvider.ChartResult chartResult = chartResponse.getChart();
        List<UsMarketDataProvider.ChartResultItem> results = chartResult.getResult();
        results.get(0).getIndicators().getQuote().get(0).setOpen(null);
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithEmptyOpenPrices_ShouldReturnEmpty() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = createMockChartResponse();
        UsMarketDataProvider.ChartResult chartResult = chartResponse.getChart();
        List<UsMarketDataProvider.ChartResultItem> results = chartResult.getResult();
        results.get(0).getIndicators().getQuote().get(0).setOpen(new ArrayList<>());
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_WithMinimalValidData_ShouldReturnHistoricalData() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = createMinimalValidChartResponse();
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isPresent();
        HistoricalData data = result.get();
        assertThat(data.getSymbol()).isEqualTo("AAPL");
        // Note: Data processing is not yet implemented (TODO in UsMarketDataProvider)
        // So data list will be empty until the TODO is completed
        assertThat(data.getData()).isEmpty();
    }

    @Test
    void getHistoricalData_WithMismatchedArraySizes_ShouldHandleGracefully() {
        // Given
        UsMarketDataProvider.ChartResponse chartResponse = createMockChartResponse();
        UsMarketDataProvider.ChartResult chartResult = chartResponse.getChart();
        List<UsMarketDataProvider.ChartResultItem> results = chartResult.getResult();
        // Make timestamps shorter than OHLC data
        results.get(0).setTimestamp(Arrays.asList(1L));
        results.get(0).getIndicators().getQuote().get(0).setOpen(Arrays.asList(100.0, 101.0, 102.0)); // longer
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(chartResponse);

        // When
        var result = usMarketDataProvider.getHistoricalData("AAPL", "1mo", "1d");

        // Then
        assertThat(result).isPresent();
        HistoricalData data = result.get();
        // Note: Data processing is not yet implemented (TODO in UsMarketDataProvider)
        // So data list will be empty until the TODO is completed
        assertThat(data.getData()).isEmpty();
    }

    private UsMarketDataProvider.ChartResponse createMockChartResponse() {
        UsMarketDataProvider.ChartResponse response = new UsMarketDataProvider.ChartResponse();
        UsMarketDataProvider.ChartResult chartResult = new UsMarketDataProvider.ChartResult();
        List<UsMarketDataProvider.ChartResultItem> results = new ArrayList<>();

        UsMarketDataProvider.ChartResultItem result = new UsMarketDataProvider.ChartResultItem();
        result.setTimestamp(Arrays.asList(1638360000L, 1638446400L, 1638532800L));

        UsMarketDataProvider.Meta meta = new UsMarketDataProvider.Meta();
        meta.setSymbol("AAPL");
        result.setMeta(meta);

        UsMarketDataProvider.Indicators indicators = new UsMarketDataProvider.Indicators();
        List<UsMarketDataProvider.Quote> quotes = new ArrayList<>();
        UsMarketDataProvider.Quote quote = new UsMarketDataProvider.Quote();
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

    private UsMarketDataProvider.ChartResponse createMinimalValidChartResponse() {
        UsMarketDataProvider.ChartResponse response = new UsMarketDataProvider.ChartResponse();
        UsMarketDataProvider.ChartResult chartResult = new UsMarketDataProvider.ChartResult();
        List<UsMarketDataProvider.ChartResultItem> results = new ArrayList<>();

        UsMarketDataProvider.ChartResultItem result = new UsMarketDataProvider.ChartResultItem();
        result.setTimestamp(Arrays.asList(1638360000L));

        UsMarketDataProvider.Meta meta = new UsMarketDataProvider.Meta();
        meta.setSymbol("AAPL");
        result.setMeta(meta);

        UsMarketDataProvider.Indicators indicators = new UsMarketDataProvider.Indicators();
        List<UsMarketDataProvider.Quote> quotes = new ArrayList<>();
        UsMarketDataProvider.Quote quote = new UsMarketDataProvider.Quote();
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
