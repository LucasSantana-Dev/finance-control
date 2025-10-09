package com.finance_control.brazilian_market.service;

import com.finance_control.brazilian_market.client.BCBApiClient;
import com.finance_control.brazilian_market.client.BrazilianStocksApiClient;
import com.finance_control.brazilian_market.model.*;
import com.finance_control.brazilian_market.repository.*;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Service for managing Brazilian market data including stocks, FIIs, and economic indicators.
 * Provides real-time data fetching, caching, and analysis capabilities.
 */
@Service
@Transactional
@Slf4j
public class BrazilianMarketDataService {

    private final BCBApiClient bcbApiClient;
    private final BrazilianStocksApiClient stocksApiClient;
    private final BrazilianStockRepository stockRepository;
    private final FIIRepository fiiRepository;
    private final MarketIndicatorRepository indicatorRepository;
    private final UserRepository userRepository;

    @Autowired
    public BrazilianMarketDataService(BCBApiClient bcbApiClient,
                                    BrazilianStocksApiClient stocksApiClient,
                                    BrazilianStockRepository stockRepository,
                                    FIIRepository fiiRepository,
                                    MarketIndicatorRepository indicatorRepository,
                                    UserRepository userRepository) {
        this.bcbApiClient = bcbApiClient;
        this.stocksApiClient = stocksApiClient;
        this.stockRepository = stockRepository;
        this.fiiRepository = fiiRepository;
        this.indicatorRepository = indicatorRepository;
        this.userRepository = userRepository;
    }

    /**
     * Fetches and updates current Selic rate.
     */
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

    /**
     * Fetches and updates current CDI rate.
     */
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

    /**
     * Fetches and updates current IPCA inflation rate.
     */
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

    /**
     * Fetches and updates stock data for a specific ticker.
     */
    @Async
    public CompletableFuture<BrazilianStock> updateStockData(String ticker, Long userId) {
        try {
            log.info("Updating stock data for ticker: {} for user: {}", ticker, userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            BrazilianStock stockData = stocksApiClient.getStockQuote(ticker);
            if (stockData == null) {
                throw new IllegalArgumentException("Stock data not found for ticker: " + ticker);
            }

            stockData.setUser(user);

            Optional<BrazilianStock> existingStock = stockRepository.findByTickerAndUserId(ticker, userId);
            if (existingStock.isPresent()) {
                BrazilianStock existing = existingStock.get();
                existing.updatePrice(stockData.getCurrentPrice());
                existing.setVolume(stockData.getVolume());
                existing.setMarketCap(stockData.getMarketCap());
                existing.setLastUpdated(LocalDateTime.now());

                BrazilianStock saved = stockRepository.save(existing);
                log.info("Stock data updated for ticker: {}", ticker);
                return CompletableFuture.completedFuture(saved);
            } else {
                BrazilianStock saved = stockRepository.save(stockData);
                log.info("New stock data saved for ticker: {}", ticker);
                return CompletableFuture.completedFuture(saved);
            }
        } catch (Exception e) {
            log.error("Error updating stock data for ticker: {}", ticker, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Fetches and updates FII data for a specific ticker.
     */
    @Async
    public CompletableFuture<FII> updateFIIData(String ticker, Long userId) {
        try {
            log.info("Updating FII data for ticker: {} for user: {}", ticker, userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            FII fiiData = stocksApiClient.getFIIQuote(ticker);
            if (fiiData == null) {
                throw new IllegalArgumentException("FII data not found for ticker: " + ticker);
            }

            fiiData.setUser(user);

            Optional<FII> existingFII = fiiRepository.findByTickerAndUserId(ticker, userId);
            if (existingFII.isPresent()) {
                FII existing = existingFII.get();
                existing.updatePrice(fiiData.getCurrentPrice());
                existing.setVolume(fiiData.getVolume());
                existing.setMarketCap(fiiData.getMarketCap());
                existing.setDividendYield(fiiData.getDividendYield());
                existing.setLastDividend(fiiData.getLastDividend());
                existing.setLastDividendDate(fiiData.getLastDividendDate());
                existing.setNetWorth(fiiData.getNetWorth());
                existing.setLastUpdated(LocalDateTime.now());

                FII saved = fiiRepository.save(existing);
                log.info("FII data updated for ticker: {}", ticker);
                return CompletableFuture.completedFuture(saved);
            } else {
                FII saved = fiiRepository.save(fiiData);
                log.info("New FII data saved for ticker: {}", ticker);
                return CompletableFuture.completedFuture(saved);
            }
        } catch (Exception e) {
            log.error("Error updating FII data for ticker: {}", ticker, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Gets current Selic rate.
     */
    public BigDecimal getCurrentSelicRate() {
        return indicatorRepository.findByCode("SELIC")
                .map(MarketIndicator::getCurrentValue)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Gets current CDI rate.
     */
    public BigDecimal getCurrentCDIRate() {
        return indicatorRepository.findByCode("CDI")
                .map(MarketIndicator::getCurrentValue)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Gets current IPCA.
     */
    public BigDecimal getCurrentIPCA() {
        return indicatorRepository.findByCode("IPCA")
                .map(MarketIndicator::getCurrentValue)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Gets all key economic indicators.
     */
    public List<MarketIndicator> getKeyIndicators() {
        return indicatorRepository.findKeyIndicators();
    }

    /**
     * Gets all stocks for a user.
     */
    public List<BrazilianStock> getUserStocks(Long userId) {
        return stockRepository.findByUserId(userId);
    }

    /**
     * Gets all FIIs for a user.
     */
    public List<FII> getUserFIIs(Long userId) {
        return fiiRepository.findByUserId(userId);
    }

    /**
     * Searches stocks for a user.
     */
    public List<BrazilianStock> searchUserStocks(Long userId, String query) {
        return stockRepository.searchByUserAndQuery(userId, query);
    }

    /**
     * Searches FIIs for a user.
     */
    public List<FII> searchUserFIIs(Long userId, String query) {
        return fiiRepository.searchByUserAndQuery(userId, query);
    }

    /**
     * Scheduled task to update key indicators every hour.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
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

    /**
     * Gets market summary data.
     */
    public Object getMarketSummary() {
        return stocksApiClient.getMarketSummary();
    }
}
