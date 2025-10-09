package com.finance_control.brazilian_market.controller;

import com.finance_control.brazilian_market.model.BrazilianStock;
import com.finance_control.brazilian_market.model.FII;
import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.brazilian_market.service.BrazilianMarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for Brazilian market data operations.
 * Provides endpoints for stocks, FIIs, and economic indicators.
 */
@RestController
@RequestMapping("/api/brazilian-market")
@Tag(name = "Brazilian Market", description = "Endpoints for Brazilian market data including stocks, FIIs, and economic indicators")
@Slf4j
public class BrazilianMarketController {

    private final BrazilianMarketDataService marketDataService;

    public BrazilianMarketController(BrazilianMarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    /**
     * Gets current Selic rate.
     */
    @GetMapping("/indicators/selic")
    @Operation(summary = "Get current Selic rate", description = "Retrieves the current Selic interest rate from BCB")
    public ResponseEntity<BigDecimal> getCurrentSelicRate() {
        log.debug("GET request to retrieve current Selic rate");
        BigDecimal selicRate = marketDataService.getCurrentSelicRate();
        return ResponseEntity.ok(selicRate);
    }

    /**
     * Gets current CDI rate.
     */
    @GetMapping("/indicators/cdi")
    @Operation(summary = "Get current CDI rate", description = "Retrieves the current CDI interest rate from BCB")
    public ResponseEntity<BigDecimal> getCurrentCDIRate() {
        log.debug("GET request to retrieve current CDI rate");
        BigDecimal cdiRate = marketDataService.getCurrentCDIRate();
        return ResponseEntity.ok(cdiRate);
    }

    /**
     * Gets current IPCA inflation rate.
     */
    @GetMapping("/indicators/ipca")
    @Operation(summary = "Get current IPCA", description = "Retrieves the current IPCA inflation rate from BCB")
    public ResponseEntity<BigDecimal> getCurrentIPCA() {
        log.debug("GET request to retrieve current IPCA");
        BigDecimal ipca = marketDataService.getCurrentIPCA();
        return ResponseEntity.ok(ipca);
    }

    /**
     * Gets all key economic indicators.
     */
    @GetMapping("/indicators")
    @Operation(summary = "Get key indicators", description = "Retrieves all key economic indicators (Selic, CDI, IPCA, etc.)")
    public ResponseEntity<List<MarketIndicator>> getKeyIndicators() {
        log.debug("GET request to retrieve key economic indicators");
        List<MarketIndicator> indicators = marketDataService.getKeyIndicators();
        return ResponseEntity.ok(indicators);
    }

    /**
     * Updates Selic rate from BCB.
     */
    @PostMapping("/indicators/selic/update")
    @Operation(summary = "Update Selic rate", description = "Fetches and updates the current Selic rate from BCB")
    public ResponseEntity<CompletableFuture<MarketIndicator>> updateSelicRate() {
        log.debug("POST request to update Selic rate");
        CompletableFuture<MarketIndicator> future = marketDataService.updateSelicRate();
        return ResponseEntity.ok(future);
    }

    /**
     * Updates CDI rate from BCB.
     */
    @PostMapping("/indicators/cdi/update")
    @Operation(summary = "Update CDI rate", description = "Fetches and updates the current CDI rate from BCB")
    public ResponseEntity<CompletableFuture<MarketIndicator>> updateCDIRate() {
        log.debug("POST request to update CDI rate");
        CompletableFuture<MarketIndicator> future = marketDataService.updateCDIRate();
        return ResponseEntity.ok(future);
    }

    /**
     * Updates IPCA from BCB.
     */
    @PostMapping("/indicators/ipca/update")
    @Operation(summary = "Update IPCA", description = "Fetches and updates the current IPCA from BCB")
    public ResponseEntity<CompletableFuture<MarketIndicator>> updateIPCA() {
        log.debug("POST request to update IPCA");
        CompletableFuture<MarketIndicator> future = marketDataService.updateIPCA();
        return ResponseEntity.ok(future);
    }

    /**
     * Gets all stocks for the authenticated user.
     */
    @GetMapping("/stocks")
    @Operation(summary = "Get user stocks", description = "Retrieves all stocks tracked by the authenticated user")
    public ResponseEntity<List<BrazilianStock>> getUserStocks(Authentication authentication) {
        log.debug("GET request to retrieve user stocks");
        Long userId = getUserIdFromAuthentication(authentication);
        List<BrazilianStock> stocks = marketDataService.getUserStocks(userId);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Gets all FIIs for the authenticated user.
     */
    @GetMapping("/fiis")
    @Operation(summary = "Get user FIIs", description = "Retrieves all FIIs tracked by the authenticated user")
    public ResponseEntity<List<FII>> getUserFIIs(Authentication authentication) {
        log.debug("GET request to retrieve user FIIs");
        Long userId = getUserIdFromAuthentication(authentication);
        List<FII> fiis = marketDataService.getUserFIIs(userId);
        return ResponseEntity.ok(fiis);
    }

    /**
     * Searches stocks for the authenticated user.
     */
    @GetMapping("/stocks/search")
    @Operation(summary = "Search user stocks", description = "Searches stocks by ticker or company name for the authenticated user")
    public ResponseEntity<List<BrazilianStock>> searchUserStocks(
            @Parameter(description = "Search query (ticker or company name)") @RequestParam String query,
            Authentication authentication) {
        log.debug("GET request to search user stocks with query: {}", query);
        Long userId = getUserIdFromAuthentication(authentication);
        List<BrazilianStock> stocks = marketDataService.searchUserStocks(userId, query);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Searches FIIs for the authenticated user.
     */
    @GetMapping("/fiis/search")
    @Operation(summary = "Search user FIIs", description = "Searches FIIs by ticker or fund name for the authenticated user")
    public ResponseEntity<List<FII>> searchUserFIIs(
            @Parameter(description = "Search query (ticker or fund name)") @RequestParam String query,
            Authentication authentication) {
        log.debug("GET request to search user FIIs with query: {}", query);
        Long userId = getUserIdFromAuthentication(authentication);
        List<FII> fiis = marketDataService.searchUserFIIs(userId, query);
        return ResponseEntity.ok(fiis);
    }

    /**
     * Updates stock data for a specific ticker.
     */
    @PostMapping("/stocks/{ticker}/update")
    @Operation(summary = "Update stock data", description = "Fetches and updates real-time data for a specific stock")
    public ResponseEntity<CompletableFuture<BrazilianStock>> updateStockData(
            @Parameter(description = "Stock ticker symbol") @PathVariable String ticker,
            Authentication authentication) {
        log.debug("POST request to update stock data for ticker: {}", ticker);
        Long userId = getUserIdFromAuthentication(authentication);
        CompletableFuture<BrazilianStock> future = marketDataService.updateStockData(ticker, userId);
        return ResponseEntity.ok(future);
    }

    /**
     * Updates FII data for a specific ticker.
     */
    @PostMapping("/fiis/{ticker}/update")
    @Operation(summary = "Update FII data", description = "Fetches and updates real-time data for a specific FII")
    public ResponseEntity<CompletableFuture<FII>> updateFIIData(
            @Parameter(description = "FII ticker symbol") @PathVariable String ticker,
            Authentication authentication) {
        log.debug("POST request to update FII data for ticker: {}", ticker);
        Long userId = getUserIdFromAuthentication(authentication);
        CompletableFuture<FII> future = marketDataService.updateFIIData(ticker, userId);
        return ResponseEntity.ok(future);
    }

    /**
     * Gets market summary data.
     */
    @GetMapping("/summary")
    @Operation(summary = "Get market summary", description = "Retrieves overall market summary and statistics")
    public ResponseEntity<Object> getMarketSummary() {
        log.debug("GET request to retrieve market summary");
        Object summary = marketDataService.getMarketSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Updates all key indicators.
     */
    @PostMapping("/indicators/update-all")
    @Operation(summary = "Update all indicators", description = "Fetches and updates all key economic indicators from BCB")
    public ResponseEntity<Map<String, CompletableFuture<MarketIndicator>>> updateAllIndicators() {
        log.debug("POST request to update all indicators");

        Map<String, CompletableFuture<MarketIndicator>> futures = Map.of(
            "selic", marketDataService.updateSelicRate(),
            "cdi", marketDataService.updateCDIRate(),
            "ipca", marketDataService.updateIPCA()
        );

        return ResponseEntity.ok(futures);
    }

    /**
     * Helper method to extract user ID from authentication.
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("User not authenticated");
        }

        // Assuming the principal contains user ID - adjust based on your authentication setup
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID in authentication");
        }
    }
}
