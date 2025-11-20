package com.finance_control.unit.brazilian_market.controller;

import com.finance_control.brazilian_market.dto.InvestmentDTO;
import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.model.InvestmentType;
import com.finance_control.brazilian_market.model.InvestmentSubtype;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.finance_control.users.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Test-specific controller that doesn't use @AuthenticationPrincipal for unit testing.
 * This allows unit tests to work without Spring Security context.
 */
@RestController
@RequestMapping("/investments")
@Profile("test")
public class TestInvestmentController {

    private final InvestmentService investmentService;

    public TestInvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @PostMapping
    public ResponseEntity<InvestmentDTO> createInvestment(
            @RequestBody InvestmentDTO investmentDTO,
            @RequestParam Long userId) {

        User user = new User();
        user.setId(userId);

        if (investmentService.investmentExists(investmentDTO.getTicker(), user)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Investment createdInvestment = investmentService.createInvestment(investmentDTO, user);
        InvestmentDTO responseDTO = convertToDTO(createdInvestment);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    /**
     * Unified endpoint for getting investments with various sorting and filtering options
     * Query parameters:
     * - sort: performance, dividend-yield, or default (all)
     * - order: top, worst (for performance sorting)
     * - type: investment type filter
     * - subtype: investment subtype filter
     * - sector: sector filter
     * - industry: industry filter
     * - search: search term
     *
     * Examples:
     * - GET /investments?userId=1&sort=performance&order=top
     * - GET /investments?userId=1&sort=dividend-yield
     * - GET /investments?userId=1&type=STOCK
     * - GET /investments?userId=1&search=PETR4
     */
    @GetMapping
    public ResponseEntity<List<InvestmentDTO>> getAllInvestments(
            @RequestParam Long userId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) InvestmentType type,
            @RequestParam(required = false) InvestmentSubtype subtype,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        User user = new User();
        user.setId(userId);

        List<Investment> investments;

        // Handle different sorting options
        if ("performance".equals(sort)) {
            if ("worst".equals(order)) {
                investments = investmentService.getWorstPerformers(user, pageable);
            } else {
                // Default to top performers
                investments = investmentService.getTopPerformers(user, pageable);
            }
        } else if ("dividend-yield".equals(sort)) {
            investments = investmentService.getTopDividendYield(user, pageable);
        } else if (type != null && subtype != null) {
            investments = investmentService.getInvestmentsByTypeAndSubtype(user, type, subtype);
        } else if (type != null) {
            investments = investmentService.getInvestmentsByType(user, type);
        } else if (sector != null) {
            investments = investmentService.getInvestmentsBySector(user, sector);
        } else if (industry != null) {
            investments = investmentService.getInvestmentsByIndustry(user, industry);
        } else if (search != null) {
            investments = investmentService.searchInvestments(user, search);
        } else {
            // Default: get all investments
            investments = investmentService.getAllInvestments(user);
        }

        List<InvestmentDTO> investmentDTOs = investments.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(investmentDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentDTO> getInvestmentById(
            @PathVariable Long id,
            @RequestParam Long userId) {

        User user = new User();
        user.setId(userId);

        Optional<Investment> investment = investmentService.getInvestmentById(id, user);
        return investment.map(inv -> ResponseEntity.ok(convertToDTO(inv)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ticker/{ticker}")
    public ResponseEntity<InvestmentDTO> getInvestmentByTicker(
            @PathVariable String ticker,
            @RequestParam Long userId) {

        User user = new User();
        user.setId(userId);

        Optional<Investment> investment = investmentService.getInvestmentByTicker(ticker, user);
        return investment.map(inv -> ResponseEntity.ok(convertToDTO(inv)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentDTO> updateInvestment(
            @PathVariable Long id,
            @RequestBody InvestmentDTO investmentDTO,
            @RequestParam Long userId) {

        User user = new User();
        user.setId(userId);

        try {
            Investment updatedInvestment = investmentService.updateInvestment(id, investmentDTO, user);
            InvestmentDTO responseDTO = convertToDTO(updatedInvestment);
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvestment(
            @PathVariable Long id,
            @RequestParam Long userId) {

        User user = new User();
        user.setId(userId);

        try {
            investmentService.deleteInvestment(id, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<InvestmentDTO>> getInvestmentsByType(
            @PathVariable InvestmentType type,
            @RequestParam Long userId) {

        User user = new User();
        user.setId(userId);

        List<Investment> investments = investmentService.getInvestmentsByType(user, type);
        List<InvestmentDTO> investmentDTOs = investments.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(investmentDTOs);
    }

    @GetMapping("/type/{type}/subtype/{subtype}")
    public ResponseEntity<List<InvestmentDTO>> getInvestmentsByTypeAndSubtype(
            @PathVariable InvestmentType type,
            @PathVariable InvestmentSubtype subtype,
            @RequestParam Long userId) {

        User user = new User();
        user.setId(userId);

        List<Investment> investments = investmentService.getInvestmentsByTypeAndSubtype(user, type, subtype);
        List<InvestmentDTO> investmentDTOs = investments.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(investmentDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<InvestmentDTO>> searchInvestments(
            @RequestParam String q,
            @RequestParam Long userId) {

        User user = new User();
        user.setId(userId);

        List<Investment> investments = investmentService.searchInvestments(user, q);
        List<InvestmentDTO> investmentDTOs = investments.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(investmentDTOs);
    }

    @PostMapping("/{id}/update-market-data")
    public ResponseEntity<InvestmentDTO> updateMarketData(
            @PathVariable Long id,
            @RequestParam Long userId) {

        User user = new User();
        user.setId(userId);

        Optional<Investment> investmentOpt = investmentService.getInvestmentById(id, user);
        if (investmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Investment updatedInvestment = investmentService.updateMarketData(investmentOpt.get());
        InvestmentDTO responseDTO = convertToDTO(updatedInvestment);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/performance/top")
    public ResponseEntity<List<InvestmentDTO>> getTopPerformers(
            @RequestParam Long userId,
            Pageable pageable) {

        User user = new User();
        user.setId(userId);

        List<Investment> investments = investmentService.getTopPerformers(user, pageable);
        List<InvestmentDTO> investmentDTOs = investments.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(investmentDTOs);
    }

    @GetMapping("/performance/worst")
    public ResponseEntity<List<InvestmentDTO>> getWorstPerformers(
            @RequestParam Long userId,
            Pageable pageable) {

        User user = new User();
        user.setId(userId);

        List<Investment> investments = investmentService.getWorstPerformers(user, pageable);
        List<InvestmentDTO> investmentDTOs = investments.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(investmentDTOs);
    }

    @GetMapping("/dividend-yield/top")
    public ResponseEntity<List<InvestmentDTO>> getTopDividendYield(
            @RequestParam Long userId,
            Pageable pageable) {

        User user = new User();
        user.setId(userId);

        List<Investment> investments = investmentService.getTopDividendYield(user, pageable);
        List<InvestmentDTO> investmentDTOs = investments.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(investmentDTOs);
    }

    @GetMapping("/total-market-value")
    public ResponseEntity<Double> getTotalMarketValue(@RequestParam Long userId) {
        User user = new User();
        user.setId(userId);
        return ResponseEntity.ok(investmentService.getTotalMarketValue(user).orElse(0.0));
    }

    /**
     * Unified endpoint for getting investment metadata and analytics
     * Query parameters:
     * - data: sectors, industries, types, subtypes, market-value-by-type, total-market-value
     * - type: required for subtypes endpoint
     *
     * Examples:
     * - GET /investments/metadata?userId=1&data=sectors
     * - GET /investments/metadata?userId=1&data=types
     * - GET /investments/metadata?userId=1&data=subtypes&type=STOCK
     * - GET /investments/metadata?userId=1&data=market-value-by-type
     * - GET /investments/metadata?userId=1&data=total-market-value
     */
    @GetMapping("/metadata")
    public ResponseEntity<?> getInvestmentMetadata(
            @RequestParam Long userId,
            @RequestParam String data,
            @RequestParam(required = false) InvestmentType type) {

        User user = new User();
        user.setId(userId);

        switch (data) {
            case "sectors":
                return ResponseEntity.ok(investmentService.getSectors(user));
            case "industries":
                return ResponseEntity.ok(investmentService.getIndustries(user));
            case "types":
                return ResponseEntity.ok(investmentService.getInvestmentTypes(user));
            case "subtypes":
                if (type == null) {
                    return ResponseEntity.badRequest().body("Type parameter is required for subtypes");
                }
                return ResponseEntity.ok(investmentService.getInvestmentSubtypes(user, type));
            case "market-value-by-type":
                return ResponseEntity.ok(investmentService.getMarketValueByType(user));
            case "total-market-value":
                return ResponseEntity.ok(investmentService.getTotalMarketValue(user).orElse(0.0));
            default:
                return ResponseEntity.badRequest().body("Invalid data type: " + data);
        }
    }

    // Keep the individual endpoints for backward compatibility
    @GetMapping("/sectors")
    public ResponseEntity<List<String>> getSectors(@RequestParam Long userId) {
        User user = new User();
        user.setId(userId);
        return ResponseEntity.ok(investmentService.getSectors(user));
    }

    @GetMapping("/industries")
    public ResponseEntity<List<String>> getIndustries(@RequestParam Long userId) {
        User user = new User();
        user.setId(userId);
        return ResponseEntity.ok(investmentService.getIndustries(user));
    }

    @GetMapping("/types")
    public ResponseEntity<List<InvestmentType>> getInvestmentTypes(@RequestParam Long userId) {
        User user = new User();
        user.setId(userId);
        return ResponseEntity.ok(investmentService.getInvestmentTypes(user));
    }

    @GetMapping("/types/{type}/subtypes")
    public ResponseEntity<List<InvestmentSubtype>> getInvestmentSubtypes(
            @PathVariable InvestmentType type,
            @RequestParam Long userId) {
        User user = new User();
        user.setId(userId);
        return ResponseEntity.ok(investmentService.getInvestmentSubtypes(user, type));
    }

    @GetMapping("/market-value-by-type")
    public ResponseEntity<List<Object[]>> getMarketValueByType(@RequestParam Long userId) {
        User user = new User();
        user.setId(userId);
        return ResponseEntity.ok(investmentService.getMarketValueByType(user));
    }

    private InvestmentDTO convertToDTO(Investment investment) {
        InvestmentDTO dto = new InvestmentDTO();
        dto.setId(investment.getId());
        dto.setTicker(investment.getTicker());
        dto.setName(investment.getName());
        dto.setDescription(investment.getDescription());
        dto.setInvestmentType(investment.getInvestmentType());
        dto.setInvestmentSubtype(investment.getInvestmentSubtype());
        dto.setCurrentPrice(investment.getCurrentPrice());
        dto.setDayChangePercent(investment.getDayChangePercent());
        dto.setDividendYield(investment.getDividendYield());
        dto.setSector(investment.getSector());
        dto.setIndustry(investment.getIndustry());
        dto.setExchange(investment.getExchange());
        dto.setIsActive(investment.getIsActive());
        dto.setCreatedAt(investment.getCreatedAt());
        dto.setUpdatedAt(investment.getUpdatedAt());
        return dto;
    }
}
