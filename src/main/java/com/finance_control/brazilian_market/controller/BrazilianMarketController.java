package com.finance_control.brazilian_market.controller;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.brazilian_market.service.BrazilianMarketDataService;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
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
@RequestMapping("/brazilian-market")
@Tag(name = "Brazilian Market", description = "Endpoints for Brazilian market data including stocks, FIIs, and economic indicators")
@Slf4j
public class BrazilianMarketController {

    private final BrazilianMarketDataService marketDataService;
    private final InvestmentService investmentService;
    private final UserRepository userRepository;

    public BrazilianMarketController(BrazilianMarketDataService marketDataService, InvestmentService investmentService, UserRepository userRepository) {
        this.marketDataService = marketDataService;
        this.investmentService = investmentService;
        this.userRepository = userRepository;
    }

    @GetMapping("/indicators/selic")
    @Operation(summary = "Get current Selic rate", description = "Retrieves the current Selic interest rate from BCB")
    public ResponseEntity<BigDecimal> getCurrentSelicRate() {
        log.debug("GET request to retrieve current Selic rate");
        BigDecimal selicRate = marketDataService.getCurrentSelicRate();
        return ResponseEntity.ok(selicRate);
    }

    @GetMapping("/indicators/cdi")
    @Operation(summary = "Get current CDI rate", description = "Retrieves the current CDI interest rate from BCB")
    public ResponseEntity<BigDecimal> getCurrentCDIRate() {
        log.debug("GET request to retrieve current CDI rate");
        BigDecimal cdiRate = marketDataService.getCurrentCDIRate();
        return ResponseEntity.ok(cdiRate);
    }

    @GetMapping("/indicators/ipca")
    @Operation(summary = "Get current IPCA", description = "Retrieves the current IPCA inflation rate from BCB")
    public ResponseEntity<BigDecimal> getCurrentIPCA() {
        log.debug("GET request to retrieve current IPCA");
        BigDecimal ipca = marketDataService.getCurrentIPCA();
        return ResponseEntity.ok(ipca);
    }

    @GetMapping("/indicators")
    @Operation(summary = "Get key indicators", description = "Retrieves all key economic indicators (Selic, CDI, IPCA, etc.)")
    public ResponseEntity<List<MarketIndicator>> getKeyIndicators() {
        log.debug("GET request to retrieve key economic indicators");
        List<MarketIndicator> indicators = marketDataService.getKeyIndicators();
        return ResponseEntity.ok(indicators);
    }

    @PostMapping("/indicators/selic/update")
    @Operation(summary = "Update Selic rate", description = "Fetches and updates the current Selic rate from BCB")
    public ResponseEntity<CompletableFuture<MarketIndicator>> updateSelicRate() {
        log.debug("POST request to update Selic rate");
        CompletableFuture<MarketIndicator> future = marketDataService.updateSelicRate();
        return ResponseEntity.ok(future);
    }

    @PostMapping("/indicators/cdi/update")
    @Operation(summary = "Update CDI rate", description = "Fetches and updates the current CDI rate from BCB")
    public ResponseEntity<CompletableFuture<MarketIndicator>> updateCDIRate() {
        log.debug("POST request to update CDI rate");
        CompletableFuture<MarketIndicator> future = marketDataService.updateCDIRate();
        return ResponseEntity.ok(future);
    }

    @PostMapping("/indicators/ipca/update")
    @Operation(summary = "Update IPCA", description = "Fetches and updates the current IPCA from BCB")
    public ResponseEntity<CompletableFuture<MarketIndicator>> updateIPCA() {
        log.debug("POST request to update IPCA");
        CompletableFuture<MarketIndicator> future = marketDataService.updateIPCA();
        return ResponseEntity.ok(future);
    }

    @GetMapping("/investments")
    @Operation(summary = "Get user investments", description = "Retrieves all investments tracked by the authenticated user")
    public ResponseEntity<List<Investment>> getUserInvestments(Authentication authentication) {
        log.debug("GET request to retrieve user investments");
        List<Investment> investments = investmentService.getAllInvestments(getUserFromAuthentication(authentication));
        return ResponseEntity.ok(investments);
    }

    @GetMapping("/investments/stocks")
    @Operation(summary = "Get user stocks", description = "Retrieves all stocks tracked by the authenticated user")
    public ResponseEntity<List<Investment>> getUserStocks(Authentication authentication) {
        log.debug("GET request to retrieve user stocks");
        List<Investment> stocks = investmentService.getInvestmentsByType(getUserFromAuthentication(authentication), Investment.InvestmentType.STOCK);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/investments/fiis")
    @Operation(summary = "Get user FIIs", description = "Retrieves all FIIs tracked by the authenticated user")
    public ResponseEntity<List<Investment>> getUserFIIs(Authentication authentication) {
        log.debug("GET request to retrieve user FIIs");
        List<Investment> fiis = investmentService.getInvestmentsByType(getUserFromAuthentication(authentication), Investment.InvestmentType.FII);
        return ResponseEntity.ok(fiis);
    }

    @GetMapping("/investments/search")
    @Operation(summary = "Search user investments", description = "Searches investments by ticker or name for the authenticated user")
    public ResponseEntity<List<Investment>> searchUserInvestments(
            @Parameter(description = "Search query (ticker or name)") @RequestParam String query,
            Authentication authentication) {
        log.debug("GET request to search user investments (query length: {})", query != null ? query.length() : 0);
        List<Investment> investments = investmentService.searchInvestments(getUserFromAuthentication(authentication), query);
        return ResponseEntity.ok(investments);
    }

    @PostMapping("/investments/{ticker}/update")
    @Operation(summary = "Update investment data", description = "Fetches and updates real-time data for a specific investment")
    public ResponseEntity<Investment> updateInvestmentData(
            @Parameter(description = "Investment ticker symbol") @PathVariable String ticker,
            Authentication authentication) {
        log.debug("POST request to update investment data (ticker length: {})", ticker != null ? ticker.length() : 0);
        Investment investment = investmentService.getInvestmentByTicker(ticker, getUserFromAuthentication(authentication))
                .orElseThrow(() -> new IllegalArgumentException("Investment not found: " + ticker));
        Investment updatedInvestment = investmentService.updateMarketData(investment);
        return ResponseEntity.ok(updatedInvestment);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get market summary", description = "Retrieves overall market summary and statistics")
    public ResponseEntity<Object> getMarketSummary() {
        log.debug("GET request to retrieve market summary");
        Object summary = marketDataService.getMarketSummary();
        return ResponseEntity.ok(summary);
    }

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

    private User getUserFromAuthentication(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
