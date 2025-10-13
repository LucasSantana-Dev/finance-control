package com.finance_control.unit.brazilian_market.controller;

import com.finance_control.brazilian_market.controller.InvestmentController;
import com.finance_control.brazilian_market.dto.InvestmentDTO;
import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.service.ExternalMarketDataService;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.finance_control.users.model.User;
import com.finance_control.shared.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * Unit tests for unified InvestmentController endpoints.
 * Tests the unified GET /investments endpoint with filtering, sorting, pagination, and metadata.
 */
@ExtendWith(MockitoExtension.class)
class InvestmentControllerUnifiedTest {

    private MockMvc mockMvc;

    @Mock
    private InvestmentService investmentService;

    @Mock
    private ExternalMarketDataService externalMarketDataService;

    @InjectMocks
    private InvestmentController investmentController;

    private ObjectMapper objectMapper;

    private User testUser;
    private Investment testInvestment;
    private InvestmentDTO testInvestmentDTO;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(investmentController)
                .build();

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        // Create test user details
        testUserDetails = new CustomUserDetails(testUser);

        // Create test investment
        testInvestment = new Investment();
        testInvestment.setId(1L);
        testInvestment.setTicker("PETR4");
        testInvestment.setName("Petrobras");
        testInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        testInvestment.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        testInvestment.setCurrentPrice(BigDecimal.valueOf(26.00));
        testInvestment.setDayChangePercent(BigDecimal.valueOf(2.5));
        testInvestment.setDividendYield(BigDecimal.valueOf(8.5));
        testInvestment.setSector("Energy");
        testInvestment.setIndustry("Oil & Gas");
        testInvestment.setIsActive(true);
        testInvestment.setUser(testUser);
        testInvestment.setCreatedAt(LocalDateTime.now());
        testInvestment.setUpdatedAt(LocalDateTime.now());

        // Create test investment DTO
        testInvestmentDTO = new InvestmentDTO();
        testInvestmentDTO.setId(1L);
        testInvestmentDTO.setTicker("PETR4");
        testInvestmentDTO.setName("Petrobras");
        testInvestmentDTO.setInvestmentType(Investment.InvestmentType.STOCK);
        testInvestmentDTO.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        testInvestmentDTO.setCurrentPrice(BigDecimal.valueOf(26.00));
        testInvestmentDTO.setDayChangePercent(BigDecimal.valueOf(2.5));
        testInvestmentDTO.setDividendYield(BigDecimal.valueOf(8.5));
        testInvestmentDTO.setSector("Energy");
        testInvestmentDTO.setIndustry("Oil & Gas");
        testInvestmentDTO.setIsActive(true);
        testInvestmentDTO.setCreatedAt(LocalDateTime.now());
        testInvestmentDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getInvestments_WithValidParameters_ShouldReturnOk() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getAllInvestments(testUser)).thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("sortBy", "createdAt")
                .param("sortDirection", "desc")
                .param("page", "0")
                .param("size", "20")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$.content[0].name").value("Petrobras"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getInvestments_WithTypeFilter_ShouldReturnFilteredResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getInvestmentsByType(testUser, Investment.InvestmentType.STOCK))
                .thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("type", "STOCK")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].investmentType").value("STOCK"));
    }

    @Test
    void getInvestments_WithSearchTerm_ShouldReturnSearchResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.searchInvestments(testUser, "PETR4")).thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("search", "PETR4")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].ticker").value("PETR4"));
    }

    @Test
    void getInvestments_WithPriceRangeFilter_ShouldReturnFilteredResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getAllInvestments(testUser)).thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("minPrice", "20.00")
                .param("maxPrice", "30.00")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].currentPrice").value(26.00));
    }

    @Test
    void getInvestments_WithDividendYieldFilter_ShouldReturnFilteredResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getAllInvestments(testUser)).thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("minDividendYield", "5.0")
                .param("maxDividendYield", "10.0")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].dividendYield").value(8.5));
    }

    @Test
    void getInvestments_WithSectorsMetadata_ShouldReturnSectors() throws Exception {
        List<String> sectors = Arrays.asList("Energy", "Technology", "Finance");
        when(investmentService.getSectors(testUser)).thenReturn(sectors);

        mockMvc.perform(get("/investments")
                .param("data", "sectors")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Energy"))
                .andExpect(jsonPath("$[1]").value("Technology"))
                .andExpect(jsonPath("$[2]").value("Finance"));
    }

    @Test
    void getInvestments_WithIndustriesMetadata_ShouldReturnIndustries() throws Exception {
        List<String> industries = Arrays.asList("Oil & Gas", "Software", "Banking");
        when(investmentService.getIndustries(testUser)).thenReturn(industries);

        mockMvc.perform(get("/investments")
                .param("data", "industries")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Oil & Gas"))
                .andExpect(jsonPath("$[1]").value("Software"))
                .andExpect(jsonPath("$[2]").value("Banking"));
    }

    @Test
    void getInvestments_WithTypesMetadata_ShouldReturnTypes() throws Exception {
        List<Investment.InvestmentType> types = Arrays.asList(
                Investment.InvestmentType.STOCK,
                Investment.InvestmentType.FII,
                Investment.InvestmentType.BOND
        );
        when(investmentService.getInvestmentTypes(testUser)).thenReturn(types);

        mockMvc.perform(get("/investments")
                .param("data", "types")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("STOCK"))
                .andExpect(jsonPath("$[1]").value("FII"))
                .andExpect(jsonPath("$[2]").value("BOND"));
    }

    @Test
    void getInvestments_WithSubtypesMetadata_ShouldReturnSubtypes() throws Exception {
        List<Investment.InvestmentSubtype> subtypes = Arrays.asList(
                Investment.InvestmentSubtype.ORDINARY,
                Investment.InvestmentSubtype.PREFERRED
        );
        when(investmentService.getInvestmentSubtypes(testUser, Investment.InvestmentType.STOCK))
                .thenReturn(subtypes);

        mockMvc.perform(get("/investments")
                .param("data", "subtypes")
                .param("type", "STOCK")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("ORDINARY"))
                .andExpect(jsonPath("$[1]").value("PREFERRED"));
    }

    @Test
    void getInvestments_WithSubtypesMetadataWithoutType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/investments")
                .param("data", "subtypes")
                .with(user(testUserDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInvestments_WithExchangesMetadata_ShouldReturnExchanges() throws Exception {
        Map<String, String> exchanges = Map.of(
                "B3", "Brasil Bolsa Balcão (Brazil)",
                "NYSE", "New York Stock Exchange",
                "NASDAQ", "NASDAQ"
        );
        when(externalMarketDataService.getSupportedExchanges()).thenReturn(exchanges);

        mockMvc.perform(get("/investments")
                .param("data", "exchanges")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.B3").value("Brasil Bolsa Balcão (Brazil)"))
                .andExpect(jsonPath("$.NYSE").value("New York Stock Exchange"))
                .andExpect(jsonPath("$.NASDAQ").value("NASDAQ"));
    }

    @Test
    void getInvestments_WithTopPerformersMetadata_ShouldReturnTopPerformers() throws Exception {
        List<Investment> topPerformers = Arrays.asList(testInvestment);
        when(investmentService.getTopPerformers(eq(testUser), any(Pageable.class)))
                .thenReturn(topPerformers);

        mockMvc.perform(get("/investments")
                .param("data", "top-performers")
                .param("page", "0")
                .param("size", "10")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));
    }

    @Test
    void getInvestments_WithWorstPerformersMetadata_ShouldReturnWorstPerformers() throws Exception {
        List<Investment> worstPerformers = Arrays.asList(testInvestment);
        when(investmentService.getWorstPerformers(eq(testUser), any(Pageable.class)))
                .thenReturn(worstPerformers);

        mockMvc.perform(get("/investments")
                .param("data", "worst-performers")
                .param("page", "0")
                .param("size", "10")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));
    }

    @Test
    void getInvestments_WithTopDividendYieldMetadata_ShouldReturnTopDividendYield() throws Exception {
        List<Investment> topDividendYield = Arrays.asList(testInvestment);
        when(investmentService.getTopDividendYield(eq(testUser), any(Pageable.class)))
                .thenReturn(topDividendYield);

        mockMvc.perform(get("/investments")
                .param("data", "top-dividend-yield")
                .param("page", "0")
                .param("size", "10")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));
    }

    @Test
    void getInvestments_WithPortfolioSummaryMetadata_ShouldReturnPortfolioSummary() throws Exception {
        when(investmentService.getTotalMarketValue(testUser)).thenReturn(Optional.of(10000.0));
        List<Object[]> marketValueList = new java.util.ArrayList<>();
        marketValueList.add(new Object[]{"STOCK", 5000.0});
        when(investmentService.getMarketValueByType(testUser)).thenReturn(marketValueList);
        when(investmentService.getAllInvestments(testUser)).thenReturn(Arrays.asList(testInvestment));

        mockMvc.perform(get("/investments")
                .param("data", "portfolio-summary")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalMarketValue").value(10000.0))
                .andExpect(jsonPath("$.totalInvestments").value(1))
                .andExpect(jsonPath("$.marketValueByType").isArray());
    }

    @Test
    void getInvestments_WithInvalidDataType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/investments")
                .param("data", "invalid-type")
                .with(user(testUserDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInvestments_WithComplexFiltering_ShouldReturnFilteredResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getInvestmentsByType(testUser, Investment.InvestmentType.STOCK))
                .thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("type", "STOCK")
                .param("sector", "Energy")
                .param("minPrice", "20.00")
                .param("maxPrice", "30.00")
                .param("minDividendYield", "5.0")
                .param("sortBy", "currentPrice")
                .param("sortDirection", "asc")
                .param("page", "0")
                .param("size", "10")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].investmentType").value("STOCK"));
    }

    @Test
    void getInvestments_WithPagination_ShouldReturnPagedResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getAllInvestments(testUser)).thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("page", "0")
                .param("size", "1")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }
}
