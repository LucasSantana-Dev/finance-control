package com.finance_control.brazilian_market.service;

import com.finance_control.brazilian_market.client.BCBApiClient;
import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.brazilian_market.repository.MarketIndicatorRepository;
import com.finance_control.shared.monitoring.MetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing Brazilian market indicators and economic data.
 * Provides real-time data fetching, caching, and analysis capabilities for market indicators.
 */
@Service
@Transactional
@Slf4j
public class BrazilianMarketDataService {

    private final BCBApiClient bcbApiClient;
    private final MarketIndicatorRepository indicatorRepository;
    private final MetricsService metricsService;

    @Autowired
    public BrazilianMarketDataService(BCBApiClient bcbApiClient,
                                    MarketIndicatorRepository indicatorRepository,
                                    MetricsService metricsService) {
        this.bcbApiClient = bcbApiClient;
        this.indicatorRepository = indicatorRepository;
        this.metricsService = metricsService;
    }

    @Async
    public CompletableFuture<MarketIndicator> updateSelicRate() {
        try {
            log.info("Updating Selic rate from BCB");
            BigDecimal currentRate = bcbApiClient.getCurrentSelicRate();

            Optional<MarketIndicator> existingIndicator = indicatorRepository.findByCode("SELIC");
            MarketIndicator indicator;

            if (existingIndicator.isPresent()) {
                indicator = existingIndicator.get();
                indicator.updateValue(currentRate, LocalDate.now());
            } else {
                indicator = bcbApiClient.createMarketIndicator(
                    "SELIC",
                    "Taxa Selic",
                    "Taxa básica de juros da economia brasileira",
                    MarketIndicator.IndicatorType.INTEREST_RATE,
                    MarketIndicator.Frequency.DAILY
                );
                indicator.setCurrentValue(currentRate);
                indicator.setReferenceDate(LocalDate.now());
            }

            MarketIndicator saved = indicatorRepository.save(indicator);
            log.info("Selic rate updated: {}%", currentRate);
            return CompletableFuture.completedFuture(saved);
        } catch (Exception e) {
            log.error("Error updating Selic rate", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<MarketIndicator> updateCDIRate() {
        try {
            log.info("Updating CDI rate from BCB");
            BigDecimal currentRate = bcbApiClient.getCurrentCDIRate();

            Optional<MarketIndicator> existingIndicator = indicatorRepository.findByCode("CDI");
            MarketIndicator indicator;

            if (existingIndicator.isPresent()) {
                indicator = existingIndicator.get();
                indicator.updateValue(currentRate, LocalDate.now());
            } else {
                indicator = bcbApiClient.createMarketIndicator(
                    "CDI",
                    "CDI",
                    "Certificado de Depósito Interbancário",
                    MarketIndicator.IndicatorType.INTEREST_RATE,
                    MarketIndicator.Frequency.DAILY
                );
                indicator.setCurrentValue(currentRate);
                indicator.setReferenceDate(LocalDate.now());
            }

            MarketIndicator saved = indicatorRepository.save(indicator);
            log.info("CDI rate updated: {}%", currentRate);
            return CompletableFuture.completedFuture(saved);
        } catch (Exception e) {
            log.error("Error updating CDI rate", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<MarketIndicator> updateIPCA() {
        try {
            log.info("Updating IPCA from BCB");
            BigDecimal currentIPCA = bcbApiClient.getCurrentIPCA();

            Optional<MarketIndicator> existingIndicator = indicatorRepository.findByCode("IPCA");
            MarketIndicator indicator;

            if (existingIndicator.isPresent()) {
                indicator = existingIndicator.get();
                indicator.updateValue(currentIPCA, LocalDate.now());
            } else {
                indicator = bcbApiClient.createMarketIndicator(
                    "IPCA",
                    "IPCA",
                    "Índice Nacional de Preços ao Consumidor Amplo",
                    MarketIndicator.IndicatorType.INFLATION,
                    MarketIndicator.Frequency.MONTHLY
                );
                indicator.setCurrentValue(currentIPCA);
                indicator.setReferenceDate(LocalDate.now());
            }

            MarketIndicator saved = indicatorRepository.save(indicator);
            log.info("IPCA updated: {}%", currentIPCA);
            return CompletableFuture.completedFuture(saved);
        } catch (Exception e) {
            log.error("Error updating IPCA", e);
            return CompletableFuture.failedFuture(e);
        }
    }



    @Cacheable(value = "market-data", key = "'selic_rate'")
    public BigDecimal getCurrentSelicRate() {
        return indicatorRepository.findByCode("SELIC")
                .map(MarketIndicator::getCurrentValue)
                .orElse(BigDecimal.ZERO);
    }

    @Cacheable(value = "market-data", key = "'cdi_rate'")
    public BigDecimal getCurrentCDIRate() {
        return indicatorRepository.findByCode("CDI")
                .map(MarketIndicator::getCurrentValue)
                .orElse(BigDecimal.ZERO);
    }

    @Cacheable(value = "market-data", key = "'ipca_rate'")
    public BigDecimal getCurrentIPCA() {
        return indicatorRepository.findByCode("IPCA")
                .map(MarketIndicator::getCurrentValue)
                .orElse(BigDecimal.ZERO);
    }

    @Cacheable(value = "market-data", key = "'key_indicators'")
    public List<MarketIndicator> getKeyIndicators() {
        return indicatorRepository.findKeyIndicators();
    }


    @Scheduled(fixedRate = 3600000)
    @CacheEvict(value = "market-data", allEntries = true)
    public void updateKeyIndicators() {
        log.info("Starting scheduled update of key indicators");

        CompletableFuture.allOf(
            updateSelicRate(),
            updateCDIRate(),
            updateIPCA()
        ).thenRun(() -> {
            log.info("Key indicators update completed");
        }).exceptionally(throwable -> {
            log.error("Error in scheduled indicators update", throwable);
            return null;
        });
    }

    public Object getMarketSummary() {
        var sample = metricsService.startMarketDataFetchTimer();
        try {
            // Return market indicators as summary since we no longer have stock/FII data
            return getKeyIndicators();
        } finally {
            metricsService.recordMarketDataFetchTime(sample);
        }
    }
}
