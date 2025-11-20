package com.finance_control.unit.brazilian_market.controller;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.model.InvestmentType;
import com.finance_control.brazilian_market.model.InvestmentSubtype;
import com.finance_control.brazilian_market.service.ExternalMarketDataService;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Example test demonstrating the new unified endpoint approach
 * for InvestmentController with query parameters for sorting and filtering.
 */
@ExtendWith(MockitoExtension.class)
class InvestmentControllerExampleTest {

    private MockMvc mockMvc;

    @Mock
    private InvestmentService investmentService;

    @Mock
    private ExternalMarketDataService externalMarketDataService;

    @InjectMocks
    private TestInvestmentController investmentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(investmentController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getFilteredInvestments_WithPerformanceSort_ShouldReturnTopPerformers() throws Exception {
        // Given
        Investment testInvestment = createTestInvestment();
        when(investmentService.getTopPerformers(any(), any(Pageable.class)))
                .thenReturn(List.of(testInvestment));

        // When & Then - Using unified endpoint with query parameters
        mockMvc.perform(get("/investments")
                        .param("userId", "1")
                        .param("sort", "performance")
                        .param("order", "top")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));
    }

    @Test
    void getFilteredInvestments_WithDividendYieldSort_ShouldReturnTopDividendYield() throws Exception {
        // Given
        Investment testInvestment = createTestInvestment();
        when(investmentService.getTopDividendYield(any(), any(Pageable.class)))
                .thenReturn(List.of(testInvestment));

        // When & Then - Using unified endpoint for dividend yield sorting
        mockMvc.perform(get("/investments")
                        .param("userId", "1")
                        .param("sort", "dividend-yield"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));
    }

    @Test
    void getFilteredInvestments_WithTypeFilter_ShouldReturnFilteredByType() throws Exception {
        // Given
        Investment testInvestment = createTestInvestment();
        when(investmentService.getInvestmentsByType(any(), eq(InvestmentType.STOCK)))
                .thenReturn(List.of(testInvestment));

        // When & Then - Using unified endpoint for type filtering
        mockMvc.perform(get("/investments")
                        .param("userId", "1")
                        .param("type", "STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));
    }

    @Test
    void getFilteredInvestments_WithSearch_ShouldReturnSearchResults() throws Exception {
        // Given
        Investment testInvestment = createTestInvestment();
        when(investmentService.searchInvestments(any(), eq("PETR4")))
                .thenReturn(List.of(testInvestment));

        // When & Then - Using unified endpoint for search
        mockMvc.perform(get("/investments")
                        .param("userId", "1")
                        .param("search", "PETR4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));
    }

    @Test
    void getInvestmentMetadata_WithSectors_ShouldReturnSectors() throws Exception {
        // Given
        when(investmentService.getSectors(any()))
                .thenReturn(List.of("Energy", "Technology"));

        // When & Then - Using unified metadata endpoint
        mockMvc.perform(get("/investments/metadata")
                        .param("userId", "1")
                        .param("data", "sectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Energy"))
                .andExpect(jsonPath("$[1]").value("Technology"));
    }

    @Test
    void getInvestmentMetadata_WithTotalMarketValue_ShouldReturnTotalValue() throws Exception {
        // Given
        when(investmentService.getTotalMarketValue(any()))
                .thenReturn(Optional.of(10000.0));

        // When & Then - Using unified metadata endpoint for market value
        mockMvc.perform(get("/investments/metadata")
                        .param("userId", "1")
                        .param("data", "total-market-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(10000.0));
    }

    private Investment createTestInvestment() {
        Investment investment = new Investment();
        investment.setId(1L);
        investment.setTicker("PETR4");
        investment.setName("Petrobras");
        investment.setInvestmentType(InvestmentType.STOCK);
        investment.setInvestmentSubtype(InvestmentSubtype.ORDINARY);
        investment.setCurrentPrice(BigDecimal.valueOf(25.00));
        investment.setDayChangePercent(BigDecimal.valueOf(1.5));
        investment.setDividendYield(BigDecimal.valueOf(0.05));
        investment.setSector("Oil & Gas");
        investment.setIndustry("Exploration & Production");
        investment.setExchange("B3");
        investment.setIsActive(true);
        investment.setCreatedAt(LocalDateTime.now());
        investment.setUpdatedAt(LocalDateTime.now());
        return investment;
    }
}
