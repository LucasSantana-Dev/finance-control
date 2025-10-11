package com.finance_control.brazilian_market.controller;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.service.ExternalMarketDataService;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.finance_control.users.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing investments.
 * Provides endpoints for CRUD operations and market data management.
 */
@RestController
@RequestMapping("/api/investments")
@Tag(name = "Investments", description = "Investment management endpoints")
@RequiredArgsConstructor
@Slf4j
public class InvestmentController {

    private final InvestmentService investmentService;
    private final ExternalMarketDataService externalMarketDataService;

    /**
     * Create a new investment
     */
    @PostMapping
    @Operation(summary = "Create a new investment", description = "Add a new investment to the user's portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Investment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid investment data"),
            @ApiResponse(responseCode = "409", description = "Investment with this ticker already exists")
    })
    public ResponseEntity<Investment> createInvestment(
            @Valid @RequestBody Investment investment,
            @AuthenticationPrincipal User user) {

        log.debug("Creating investment: {} for user: {}", investment.getTicker(), user.getId());

        if (investmentService.investmentExists(investment.getTicker(), user)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Investment createdInvestment = investmentService.createInvestment(investment, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInvestment);
    }

    /**
     * Get all investments for the authenticated user
     */
    @GetMapping
    @Operation(summary = "Get all investments", description = "Retrieve all investments for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Investments retrieved successfully")
    })
    public ResponseEntity<Page<Investment>> getAllInvestments(
            @AuthenticationPrincipal User user,
            Pageable pageable) {

        log.debug("Getting all investments for user: {}", user.getId());
        Page<Investment> investments = investmentService.getAllInvestments(user, pageable);
        return ResponseEntity.ok(investments);
    }

    /**
     * Get investment by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get investment by ID", description = "Retrieve a specific investment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Investment retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Investment not found")
    })
    public ResponseEntity<Investment> getInvestmentById(
            @Parameter(description = "Investment ID") @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        log.debug("Getting investment: {} for user: {}", id, user.getId());
        Optional<Investment> investment = investmentService.getInvestmentById(id, user);
        return investment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get investment by ticker
     */
    @GetMapping("/ticker/{ticker}")
    @Operation(summary = "Get investment by ticker", description = "Retrieve a specific investment by its ticker symbol")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Investment retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Investment not found")
    })
    public ResponseEntity<Investment> getInvestmentByTicker(
            @Parameter(description = "Investment ticker symbol") @PathVariable String ticker,
            @AuthenticationPrincipal User user) {

        log.debug("Getting investment by ticker: {} for user: {}", ticker, user.getId());
        Optional<Investment> investment = investmentService.getInvestmentByTicker(ticker, user);
        return investment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an investment
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an investment", description = "Update an existing investment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Investment updated successfully"),
            @ApiResponse(responseCode = "404", description = "Investment not found"),
            @ApiResponse(responseCode = "400", description = "Invalid investment data")
    })
    public ResponseEntity<Investment> updateInvestment(
            @Parameter(description = "Investment ID") @PathVariable Long id,
            @Valid @RequestBody Investment investment,
            @AuthenticationPrincipal User user) {

        log.debug("Updating investment: {} for user: {}", id, user.getId());

        try {
            Investment updatedInvestment = investmentService.updateInvestment(id, investment, user);
            return ResponseEntity.ok(updatedInvestment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete an investment
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an investment", description = "Delete an investment (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Investment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Investment not found")
    })
    public ResponseEntity<Void> deleteInvestment(
            @Parameter(description = "Investment ID") @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        log.debug("Deleting investment: {} for user: {}", id, user.getId());

        try {
            investmentService.deleteInvestment(id, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get investments by type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get investments by type", description = "Retrieve investments filtered by investment type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Investments retrieved successfully")
    })
    public ResponseEntity<List<Investment>> getInvestmentsByType(
            @Parameter(description = "Investment type") @PathVariable Investment.InvestmentType type,
            @AuthenticationPrincipal User user) {

        log.debug("Getting investments by type: {} for user: {}", type, user.getId());
        List<Investment> investments = investmentService.getInvestmentsByType(user, type);
        return ResponseEntity.ok(investments);
    }

    /**
     * Get investments by type and subtype
     */
    @GetMapping("/type/{type}/subtype/{subtype}")
    @Operation(summary = "Get investments by type and subtype", description = "Retrieve investments filtered by investment type and subtype")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Investments retrieved successfully")
    })
    public ResponseEntity<List<Investment>> getInvestmentsByTypeAndSubtype(
            @Parameter(description = "Investment type") @PathVariable Investment.InvestmentType type,
            @Parameter(description = "Investment subtype") @PathVariable Investment.InvestmentSubtype subtype,
            @AuthenticationPrincipal User user) {

        log.debug("Getting investments by type: {} and subtype: {} for user: {}", type, subtype, user.getId());
        List<Investment> investments = investmentService.getInvestmentsByTypeAndSubtype(user, type, subtype);
        return ResponseEntity.ok(investments);
    }

    /**
     * Search investments
     */
    @GetMapping("/search")
    @Operation(summary = "Search investments", description = "Search investments by name or ticker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    public ResponseEntity<List<Investment>> searchInvestments(
            @Parameter(description = "Search term") @RequestParam String q,
            @AuthenticationPrincipal User user) {

        log.debug("Searching investments with term: {} for user: {}", q, user.getId());
        List<Investment> investments = investmentService.searchInvestments(user, q);
        return ResponseEntity.ok(investments);
    }

    /**
     * Update market data for a specific investment
     */
    @PostMapping("/{id}/update-market-data")
    @Operation(summary = "Update market data", description = "Update market data for a specific investment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Market data updated successfully"),
            @ApiResponse(responseCode = "404", description = "Investment not found")
    })
    public ResponseEntity<Investment> updateMarketData(
            @Parameter(description = "Investment ID") @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        log.debug("Updating market data for investment: {} for user: {}", id, user.getId());

        Optional<Investment> investmentOpt = investmentService.getInvestmentById(id, user);
        if (investmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Investment updatedInvestment = investmentService.updateMarketData(investmentOpt.get());
        return ResponseEntity.ok(updatedInvestment);
    }

    /**
     * Update market data for all investments
     */
    @PostMapping("/update-all-market-data")
    @Operation(summary = "Update all market data", description = "Update market data for all investments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Market data update initiated")
    })
    public ResponseEntity<Map<String, String>> updateAllMarketData(@AuthenticationPrincipal User user) {
        log.debug("Updating all market data for user: {}", user.getId());

        // Run in background to avoid timeout
        new Thread(() -> investmentService.updateAllMarketData(user)).start();

        return ResponseEntity.ok(Map.of("message", "Market data update initiated"));
    }

    /**
     * Get investment metadata (sectors, industries, types, etc.)
     */
    @GetMapping("/metadata")
    @Operation(summary = "Get investment metadata", description = "Get available sectors, industries, types, and other metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getMetadata(@AuthenticationPrincipal User user) {
        log.debug("Getting investment metadata for user: {}", user.getId());

        Map<String, Object> metadata = Map.of(
                "sectors", investmentService.getSectors(user),
                "industries", investmentService.getIndustries(user),
                "investmentTypes", investmentService.getInvestmentTypes(user),
                "supportedExchanges", externalMarketDataService.getSupportedExchanges(),
                "supportedCurrencies", externalMarketDataService.getSupportedCurrencies()
        );

        return ResponseEntity.ok(metadata);
    }

    /**
     * Get top performing investments
     */
    @GetMapping("/top-performers")
    @Operation(summary = "Get top performing investments", description = "Get investments with highest day change")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top performers retrieved successfully")
    })
    public ResponseEntity<List<Investment>> getTopPerformers(
            @AuthenticationPrincipal User user,
            Pageable pageable) {

        log.debug("Getting top performing investments for user: {}", user.getId());
        List<Investment> investments = investmentService.getTopPerformers(user, pageable);
        return ResponseEntity.ok(investments);
    }

    /**
     * Get worst performing investments
     */
    @GetMapping("/worst-performers")
    @Operation(summary = "Get worst performing investments", description = "Get investments with lowest day change")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worst performers retrieved successfully")
    })
    public ResponseEntity<List<Investment>> getWorstPerformers(
            @AuthenticationPrincipal User user,
            Pageable pageable) {

        log.debug("Getting worst performing investments for user: {}", user.getId());
        List<Investment> investments = investmentService.getWorstPerformers(user, pageable);
        return ResponseEntity.ok(investments);
    }

    /**
     * Get investments with highest dividend yield
     */
    @GetMapping("/top-dividend-yield")
    @Operation(summary = "Get top dividend yield investments", description = "Get investments with highest dividend yield")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top dividend yield investments retrieved successfully")
    })
    public ResponseEntity<List<Investment>> getTopDividendYield(
            @AuthenticationPrincipal User user,
            Pageable pageable) {

        log.debug("Getting top dividend yield investments for user: {}", user.getId());
        List<Investment> investments = investmentService.getTopDividendYield(user, pageable);
        return ResponseEntity.ok(investments);
    }

    /**
     * Get portfolio summary
     */
    @GetMapping("/portfolio-summary")
    @Operation(summary = "Get portfolio summary", description = "Get portfolio summary with total market value and breakdown by type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio summary retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getPortfolioSummary(@AuthenticationPrincipal User user) {
        log.debug("Getting portfolio summary for user: {}", user.getId());

        Map<String, Object> summary = Map.of(
                "totalMarketValue", investmentService.getTotalMarketValue(user).orElse(0.0),
                "marketValueByType", investmentService.getMarketValueByType(user),
                "totalInvestments", investmentService.getAllInvestments(user).size()
        );

        return ResponseEntity.ok(summary);
    }
}
