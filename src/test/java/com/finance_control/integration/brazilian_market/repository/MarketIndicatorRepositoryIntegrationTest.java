package com.finance_control.integration.brazilian_market.repository;

import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.brazilian_market.repository.MarketIndicatorRepository;
import com.finance_control.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for MarketIndicatorRepository.
 */
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MarketIndicatorRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MarketIndicatorRepository indicatorRepository;

    private MarketIndicator selicIndicator;
    private MarketIndicator cdiIndicator;
    private MarketIndicator ipcaIndicator;
    private MarketIndicator exchangeRateIndicator;

    @BeforeEach
    void setUp() {
        // Create test indicators only if they don't exist
        selicIndicator = indicatorRepository.findByCode("SELIC")
                .orElseGet(() -> {
                    MarketIndicator indicator = createTestIndicator("SELIC", "Taxa Selic",
                            MarketIndicator.IndicatorType.INTEREST_RATE, MarketIndicator.Frequency.DAILY,
                            new BigDecimal("13.75"), new BigDecimal("13.50"), LocalDate.now());
                    return indicatorRepository.save(indicator);
                });

        cdiIndicator = indicatorRepository.findByCode("CDI")
                .orElseGet(() -> {
                    MarketIndicator indicator = createTestIndicator("CDI", "CDI",
                            MarketIndicator.IndicatorType.INTEREST_RATE, MarketIndicator.Frequency.DAILY,
                            new BigDecimal("13.25"), new BigDecimal("13.00"), LocalDate.now());
                    return indicatorRepository.save(indicator);
                });

        ipcaIndicator = indicatorRepository.findByCode("IPCA")
                .orElseGet(() -> {
                    MarketIndicator indicator = createTestIndicator("IPCA", "IPCA",
                            MarketIndicator.IndicatorType.INFLATION, MarketIndicator.Frequency.MONTHLY,
                            new BigDecimal("4.62"), new BigDecimal("4.50"), LocalDate.now().minusDays(1));
                    return indicatorRepository.save(indicator);
                });

        exchangeRateIndicator = indicatorRepository.findByCode("USD_BRL")
                .orElseGet(() -> {
                    MarketIndicator indicator = createTestIndicator("USD_BRL", "USD/BRL",
                            MarketIndicator.IndicatorType.EXCHANGE_RATE, MarketIndicator.Frequency.DAILY,
                            new BigDecimal("5.25"), new BigDecimal("5.20"), LocalDate.now());
                    return indicatorRepository.save(indicator);
                });
    }

    @Test
    void findByCode_WithValidCode_ShouldReturnIndicator() {
        // When
        Optional<MarketIndicator> result = indicatorRepository.findByCode("SELIC");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("SELIC");
        assertThat(result.get().getName()).isEqualTo("Taxa Selic");
        assertThat(result.get().getCurrentValue()).isEqualTo(new BigDecimal("13.75"));
    }

    @Test
    void findByCode_WithInvalidCode_ShouldReturnEmpty() {
        // When
        Optional<MarketIndicator> result = indicatorRepository.findByCode("INVALID");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByIndicatorType_ShouldReturnFilteredIndicators() {
        // When
        List<MarketIndicator> result = indicatorRepository.findByIndicatorType(MarketIndicator.IndicatorType.INTEREST_RATE);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(MarketIndicator::getCode)
                .containsExactlyInAnyOrder("SELIC", "CDI");
    }

    @Test
    void findByFrequency_ShouldReturnFilteredIndicators() {
        // When
        List<MarketIndicator> result = indicatorRepository.findByFrequency(MarketIndicator.Frequency.DAILY);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(MarketIndicator::getCode)
                .containsExactlyInAnyOrder("SELIC", "CDI", "USD_BRL");
    }

    @Test
    void findByIsActiveTrue_ShouldReturnActiveIndicators() {
        // Given
        ipcaIndicator.setIsActive(false);
        indicatorRepository.save(ipcaIndicator);

        // When
        List<MarketIndicator> result = indicatorRepository.findByIsActiveTrue();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(MarketIndicator::getCode)
                .containsExactlyInAnyOrder("SELIC", "CDI", "USD_BRL");
    }

    @Test
    void findByIndicatorTypeAndFrequency_ShouldReturnFilteredIndicators() {
        // When
        List<MarketIndicator> result = indicatorRepository.findByIndicatorTypeAndFrequency(
                MarketIndicator.IndicatorType.INTEREST_RATE, MarketIndicator.Frequency.DAILY);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(MarketIndicator::getCode)
                .containsExactlyInAnyOrder("SELIC", "CDI");
    }

    @Test
    void searchByNameOrCode_WithCode_ShouldReturnMatchingIndicators() {
        // When
        List<MarketIndicator> result = indicatorRepository.searchByNameOrCode("SELIC");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("SELIC");
    }

    @Test
    void searchByNameOrCode_WithName_ShouldReturnMatchingIndicators() {
        // When
        List<MarketIndicator> result = indicatorRepository.searchByNameOrCode("Taxa");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("SELIC");
    }

    @Test
    void searchByNameOrCode_WithPartialMatch_ShouldReturnMatchingIndicators() {
        // When
        List<MarketIndicator> result = indicatorRepository.searchByNameOrCode("USD");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("USD_BRL");
    }

    @Test
    void findKeyIndicators_ShouldReturnKeyEconomicIndicators() {
        // When
        List<MarketIndicator> result = indicatorRepository.findKeyIndicators();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(MarketIndicator::getCode)
                .containsExactlyInAnyOrder("SELIC", "CDI", "IPCA");
    }

    @Test
    void findByLastUpdatedAfter_ShouldReturnRecentlyUpdatedIndicators() {
        // Given
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        selicIndicator.setLastUpdated(LocalDateTime.now().minusHours(2));
        cdiIndicator.setLastUpdated(LocalDateTime.now().minusMinutes(30));
        indicatorRepository.saveAll(List.of(selicIndicator, cdiIndicator));

        // When
        List<MarketIndicator> result = indicatorRepository.findByLastUpdatedAfter(cutoffTime);

        // Then
        assertThat(result).hasSize(3); // cdiIndicator + ipcaIndicator + exchangeRateIndicator
        assertThat(result).extracting(MarketIndicator::getCode)
                .containsExactlyInAnyOrder("CDI", "IPCA", "USD_BRL");
    }

    @Test
    void findByReferenceDateBetween_ShouldReturnIndicatorsInDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();

        // When
        List<MarketIndicator> result = indicatorRepository.findByReferenceDateBetween(startDate, endDate);

        // Then
        assertThat(result).hasSize(4); // selicIndicator, cdiIndicator, ipcaIndicator, exchangeRateIndicator
        assertThat(result).extracting(MarketIndicator::getCode)
                .containsExactlyInAnyOrder("SELIC", "CDI", "IPCA", "USD_BRL");
    }

    @Test
    void countByIndicatorType_ShouldReturnCorrectCount() {
        // When
        long count = indicatorRepository.countByIndicatorType(MarketIndicator.IndicatorType.INTEREST_RATE);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByFrequency_ShouldReturnCorrectCount() {
        // When
        long count = indicatorRepository.countByFrequency(MarketIndicator.Frequency.DAILY);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void existsByCode_WithExistingCode_ShouldReturnTrue() {
        // When
        boolean exists = indicatorRepository.existsByCode("SELIC");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_WithNonExistingCode_ShouldReturnFalse() {
        // When
        boolean exists = indicatorRepository.existsByCode("INVALID");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findBySignificantChange_ShouldReturnIndicatorsWithSignificantChanges() {
        // Given
        selicIndicator.setChangePercent(new BigDecimal("1.85")); // 1.85% change
        cdiIndicator.setChangePercent(new BigDecimal("1.92")); // 1.92% change
        ipcaIndicator.setChangePercent(new BigDecimal("2.67")); // 2.67% change
        exchangeRateIndicator.setChangePercent(new BigDecimal("0.96")); // 0.96% change
        indicatorRepository.saveAll(List.of(selicIndicator, cdiIndicator, ipcaIndicator, exchangeRateIndicator));

        // When
        List<MarketIndicator> result = indicatorRepository.findBySignificantChange(new BigDecimal("2.0"));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("IPCA");
    }

    @Test
    void findMostRecentlyUpdated_ShouldReturnIndicatorsOrderedByLastUpdated() {
        // Given
        selicIndicator.setLastUpdated(LocalDateTime.now().minusHours(3));
        cdiIndicator.setLastUpdated(LocalDateTime.now().minusHours(1));
        ipcaIndicator.setLastUpdated(LocalDateTime.now().minusHours(2));
        exchangeRateIndicator.setLastUpdated(LocalDateTime.now().minusMinutes(30));
        indicatorRepository.saveAll(List.of(selicIndicator, cdiIndicator, ipcaIndicator, exchangeRateIndicator));

        // When
        List<MarketIndicator> result = indicatorRepository.findMostRecentlyUpdated(PageRequest.of(0, 2));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("USD_BRL"); // Most recent
        assertThat(result.get(1).getCode()).isEqualTo("CDI"); // Second most recent
    }

    private MarketIndicator createTestIndicator(String code, String name,
                                              MarketIndicator.IndicatorType type,
                                              MarketIndicator.Frequency frequency,
                                              BigDecimal currentValue,
                                              BigDecimal previousValue,
                                              LocalDate referenceDate) {
        MarketIndicator indicator = new MarketIndicator();
        indicator.setCode(code);
        indicator.setName(name);
        indicator.setDescription("Test indicator: " + name);
        indicator.setIndicatorType(type);
        indicator.setFrequency(frequency);
        indicator.setCurrentValue(currentValue);
        indicator.setPreviousValue(previousValue);
        indicator.setChangeValue(currentValue.subtract(previousValue));
        indicator.setChangePercent(currentValue.subtract(previousValue)
                .divide(previousValue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)));
        indicator.setReferenceDate(referenceDate);
        indicator.setLastUpdated(LocalDateTime.now());
        indicator.setIsActive(true);
        return indicator;
    }
}
