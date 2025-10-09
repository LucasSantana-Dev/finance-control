package com.finance_control.unit.brazilian_market.client;

import com.finance_control.brazilian_market.client.BCBApiClient;
import com.finance_control.brazilian_market.model.MarketIndicator;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BCBApiClient.
 */
@ExtendWith(MockitoExtension.class)
class BCBApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BCBApiClient bcbApiClient;

    private List<Map<String, Object>> mockResponseData;

    @BeforeEach
    void setUp() {
        bcbApiClient = new BCBApiClient(restTemplate, "https://api.bcb.gov.br/dados/serie/bcdata.sgs", "");
        
        // Mock response data
        mockResponseData = List.of(
            Map.of("data", "2024-01-15", "valor", "13.75")
        );
    }

    @Test
    void getCurrentSelicRate_WithValidResponse_ShouldReturnSelicRate() {
        // Given
        ResponseEntity<List> response = new ResponseEntity<>(mockResponseData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        BigDecimal result = bcbApiClient.getCurrentSelicRate();

        // Then
        assertThat(result).isEqualTo(new BigDecimal("13.75"));
        verify(restTemplate).getForEntity(anyString(), eq(List.class));
    }

    @Test
    void getCurrentSelicRate_WithEmptyResponse_ShouldReturnZero() {
        // Given
        ResponseEntity<List> response = new ResponseEntity<>(List.of(), HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        BigDecimal result = bcbApiClient.getCurrentSelicRate();

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getCurrentSelicRate_WithNullValue_ShouldReturnZero() {
        // Given
        List<Map<String, Object>> responseData = List.of(
            Map.of("data", "2024-01-15", "valor", null)
        );
        ResponseEntity<List> response = new ResponseEntity<>(responseData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        BigDecimal result = bcbApiClient.getCurrentSelicRate();

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getCurrentSelicRate_WithException_ShouldReturnZero() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(List.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        BigDecimal result = bcbApiClient.getCurrentSelicRate();

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getCurrentCDIRate_WithValidResponse_ShouldReturnCDIRate() {
        // Given
        ResponseEntity<List> response = new ResponseEntity<>(mockResponseData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        BigDecimal result = bcbApiClient.getCurrentCDIRate();

        // Then
        assertThat(result).isEqualTo(new BigDecimal("13.75"));
        verify(restTemplate).getForEntity(anyString(), eq(List.class));
    }

    @Test
    void getCurrentIPCA_WithValidResponse_ShouldReturnIPCA() {
        // Given
        ResponseEntity<List> response = new ResponseEntity<>(mockResponseData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        BigDecimal result = bcbApiClient.getCurrentIPCA();

        // Then
        assertThat(result).isEqualTo(new BigDecimal("13.75"));
        verify(restTemplate).getForEntity(anyString(), eq(List.class));
    }

    @Test
    void getCurrentExchangeRate_WithValidResponse_ShouldReturnExchangeRate() {
        // Given
        ResponseEntity<List> response = new ResponseEntity<>(mockResponseData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        BigDecimal result = bcbApiClient.getCurrentExchangeRate();

        // Then
        assertThat(result).isEqualTo(new BigDecimal("13.75"));
        verify(restTemplate).getForEntity(anyString(), eq(List.class));
    }

    @Test
    void getHistoricalData_WithValidResponse_ShouldReturnHistoricalData() {
        // Given
        List<Map<String, Object>> historicalData = List.of(
            Map.of("data", "2024-01-15", "valor", "13.75"),
            Map.of("data", "2024-01-14", "valor", "13.50"),
            Map.of("data", "2024-01-13", "valor", "13.25")
        );
        ResponseEntity<List> response = new ResponseEntity<>(historicalData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        List<Map<String, Object>> result = bcbApiClient.getHistoricalData("432", 3);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).get("valor")).isEqualTo("13.75");
        verify(restTemplate).getForEntity(anyString(), eq(List.class));
    }

    @Test
    void getDataInRange_WithValidResponse_ShouldReturnDataInRange() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<Map<String, Object>> rangeData = List.of(
            Map.of("data", "2024-01-15", "valor", "13.75"),
            Map.of("data", "2024-01-10", "valor", "13.50")
        );
        ResponseEntity<List> response = new ResponseEntity<>(rangeData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        List<Map<String, Object>> result = bcbApiClient.getDataInRange("432", startDate, endDate);

        // Then
        assertThat(result).hasSize(2);
        verify(restTemplate).getForEntity(anyString(), eq(List.class));
    }

    @Test
    void getDataInRange_WithException_ShouldReturnEmptyList() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        when(restTemplate.getForEntity(anyString(), eq(List.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        List<Map<String, Object>> result = bcbApiClient.getDataInRange("432", startDate, endDate);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void createMarketIndicator_ShouldCreateIndicatorWithCurrentValue() {
        // Given
        ResponseEntity<List> response = new ResponseEntity<>(mockResponseData, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(List.class))).thenReturn(response);

        // When
        MarketIndicator indicator = bcbApiClient.createMarketIndicator(
                "SELIC", 
                "Taxa Selic", 
                "Taxa básica de juros da economia brasileira",
                MarketIndicator.IndicatorType.INTEREST_RATE,
                MarketIndicator.Frequency.DAILY
        );

        // Then
        assertThat(indicator.getCode()).isEqualTo("SELIC");
        assertThat(indicator.getName()).isEqualTo("Taxa Selic");
        assertThat(indicator.getDescription()).isEqualTo("Taxa básica de juros da economia brasileira");
        assertThat(indicator.getIndicatorType()).isEqualTo(MarketIndicator.IndicatorType.INTEREST_RATE);
        assertThat(indicator.getFrequency()).isEqualTo(MarketIndicator.Frequency.DAILY);
        assertThat(indicator.getCurrentValue()).isEqualTo(new BigDecimal("13.75"));
        assertThat(indicator.getIsActive()).isTrue();
    }

    @Test
    void createMarketIndicator_WithApiError_ShouldCreateIndicatorWithoutValue() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(List.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        MarketIndicator indicator = bcbApiClient.createMarketIndicator(
                "SELIC", 
                "Taxa Selic", 
                "Taxa básica de juros da economia brasileira",
                MarketIndicator.IndicatorType.INTEREST_RATE,
                MarketIndicator.Frequency.DAILY
        );

        // Then
        assertThat(indicator.getCode()).isEqualTo("SELIC");
        assertThat(indicator.getName()).isEqualTo("Taxa Selic");
        assertThat(indicator.getCurrentValue()).isNull();
        assertThat(indicator.getIsActive()).isTrue();
    }

    @Test
    void getHistoricalData_WithException_ShouldReturnEmptyList() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(List.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        List<Map<String, Object>> result = bcbApiClient.getHistoricalData("432", 5);

        // Then
        assertThat(result).isEmpty();
    }
}
