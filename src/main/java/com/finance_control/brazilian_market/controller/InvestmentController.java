package com.finance_control.brazilian_market.controller;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.dto.InvestmentDTO;
import com.finance_control.brazilian_market.service.ExternalMarketDataService;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.finance_control.users.model.User;
import com.finance_control.shared.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.finance_control.shared.util.RangeUtils;

/**
 * REST controller for managing investments.
 * Provides endpoints for CRUD operations and market data management.
 */
@RestController
@RequestMapping("/investments")
@Tag(name = "Investments", description = "Investment management endpoints")
@Slf4j
public class InvestmentController {

    private final InvestmentService investmentService;
    private final ExternalMarketDataService externalMarketDataService;

    public InvestmentController(InvestmentService investmentService, ExternalMarketDataService externalMarketDataService) {
        this.investmentService = investmentService;
        this.externalMarketDataService = externalMarketDataService;
    }

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
    public ResponseEntity<InvestmentDTO> create(@Valid @RequestBody InvestmentDTO investmentDTO) {
        log.debug("Creating investment (ticker length: {}) for current user", investmentDTO.getTicker() != null ? investmentDTO.getTicker().length() : 0);

        // Get current user from context
        Long currentUserId = com.finance_control.shared.context.UserContext.getCurrentUserId();
        if (currentUserId == null) {
            log.error("User context not available");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = new User();
        user.setId(currentUserId);

        if (investmentService.investmentExists(investmentDTO.getTicker(), user)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Investment createdInvestment = investmentService.createInvestment(investmentDTO, user);
        InvestmentDTO responseDTO = investmentService.convertToResponseDTO(createdInvestment);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    /**
     * Get investments with filtering, sorting, pagination, or metadata
     */
    @GetMapping
    @Operation(summary = "Get investments with filtering",
               description = "Retrieve investments with flexible filtering, sorting, and pagination options, or metadata")
    public ResponseEntity<Object> getInvestments(
            @Parameter(description = "Investment type filter")
            @RequestParam(required = false) Investment.InvestmentType type,
            @Parameter(description = "Investment subtype filter")
            @RequestParam(required = false) Investment.InvestmentSubtype subtype,
            @Parameter(description = "Sector filter")
            @RequestParam(required = false) String sector,
            @Parameter(description = "Industry filter")
            @RequestParam(required = false) String industry,
            @Parameter(description = "Exchange filter")
            @RequestParam(required = false) String exchange,
            @Parameter(description = "Search term for ticker or name")
            @RequestParam(required = false) String search,
            @Parameter(description = "Sort field")
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Minimum current price filter")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum current price filter")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Minimum dividend yield filter")
            @RequestParam(required = false) BigDecimal minDividendYield,
            @Parameter(description = "Maximum dividend yield filter")
            @RequestParam(required = false) BigDecimal maxDividendYield,
            @Parameter(description = "Type of data to retrieve (metadata types: sectors, industries, " +
                    "types, subtypes, exchanges, top-performers, worst-performers, " +
                    "top-dividend-yield, portfolio-summary)")
            @RequestParam(required = false) String data,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        log.debug("GET request to retrieve investments with filtering (user present: {})", user != null);

        // If data parameter is provided, return metadata
        if (data != null && !data.trim().isEmpty()) {
            return switch (data) {
                case "sectors" -> ResponseEntity.ok(investmentService.getSectors(user));
                case "industries" -> ResponseEntity.ok(investmentService.getIndustries(user));
                case "types" -> ResponseEntity.ok(investmentService.getInvestmentTypes(user));
                case "subtypes" -> {
                    if (type == null) {
                        throw new IllegalArgumentException("Investment type is required for subtypes data");
                    }
                    yield ResponseEntity.ok(investmentService.getInvestmentSubtypes(user, type));
                }
                case "exchanges" -> ResponseEntity.ok(externalMarketDataService.getSupportedExchanges());
                case "top-performers" -> {
                    Pageable pageable = PageRequest.of(page, size);
                    List<Investment> topPerformers = investmentService.getTopPerformers(user, pageable);
                    yield ResponseEntity.ok(topPerformers.stream().map(investmentService::convertToResponseDTO).toList());
                }
                case "worst-performers" -> {
                    Pageable pageable = PageRequest.of(page, size);
                    List<Investment> worstPerformers = investmentService.getWorstPerformers(user, pageable);
                    yield ResponseEntity.ok(worstPerformers.stream().map(investmentService::convertToResponseDTO).toList());
                }
                case "top-dividend-yield" -> {
                    Pageable pageable = PageRequest.of(page, size);
                    List<Investment> topDividendYield = investmentService.getTopDividendYield(user, pageable);
                    yield ResponseEntity.ok(topDividendYield.stream().map(investmentService::convertToResponseDTO).toList());
                }
                case "portfolio-summary" -> {
                    Map<String, Object> summary = Map.of(
                            "totalMarketValue", investmentService.getTotalMarketValue(user).orElse(0.0),
                            "marketValueByType", investmentService.getMarketValueByType(user),
                            "totalInvestments", investmentService.getAllInvestments(user).size()
                    );
                    yield ResponseEntity.ok(summary);
                }
                default -> throw new IllegalArgumentException("Invalid data type: " + data);
            };
        }

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Apply filters based on parameters
        List<Investment> investments = getInvestmentsByFilters(user, search, type, subtype, sector, industry);

        // Apply additional filters
        investments = applyPriceAndDividendFilters(investments, minPrice, maxPrice, minDividendYield, maxDividendYield);

        // Convert to DTOs and create pagination manually
        List<InvestmentDTO> investmentDTOs = investments.stream()
                .map(investmentService::convertToResponseDTO)
                .toList();

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), investmentDTOs.size());
        List<InvestmentDTO> pagedDTOs = investmentDTOs.subList(start, end);

        // Create a custom page response
        Page<InvestmentDTO> result = new org.springframework.data.domain.PageImpl<>(
                pagedDTOs, pageable, investmentDTOs.size());

        return ResponseEntity.ok(result);
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
    public ResponseEntity<InvestmentDTO> findById(@PathVariable Long id) {
        // SuppressFBWarnings: LoggingUtils.sanitizeForLogging prevents CRLF injection by replacing CRLF chars with underscores
        log.debug("Getting investment (ID length: {}) for current user", String.valueOf(id).length());

        // Get current user from context
        Long currentUserId = com.finance_control.shared.context.UserContext.getCurrentUserId();
        if (currentUserId == null) {
            log.error("User context not available");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = new User();
        user.setId(currentUserId);

        Optional<Investment> investment = investmentService.getInvestmentById(id, user);
        return investment.map(inv -> ResponseEntity.ok(investmentService.convertToResponseDTO(inv)))
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
    public ResponseEntity<InvestmentDTO> findByTicker(@PathVariable String ticker) {
        log.debug("Getting investment by ticker (ticker length: {}) for current user", ticker != null ? ticker.length() : 0);

        // Get current user from context
        Long currentUserId = com.finance_control.shared.context.UserContext.getCurrentUserId();
        if (currentUserId == null) {
            log.error("User context not available");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Create a User object with just the ID for the service call
        User user = new User();
        user.setId(currentUserId);

        Optional<Investment> investment = investmentService.getInvestmentByTicker(ticker, user);
        return investment.map(inv -> ResponseEntity.ok(investmentService.convertToResponseDTO(inv)))
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
    public ResponseEntity<InvestmentDTO> updateInvestment(
            @Parameter(description = "Investment ID") @PathVariable Long id,
            @Valid @RequestBody InvestmentDTO investmentDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        log.debug("Updating investment (ID length: {}, user present: {})", String.valueOf(id).length(), user != null);

        try {
            Investment updatedInvestment = investmentService.updateInvestment(id, investmentDTO, user);
            InvestmentDTO responseDTO = investmentService.convertToResponseDTO(updatedInvestment);
            return ResponseEntity.ok(responseDTO);
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Deleting investment (ID length: {}) for current user", String.valueOf(id).length());

        // Get current user from context
        Long currentUserId = com.finance_control.shared.context.UserContext.getCurrentUserId();
        if (currentUserId == null) {
            log.error("User context not available");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = new User();
        user.setId(currentUserId);

        try {
            investmentService.deleteInvestment(id, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
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
    public ResponseEntity<InvestmentDTO> updateMarketData(
            @Parameter(description = "Investment ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        log.debug("Updating market data for investment (ID length: {}, user present: {})", String.valueOf(id).length(), user != null);

        Optional<Investment> investmentOpt = investmentService.getInvestmentById(id, user);
        if (investmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Investment updatedInvestment = investmentService.updateMarketData(investmentOpt.get());
        InvestmentDTO responseDTO = investmentService.convertToResponseDTO(updatedInvestment);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Update market data for all investments
     */
    @PostMapping("/update-all-market-data")
    @Operation(summary = "Update all market data", description = "Update market data for all investments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Market data update initiated")
    })
    public ResponseEntity<Map<String, String>> updateAllMarketData(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        log.debug("Updating all market data (user present: {})", user != null);

        // Run in background to avoid timeout
        new Thread(() -> investmentService.updateAllMarketData(user)).start();

        return ResponseEntity.ok(Map.of("message", "Market data update initiated"));
    }

    /**
     * Gets investments based on the provided filters.
     *
     * @param user the user
     * @param search search term
     * @param type investment type
     * @param subtype investment subtype
     * @param sector sector filter
     * @param industry industry filter
     * @return list of investments matching the filters
     */
    private List<Investment> getInvestmentsByFilters(User user, String search, Investment.InvestmentType type,
                                                   Investment.InvestmentSubtype subtype, String sector, String industry) {
        if (search != null && !search.trim().isEmpty()) {
            return investmentService.searchInvestments(user, search);
        }

        if (type != null && subtype != null) {
            return investmentService.getInvestmentsByTypeAndSubtype(user, type, subtype);
        }

        if (type != null) {
            return investmentService.getInvestmentsByType(user, type);
        }

        if (sector != null && !sector.trim().isEmpty()) {
            return investmentService.getInvestmentsBySector(user, sector);
        }

        if (industry != null && !industry.trim().isEmpty()) {
            return investmentService.getInvestmentsByIndustry(user, industry);
        }

        return investmentService.getAllInvestments(user);
    }

    /**
     * Applies price and dividend yield filters to the investment list.
     *
     * @param investments the list of investments to filter
     * @param minPrice minimum price filter
     * @param maxPrice maximum price filter
     * @param minDividendYield minimum dividend yield filter
     * @param maxDividendYield maximum dividend yield filter
     * @return filtered list of investments
     */
    private List<Investment> applyPriceAndDividendFilters(List<Investment> investments,
                                                         BigDecimal minPrice,
                                                         BigDecimal maxPrice,
                                                         BigDecimal minDividendYield,
                                                         BigDecimal maxDividendYield) {
        if (minPrice == null && maxPrice == null && minDividendYield == null && maxDividendYield == null) {
            return investments;
        }

        return investments.stream()
                .filter(inv -> isPriceInRange(inv.getCurrentPrice(), minPrice, maxPrice))
                .filter(inv -> isDividendYieldInRange(inv.getDividendYield(), minDividendYield, maxDividendYield))
                .toList();
    }

    /**
     * Checks if the investment price is within the specified range.
     */
    private boolean isPriceInRange(BigDecimal currentPrice, BigDecimal minPrice, BigDecimal maxPrice) {
        return RangeUtils.isInRange(currentPrice, minPrice, maxPrice);
    }

    /**
     * Checks if the investment dividend yield is within the specified range.
     */
    private boolean isDividendYieldInRange(BigDecimal dividendYield, BigDecimal minDividendYield, BigDecimal maxDividendYield) {
        return RangeUtils.isInRange(dividendYield, minDividendYield, maxDividendYield);
    }
}
