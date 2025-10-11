package com.finance_control.unit.brazilian_market.client;

import com.finance_control.brazilian_market.client.BrazilianStocksApiClient;
import com.finance_control.brazilian_market.model.BrazilianStock;
import com.finance_control.brazilian_market.model.FII;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BrazilianStocksApiClient.
 */
@ExtendWith(MockitoExtension.class)
class BrazilianStocksApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BrazilianStocksApiClient stocksApiClient;

    private List<Map<String, Object>> mockStocksData;
    private List<Map<String, Object>> mockFIIsData;
    private Map<String, Object> mockStockData;
    private Map<String, Object> mockFIIData;

    @BeforeEach
    void setUp() {
        stocksApiClient = new BrazilianStocksApiClient(restTemplate, "https://api.brazilianstocks.com", "");

        // Mock stock data
        mockStockData = Map.of(
            "ticker", "PETR4",
            "companyName", "Petrobras",
            "description", "Petróleo Brasileiro S.A.",
            "stockType", "ORDINARY",
            "segment", "NOVO_MERCADO",
            "currentPrice", "25.50",
            "previousClose", "24.50",
            "volume", "1000000",
            "marketCap", "1000000000.00"
        );

        // Mock FII data
        mockFIIData = new java.util.HashMap<>();
        mockFIIData.put("ticker", "HGLG11");
        mockFIIData.put("fundName", "CSHG Logística");
        mockFIIData.put("description", "Fundo de Investimento Imobiliário");
        mockFIIData.put("fiiType", "TIJOLO");
        mockFIIData.put("segment", "LOGISTICS");
        mockFIIData.put("currentPrice", "120.00");
        mockFIIData.put("previousClose", "119.00");
        mockFIIData.put("volume", "500000");
        mockFIIData.put("marketCap", "500000000.00");
        mockFIIData.put("dividendYield", "7.50");
        mockFIIData.put("lastDividend", "0.80");
        mockFIIData.put("netWorth", "115.00");

        mockStocksData = List.of(mockStockData);
        mockFIIsData = List.of(mockFIIData);
    }

    @Test
    void getAllStocks_WithValidResponse_ShouldReturnStocks() {
        // Given
        ResponseEntity<List> response = new ResponseEntity<>(mockStocksData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        List<BrazilianStock> result = stocksApiClient.getAllStocks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("PETR4");
        assertThat(result.get(0).getCompanyName()).isEqualTo("Petrobras");
        assertThat(result.get(0).getCurrentPrice()).isEqualTo(new BigDecimal("25.50"));
        verify(restTemplate).getForEntity(anyString(), eq(List.class));
    }

    @Test
    void getAllStocks_WithEmptyResponse_ShouldReturnEmptyList() {
        // Given
        ResponseEntity<List> response = new ResponseEntity<>(List.of(), HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        List<BrazilianStock> result = stocksApiClient.getAllStocks();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllStocks_WithException_ShouldReturnEmptyList() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(List.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        List<BrazilianStock> result = stocksApiClient.getAllStocks();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllFIIs_WithValidResponse_ShouldReturnFIIs() {
        // Given
        ResponseEntity<List> response = new ResponseEntity<>(mockFIIsData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        List<FII> result = stocksApiClient.getAllFIIs();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("HGLG11");
        assertThat(result.get(0).getFundName()).isEqualTo("CSHG Logística");
        assertThat(result.get(0).getCurrentPrice()).isEqualTo(new BigDecimal("120.00"));
        verify(restTemplate).getForEntity(anyString(), eq(List.class));
    }

    @Test
    void getAllFIIs_WithException_ShouldReturnEmptyList() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(List.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        List<FII> result = stocksApiClient.getAllFIIs();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getStockQuote_WithValidBrazilianApiResponse_ShouldReturnStock() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(mockStockData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(response);

        // When
        BrazilianStock result = stocksApiClient.getStockQuote("PETR4");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("PETR4");
        assertThat(result.getCompanyName()).isEqualTo("Petrobras");
        assertThat(result.getCurrentPrice()).isEqualTo(new BigDecimal("25.50"));
        verify(restTemplate).getForEntity(anyString(), eq(Map.class));
    }

    @Test
    void getStockQuote_WithBrazilianApiError_ShouldTryAlphaVantage() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Brazilian API Error"));

        // When
        BrazilianStock result = stocksApiClient.getStockQuote("PETR4");

        // Then
        assertThat(result).isNull(); // Alpha Vantage will also fail without API key
        verify(restTemplate, atLeastOnce()).getForEntity(anyString(), eq(Map.class));
    }

    @Test
    void getFIIQuote_WithValidResponse_ShouldReturnFII() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(mockFIIData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(response);

        // When
        FII result = stocksApiClient.getFIIQuote("HGLG11");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("HGLG11");
        assertThat(result.getFundName()).isEqualTo("CSHG Logística");
        assertThat(result.getCurrentPrice()).isEqualTo(new BigDecimal("120.00"));
        assertThat(result.getDividendYield()).isEqualTo(new BigDecimal("7.50"));
        verify(restTemplate).getForEntity(anyString(), eq(Map.class));
    }

    @Test
    void getFIIQuote_WithException_ShouldReturnNull() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        FII result = stocksApiClient.getFIIQuote("HGLG11");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void searchStocks_WithValidResponse_ShouldReturnMatchingStocks() {
        // Given
        ResponseEntity<List> response = new ResponseEntity<>(mockStocksData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        List<BrazilianStock> result = stocksApiClient.searchStocks("PETR");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("PETR4");
        verify(restTemplate).getForEntity(anyString(), eq(List.class));
    }

    @Test
    void searchStocks_WithException_ShouldReturnEmptyList() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(List.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        List<BrazilianStock> result = stocksApiClient.searchStocks("PETR");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getMarketSummary_WithValidResponse_ShouldReturnSummary() {
        // Given
        Map<String, Object> summaryData = Map.of(
            "totalStocks", 500,
            "totalFIIs", 200,
            "marketCap", "5000000000000.00"
        );
        ResponseEntity<Map> response = new ResponseEntity<>(summaryData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(response);

        // When
        Map<String, Object> result = stocksApiClient.getMarketSummary();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("totalStocks")).isEqualTo(500);
        assertThat(result.get("totalFIIs")).isEqualTo(200);
        verify(restTemplate).getForEntity(anyString(), eq(Map.class));
    }

    @Test
    void getMarketSummary_WithException_ShouldReturnEmptyMap() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        Map<String, Object> result = stocksApiClient.getMarketSummary();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getStockQuote_WithAlphaVantageResponse_ShouldReturnStock() {
        // Given
        Map<String, Object> alphaVantageResponse = Map.of(
            "Global Quote", Map.of(
                "05. price", "25.50",
                "08. previous close", "24.50",
                "06. volume", "1000000"
            )
        );

        // First call fails (Brazilian API), second call succeeds (Alpha Vantage)
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Brazilian API Error"))
                .thenReturn(new ResponseEntity<>(alphaVantageResponse, HttpStatus.OK));

        // When
        BrazilianStock result = stocksApiClient.getStockQuote("PETR4");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("PETR4");
        assertThat(result.getCurrentPrice()).isEqualTo(new BigDecimal("25.50"));
        assertThat(result.getPreviousClose()).isEqualTo(new BigDecimal("24.50"));
        assertThat(result.getVolume()).isEqualTo(1000000L);
    }

    @Test
    void getStockQuote_WithAlphaVantageError_ShouldReturnNull() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Brazilian API Error"))
                .thenThrow(new RuntimeException("Alpha Vantage API Error"));

        // When
        BrazilianStock result = stocksApiClient.getStockQuote("PETR4");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getStockQuote_WithAlphaVantageInvalidResponse_ShouldReturnNull() {
        // Given
        Map<String, Object> invalidResponse = Map.of("Invalid", "Data");

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Brazilian API Error"))
                .thenReturn(new ResponseEntity<>(invalidResponse, HttpStatus.OK));

        // When
        BrazilianStock result = stocksApiClient.getStockQuote("PETR4");

        // Then
        assertThat(result).isNull();
    }
}
