package com.finance_control.brazilian_market.client;

import com.finance_control.brazilian_market.model.MarketIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client for accessing Banco Central do Brasil (BCB) APIs.
 * Provides access to economic indicators like Selic rate, CDI, IPCA, etc.
 */
@Component
@Slf4j
public class BCBApiClient {

    private final RestTemplate restTemplate;
    private final String bcbBaseUrl;
    private final String bcbApiKey;

    // BCB SGS (Sistema Gerenciador de Séries Temporais) codes
    public static final String SELIC_CODE = "432";
    public static final String CDI_CODE = "12";
    public static final String IPCA_CODE = "433";
    public static final String IGP_M_CODE = "189";
    public static final String EXCHANGE_RATE_CODE = "1";

    public BCBApiClient(RestTemplate restTemplate,
                       @Value("${brazilian-market.bcb.base-url:https://api.bcb.gov.br/dados/serie/bcdata.sgs}") String bcbBaseUrl,
                       @Value("${brazilian-market.bcb.api-key:}") String bcbApiKey) {
        this.restTemplate = restTemplate;
        this.bcbBaseUrl = bcbBaseUrl;
        this.bcbApiKey = bcbApiKey;
    }

    /**
     * Fetches the current Selic rate from BCB.
     */
    public BigDecimal getCurrentSelicRate() {
        try {
            log.debug("Fetching current Selic rate from BCB");
            List<Map<String, Object>> data = fetchIndicatorData(SELIC_CODE, 1);
            
            if (!data.isEmpty()) {
                Map<String, Object> latestData = data.get(0);
                Object value = latestData.get("valor");
                if (value != null) {
                    return new BigDecimal(value.toString());
                }
            }
            
            log.warn("No Selic rate data found");
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error fetching Selic rate from BCB", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Fetches the current CDI rate from BCB.
     */
    public BigDecimal getCurrentCDIRate() {
        try {
            log.debug("Fetching current CDI rate from BCB");
            List<Map<String, Object>> data = fetchIndicatorData(CDI_CODE, 1);
            
            if (!data.isEmpty()) {
                Map<String, Object> latestData = data.get(0);
                Object value = latestData.get("valor");
                if (value != null) {
                    return new BigDecimal(value.toString());
                }
            }
            
            log.warn("No CDI rate data found");
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error fetching CDI rate from BCB", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Fetches the current IPCA inflation rate from BCB.
     */
    public BigDecimal getCurrentIPCA() {
        try {
            log.debug("Fetching current IPCA from BCB");
            List<Map<String, Object>> data = fetchIndicatorData(IPCA_CODE, 1);
            
            if (!data.isEmpty()) {
                Map<String, Object> latestData = data.get(0);
                Object value = latestData.get("valor");
                if (value != null) {
                    return new BigDecimal(value.toString());
                }
            }
            
            log.warn("No IPCA data found");
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error fetching IPCA from BCB", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Fetches the current USD/BRL exchange rate from BCB.
     */
    public BigDecimal getCurrentExchangeRate() {
        try {
            log.debug("Fetching current USD/BRL exchange rate from BCB");
            List<Map<String, Object>> data = fetchIndicatorData(EXCHANGE_RATE_CODE, 1);
            
            if (!data.isEmpty()) {
                Map<String, Object> latestData = data.get(0);
                Object value = latestData.get("valor");
                if (value != null) {
                    return new BigDecimal(value.toString());
                }
            }
            
            log.warn("No exchange rate data found");
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error fetching exchange rate from BCB", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Fetches historical data for a specific indicator.
     */
    public List<Map<String, Object>> getHistoricalData(String indicatorCode, int lastValues) {
        try {
            log.debug("Fetching historical data for indicator {} (last {} values)", indicatorCode, lastValues);
            return fetchIndicatorData(indicatorCode, lastValues);
        } catch (Exception e) {
            log.error("Error fetching historical data for indicator {}", indicatorCode, e);
            return new ArrayList<>();
        }
    }

    /**
     * Fetches data for a specific indicator within a date range.
     */
    public List<Map<String, Object>> getDataInRange(String indicatorCode, LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching data for indicator {} from {} to {}", indicatorCode, startDate, endDate);
            
            String url = UriComponentsBuilder.fromHttpUrl(bcbBaseUrl)
                    .pathSegment(indicatorCode, "dados")
                    .queryParam("formato", "json")
                    .queryParam("dataInicial", startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .queryParam("dataFinal", endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .build()
                    .toUriString();

            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
            log.warn("No data found for indicator {} in the specified range", indicatorCode);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching data in range for indicator {}", indicatorCode, e);
            return new ArrayList<>();
        }
    }

    /**
     * Creates a MarketIndicator object from BCB data.
     */
    public MarketIndicator createMarketIndicator(String code, String name, String description,
                                               MarketIndicator.IndicatorType type, MarketIndicator.Frequency frequency) {
        MarketIndicator indicator = new MarketIndicator();
        indicator.setCode(code);
        indicator.setName(name);
        indicator.setDescription(description);
        indicator.setIndicatorType(type);
        indicator.setFrequency(frequency);
        indicator.setIsActive(true);
        
        // Fetch current value
        BigDecimal currentValue = getCurrentValueForCode(code);
        if (currentValue != null) {
            indicator.setCurrentValue(currentValue);
            indicator.setLastUpdated(java.time.LocalDateTime.now());
        }
        
        return indicator;
    }

    /**
     * Private method to fetch indicator data from BCB API.
     */
    private List<Map<String, Object>> fetchIndicatorData(String indicatorCode, int lastValues) {
        String url = UriComponentsBuilder.fromHttpUrl(bcbBaseUrl)
                .pathSegment(indicatorCode, "dados")
                .queryParam("formato", "json")
                .queryParam("ultimos", lastValues)
                .build()
                .toUriString();

        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }
        
        return new ArrayList<>();
    }

    /**
     * Gets the current value for a specific indicator code.
     */
    private BigDecimal getCurrentValueForCode(String code) {
        try {
            List<Map<String, Object>> data = fetchIndicatorData(code, 1);
            if (!data.isEmpty()) {
                Map<String, Object> latestData = data.get(0);
                Object value = latestData.get("valor");
                if (value != null) {
                    return new BigDecimal(value.toString());
                }
            }
        } catch (Exception e) {
            log.error("Error getting current value for code {}", code, e);
        }
        return null;
    }
}
