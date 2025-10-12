package com.finance_control.unit.brazilian_market.client;

import com.finance_control.brazilian_market.client.BrazilianMarketDataProvider;
import com.finance_control.brazilian_market.client.MarketQuote;
import com.finance_control.brazilian_market.client.UsMarketDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Market Data Providers.
 * Tests the market data fetching logic for both Brazilian and US markets.
 */
@ExtendWith(MockitoExtension.class)
class MarketDataProviderTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BrazilianMarketDataProvider brazilianMarketDataProvider;

    @InjectMocks
    private UsMarketDataProvider usMarketDataProvider;

    private MarketQuote testQuote;

    @BeforeEach
    void setUp() {
        testQuote = MarketQuote.builder()
                .symbol("PETR4")
                .currentPrice(BigDecimal.valueOf(26.00))
                .dayChange(BigDecimal.valueOf(0.50))
                .dayChangePercent(BigDecimal.valueOf(1.96))
                .volume(1000000L)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    void brazilianMarketDataProvider_ShouldReturnQuoteWhenApiCallSucceeds() {
        // Given
        when(restTemplate.getForObject(anyString(), any(Class.class)))
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
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void usMarketDataProvider_ShouldReturnQuoteWhenApiCallSucceeds() {
        // Given
        when(restTemplate.getForObject(anyString(), any(Class.class)))
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
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        Optional<MarketQuote> result = usMarketDataProvider.getQuote("AAPL");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_ShouldHandleNullResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(null);

        // When
        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void usMarketDataProvider_ShouldHandleNullResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(null);

        // When
        Optional<MarketQuote> result = usMarketDataProvider.getQuote("AAPL");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void brazilianMarketDataProvider_ShouldHandleEmptyResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(createEmptyBrapiResponse());

        // When
        Optional<MarketQuote> result = brazilianMarketDataProvider.getQuote("PETR4");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void usMarketDataProvider_ShouldHandleEmptyResponse() {
        // Given
        when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(createEmptyYahooResponse());

        // When
        Optional<MarketQuote> result = usMarketDataProvider.getQuote("AAPL");

        // Then
        assertThat(result).isEmpty();
    }

    private Object createMockBrapiResponse() {
        // Mock Brapi API response structure
        return new Object() {
            public Object[] results = new Object[]{
                new Object() {
                    public String symbol = "PETR4";
                    public double regularMarketPrice = 26.00;
                    public double regularMarketChange = 0.50;
                    public double regularMarketChangePercent = 1.96;
                    public long regularMarketVolume = 1000000L;
                }
            };
        };
    }

    private Object createEmptyBrapiResponse() {
        return new Object() {
            public Object[] results = new Object[0];
        };
    }

    private Object createMockYahooResponse() {
        // Mock Yahoo Finance API response structure
        return new Object() {
            public Object quoteResponse = new Object() {
                public Object[] result = new Object[]{
                    new Object() {
                        public String symbol = "AAPL";
                        public double regularMarketPrice = 150.00;
                        public double regularMarketChange = 2.50;
                        public double regularMarketChangePercent = 1.69;
                        public long regularMarketVolume = 50000000L;
                    }
                };
            };
        };
    }

    private Object createEmptyYahooResponse() {
        return new Object() {
            public Object quoteResponse = new Object() {
                public Object[] result = new Object[0];
            };
        };
    }
}
