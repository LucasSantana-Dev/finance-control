package com.finance_control.unit.brazilian_market.service;

import com.finance_control.brazilian_market.client.BCBApiClient;
import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.brazilian_market.repository.MarketIndicatorRepository;
import com.finance_control.brazilian_market.service.BrazilianMarketDataService;
import com.finance_control.shared.monitoring.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrazilianMarketDataService Unit Tests")
class BrazilianMarketDataServiceTest {

    @Mock
    private BCBApiClient bcbApiClient;

    @Mock
    private MarketIndicatorRepository indicatorRepository;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private BrazilianMarketDataService service;

    private MarketIndicator selicIndicator;
    private MarketIndicator cdiIndicator;
    private MarketIndicator ipcaIndicator;

    @BeforeEach
    void setUp() {
        selicIndicator = new MarketIndicator();
        selicIndicator.setCode("SELIC");
        selicIndicator.setName("Taxa Selic");
        selicIndicator.setDescription("Taxa básica de juros da economia brasileira");
        selicIndicator.setIndicatorType(MarketIndicator.IndicatorType.INTEREST_RATE);
        selicIndicator.setFrequency(MarketIndicator.Frequency.DAILY);
        selicIndicator.setCurrentValue(new BigDecimal("13.75"));
        selicIndicator.setReferenceDate(LocalDate.now());

        cdiIndicator = new MarketIndicator();
        cdiIndicator.setCode("CDI");
        cdiIndicator.setName("CDI");
        cdiIndicator.setDescription("Certificado de Depósito Interbancário");
        cdiIndicator.setIndicatorType(MarketIndicator.IndicatorType.INTEREST_RATE);
        cdiIndicator.setFrequency(MarketIndicator.Frequency.DAILY);
        cdiIndicator.setCurrentValue(new BigDecimal("13.65"));
        cdiIndicator.setReferenceDate(LocalDate.now());

        ipcaIndicator = new MarketIndicator();
        ipcaIndicator.setCode("IPCA");
        ipcaIndicator.setName("IPCA");
        ipcaIndicator.setDescription("Índice Nacional de Preços ao Consumidor Amplo");
        ipcaIndicator.setIndicatorType(MarketIndicator.IndicatorType.INFLATION);
        ipcaIndicator.setFrequency(MarketIndicator.Frequency.MONTHLY);
        ipcaIndicator.setCurrentValue(new BigDecimal("4.62"));
        ipcaIndicator.setReferenceDate(LocalDate.now());
    }

    @Test
    @DisplayName("Should update Selic rate when indicator exists")
    void shouldUpdateSelicRateWhenIndicatorExists() {
        // Given
        BigDecimal newRate = new BigDecimal("14.25");
        when(bcbApiClient.getCurrentSelicRate()).thenReturn(newRate);
        when(indicatorRepository.findByCode("SELIC")).thenReturn(Optional.of(selicIndicator));
        when(indicatorRepository.save(any(MarketIndicator.class))).thenReturn(selicIndicator);

        // When
        CompletableFuture<MarketIndicator> result = service.updateSelicRate();

        // Then
        assertThat(result).isCompleted();
        verify(bcbApiClient).getCurrentSelicRate();
        verify(indicatorRepository).findByCode("SELIC");
        verify(indicatorRepository).save(selicIndicator);
        verify(selicIndicator).updateValue(newRate, LocalDate.now());
    }

    @Test
    @DisplayName("Should create new Selic indicator when it doesn't exist")
    void shouldCreateNewSelicIndicatorWhenItDoesntExist() {
        // Given
        BigDecimal newRate = new BigDecimal("14.25");
        when(bcbApiClient.getCurrentSelicRate()).thenReturn(newRate);
        when(indicatorRepository.findByCode("SELIC")).thenReturn(Optional.empty());
        when(bcbApiClient.createMarketIndicator(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(selicIndicator);
        when(indicatorRepository.save(any(MarketIndicator.class))).thenReturn(selicIndicator);

        // When
        CompletableFuture<MarketIndicator> result = service.updateSelicRate();

        // Then
        assertThat(result).isCompleted();
        verify(bcbApiClient).getCurrentSelicRate();
        verify(indicatorRepository).findByCode("SELIC");
        verify(bcbApiClient).createMarketIndicator(
                "SELIC",
                "Taxa Selic",
                "Taxa básica de juros da economia brasileira",
                MarketIndicator.IndicatorType.INTEREST_RATE,
                MarketIndicator.Frequency.DAILY
        );
        verify(indicatorRepository).save(selicIndicator);
    }

    @Test
    @DisplayName("Should handle exception when updating Selic rate")
    void shouldHandleExceptionWhenUpdatingSelicRate() {
        // Given
        when(bcbApiClient.getCurrentSelicRate()).thenThrow(new RuntimeException("API Error"));

        // When
        CompletableFuture<MarketIndicator> result = service.updateSelicRate();

        // Then
        assertThat(result).isCompletedExceptionally();
        verify(bcbApiClient).getCurrentSelicRate();
        verify(indicatorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update CDI rate when indicator exists")
    void shouldUpdateCDIRateWhenIndicatorExists() {
        // Given
        BigDecimal newRate = new BigDecimal("13.85");
        when(bcbApiClient.getCurrentCDIRate()).thenReturn(newRate);
        when(indicatorRepository.findByCode("CDI")).thenReturn(Optional.of(cdiIndicator));
        when(indicatorRepository.save(any(MarketIndicator.class))).thenReturn(cdiIndicator);

        // When
        CompletableFuture<MarketIndicator> result = service.updateCDIRate();

        // Then
        assertThat(result).isCompleted();
        verify(bcbApiClient).getCurrentCDIRate();
        verify(indicatorRepository).findByCode("CDI");
        verify(indicatorRepository).save(cdiIndicator);
        verify(cdiIndicator).updateValue(newRate, LocalDate.now());
    }

    @Test
    @DisplayName("Should create new CDI indicator when it doesn't exist")
    void shouldCreateNewCDIIndicatorWhenItDoesntExist() {
        // Given
        BigDecimal newRate = new BigDecimal("13.85");
        when(bcbApiClient.getCurrentCDIRate()).thenReturn(newRate);
        when(indicatorRepository.findByCode("CDI")).thenReturn(Optional.empty());
        when(bcbApiClient.createMarketIndicator(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(cdiIndicator);
        when(indicatorRepository.save(any(MarketIndicator.class))).thenReturn(cdiIndicator);

        // When
        CompletableFuture<MarketIndicator> result = service.updateCDIRate();

        // Then
        assertThat(result).isCompleted();
        verify(bcbApiClient).getCurrentCDIRate();
        verify(indicatorRepository).findByCode("CDI");
        verify(bcbApiClient).createMarketIndicator(
                "CDI",
                "CDI",
                "Certificado de Depósito Interbancário",
                MarketIndicator.IndicatorType.INTEREST_RATE,
                MarketIndicator.Frequency.DAILY
        );
        verify(indicatorRepository).save(cdiIndicator);
    }

    @Test
    @DisplayName("Should update IPCA when indicator exists")
    void shouldUpdateIPCAWhenIndicatorExists() {
        // Given
        BigDecimal newIPCA = new BigDecimal("4.85");
        when(bcbApiClient.getCurrentIPCA()).thenReturn(newIPCA);
        when(indicatorRepository.findByCode("IPCA")).thenReturn(Optional.of(ipcaIndicator));
        when(indicatorRepository.save(any(MarketIndicator.class))).thenReturn(ipcaIndicator);

        // When
        CompletableFuture<MarketIndicator> result = service.updateIPCA();

        // Then
        assertThat(result).isCompleted();
        verify(bcbApiClient).getCurrentIPCA();
        verify(indicatorRepository).findByCode("IPCA");
        verify(indicatorRepository).save(ipcaIndicator);
        verify(ipcaIndicator).updateValue(newIPCA, LocalDate.now());
    }

    @Test
    @DisplayName("Should create new IPCA indicator when it doesn't exist")
    void shouldCreateNewIPCAIndicatorWhenItDoesntExist() {
        // Given
        BigDecimal newIPCA = new BigDecimal("4.85");
        when(bcbApiClient.getCurrentIPCA()).thenReturn(newIPCA);
        when(indicatorRepository.findByCode("IPCA")).thenReturn(Optional.empty());
        when(bcbApiClient.createMarketIndicator(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(ipcaIndicator);
        when(indicatorRepository.save(any(MarketIndicator.class))).thenReturn(ipcaIndicator);

        // When
        CompletableFuture<MarketIndicator> result = service.updateIPCA();

        // Then
        assertThat(result).isCompleted();
        verify(bcbApiClient).getCurrentIPCA();
        verify(indicatorRepository).findByCode("IPCA");
        verify(bcbApiClient).createMarketIndicator(
                "IPCA",
                "IPCA",
                "Índice Nacional de Preços ao Consumidor Amplo",
                MarketIndicator.IndicatorType.INFLATION,
                MarketIndicator.Frequency.MONTHLY
        );
        verify(indicatorRepository).save(ipcaIndicator);
    }

    @Test
    @DisplayName("Should get current Selic rate from cache")
    void shouldGetCurrentSelicRateFromCache() {
        // Given
        when(indicatorRepository.findByCode("SELIC")).thenReturn(Optional.of(selicIndicator));

        // When
        BigDecimal result = service.getCurrentSelicRate();

        // Then
        assertThat(result).isEqualTo(new BigDecimal("13.75"));
        verify(indicatorRepository).findByCode("SELIC");
    }

    @Test
    @DisplayName("Should return zero when Selic rate not found")
    void shouldReturnZeroWhenSelicRateNotFound() {
        // Given
        when(indicatorRepository.findByCode("SELIC")).thenReturn(Optional.empty());

        // When
        BigDecimal result = service.getCurrentSelicRate();

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
        verify(indicatorRepository).findByCode("SELIC");
    }

    @Test
    @DisplayName("Should get current CDI rate from cache")
    void shouldGetCurrentCDIRateFromCache() {
        // Given
        when(indicatorRepository.findByCode("CDI")).thenReturn(Optional.of(cdiIndicator));

        // When
        BigDecimal result = service.getCurrentCDIRate();

        // Then
        assertThat(result).isEqualTo(new BigDecimal("13.65"));
        verify(indicatorRepository).findByCode("CDI");
    }

    @Test
    @DisplayName("Should get current IPCA from cache")
    void shouldGetCurrentIPCAFromCache() {
        // Given
        when(indicatorRepository.findByCode("IPCA")).thenReturn(Optional.of(ipcaIndicator));

        // When
        BigDecimal result = service.getCurrentIPCA();

        // Then
        assertThat(result).isEqualTo(new BigDecimal("4.62"));
        verify(indicatorRepository).findByCode("IPCA");
    }

    @Test
    @DisplayName("Should get key indicators from cache")
    void shouldGetKeyIndicatorsFromCache() {
        // Given
        List<MarketIndicator> indicators = List.of(selicIndicator, cdiIndicator, ipcaIndicator);
        when(indicatorRepository.findKeyIndicators()).thenReturn(indicators);

        // When
        List<MarketIndicator> result = service.getKeyIndicators();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(selicIndicator, cdiIndicator, ipcaIndicator);
        verify(indicatorRepository).findKeyIndicators();
    }

    @Test
    @DisplayName("Should get market summary with metrics")
    void shouldGetMarketSummaryWithMetrics() {
        // Given
        List<MarketIndicator> indicators = List.of(selicIndicator, cdiIndicator, ipcaIndicator);
        when(indicatorRepository.findKeyIndicators()).thenReturn(indicators);
        when(metricsService.startMarketDataFetchTimer()).thenReturn(Instant.now());

        // When
        Object result = service.getMarketSummary();

        // Then
        assertThat(result).isEqualTo(indicators);
        verify(metricsService).startMarketDataFetchTimer();
        verify(metricsService).recordMarketDataFetchTime(any(Instant.class));
        verify(indicatorRepository).findKeyIndicators();
    }

    @Test
    @DisplayName("Should handle exception in market summary")
    void shouldHandleExceptionInMarketSummary() {
        // Given
        when(metricsService.startMarketDataFetchTimer()).thenReturn(Instant.now());
        when(indicatorRepository.findKeyIndicators()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> service.getMarketSummary())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(metricsService).startMarketDataFetchTimer();
        verify(metricsService).recordMarketDataFetchTime(any(Instant.class));
    }
}
