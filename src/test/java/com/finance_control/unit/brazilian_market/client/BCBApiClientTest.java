package com.finance_control.unit.brazilian_market.client;

import com.finance_control.brazilian_market.client.BCBApiClient;
import com.finance_control.brazilian_market.model.MarketIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BCBApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BCBApiClient client;

    @BeforeEach
    void setUp() {
        client = new BCBApiClient(restTemplate, "https://api.bcb.gov.br/dados/serie/bcdata.sgs");
    }

    @Test
    void getCurrentSelicRate_ShouldReturnValue_WhenApiOk() {
        List<Map<String, Object>> body = new ArrayList<>();
        Map<String, Object> entry = new HashMap<>();
        entry.put("valor", "10.5");
        body.add(entry);

        when(restTemplate.getForEntity(anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        BigDecimal result = client.getCurrentSelicRate();
        assertThat(result).isEqualByComparingTo("10.5");
    }

    @Test
    void getCurrentSelicRate_ShouldReturnZero_WhenApiNotOk() {
        when(restTemplate.getForEntity(anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        BigDecimal result = client.getCurrentSelicRate();
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCurrentSelicRate_ShouldReturnZero_WhenException() {
        when(restTemplate.getForEntity(anyString(), any(Class.class)))
                .thenThrow(new RuntimeException("network error"));

        BigDecimal result = client.getCurrentSelicRate();
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getHistoricalData_ShouldReturnList_WhenApiOk() {
        List<Map<String, Object>> body = new ArrayList<>();
        body.add(Map.of("data", "01/01/2024", "valor", "10.1"));
        body.add(Map.of("data", "02/01/2024", "valor", "10.2"));

        when(restTemplate.getForEntity(anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        List<Map<String, Object>> result = client.getHistoricalData(BCBApiClient.SELIC_CODE, 2);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("valor").toString()).isEqualTo("10.1");
    }

    @Test
    void getHistoricalData_ShouldReturnEmpty_WhenApiNotOk() {
        when(restTemplate.getForEntity(anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

        List<Map<String, Object>> result = client.getHistoricalData(BCBApiClient.SELIC_CODE, 2);
        assertThat(result).isEmpty();
    }

    @Test
    void getHistoricalData_ShouldReturnEmpty_WhenException() {
        when(restTemplate.getForEntity(anyString(), any(Class.class)))
                .thenThrow(new RuntimeException("io"));

        List<Map<String, Object>> result = client.getHistoricalData(BCBApiClient.SELIC_CODE, 2);
        assertThat(result).isEmpty();
    }

    @Test
    void getDataInRange_ShouldReturnList_WhenApiOk() {
        List<Map<String, Object>> body = new ArrayList<>();
        body.add(Map.of("data", "01/01/2024", "valor", "4.95"));

        when(restTemplate.getForEntity(anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        List<Map<String, Object>> result = client.getDataInRange(
                BCBApiClient.CDI_CODE,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31)
        );
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("valor").toString()).isEqualTo("4.95");
    }

    @Test
    void getDataInRange_ShouldReturnEmpty_WhenApiNotOkOrNull() {
        when(restTemplate.getForEntity(anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        List<Map<String, Object>> result = client.getDataInRange(
                BCBApiClient.CDI_CODE,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31)
        );
        assertThat(result).isEmpty();
    }

    @Test
    void getDataInRange_ShouldReturnEmpty_WhenException() {
        when(restTemplate.getForEntity(anyString(), any(Class.class)))
                .thenThrow(new RuntimeException("timeout"));

        List<Map<String, Object>> result = client.getDataInRange(
                BCBApiClient.CDI_CODE,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31)
        );
        assertThat(result).isEmpty();
    }

    @Test
    void createMarketIndicator_ShouldPopulateCurrentValue_WhenApiOk() {
        // First call during createMarketIndicator -> getCurrentValueForCode(code)
        List<Map<String, Object>> body = new ArrayList<>();
        body.add(Map.of("valor", "3.14"));
        when(restTemplate.getForEntity(anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        MarketIndicator indicator = client.createMarketIndicator(
                BCBApiClient.SELIC_CODE,
                "Selic",
                "Brazilian base interest rate",
                MarketIndicator.IndicatorType.INTEREST_RATE,
                MarketIndicator.Frequency.DAILY
        );

        assertThat(indicator.getCode()).isEqualTo(BCBApiClient.SELIC_CODE);
        assertThat(indicator.getCurrentValue()).isEqualByComparingTo("3.14");
        assertThat(indicator.getLastUpdated()).isNotNull();
        assertThat(indicator.getIsActive()).isTrue();
    }
}
