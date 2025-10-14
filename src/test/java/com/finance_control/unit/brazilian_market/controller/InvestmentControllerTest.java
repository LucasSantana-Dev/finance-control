package com.finance_control.unit.brazilian_market.controller;

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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
class InvestmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InvestmentService investmentService;

    @Mock
    private ExternalMarketDataService externalMarketDataService;

    @InjectMocks
    private TestInvestmentController testInvestmentController;

    private ObjectMapper objectMapper;

    private User testUser;
    private Investment testInvestment;
    private InvestmentDTO testInvestmentDTO;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(testInvestmentController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
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
        when(investmentService.getAllInvestments(any(User.class))).thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("userId", "1")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$[0].name").value("Petrobras"));
    }

    @Test
    void getInvestments_WithTypeFilter_ShouldReturnFilteredResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getInvestmentsByType(any(User.class), eq(Investment.InvestmentType.STOCK)))
                .thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("userId", "1")
                .param("type", "STOCK"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].investmentType").value("STOCK"));
    }

    @Test
    void getInvestments_WithSearchTerm_ShouldReturnSearchResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.searchInvestments(any(User.class), eq("PETR4"))).thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("userId", "1")
                .param("search", "PETR4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));
    }

    @Test
    void getInvestments_WithPriceRangeFilter_ShouldReturnFilteredResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getAllInvestments(any(User.class))).thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("userId", "1")
                .param("minPrice", "20.00")
                .param("maxPrice", "30.00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].currentPrice").value(26.00));
    }

    @Test
    void getInvestments_WithDividendYieldFilter_ShouldReturnFilteredResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getAllInvestments(any(User.class))).thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("userId", "1")
                .param("minDividendYield", "5.0")
                .param("maxDividendYield", "10.0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].dividendYield").value(8.5));
    }

    @Test
    void getInvestments_WithSectorsMetadata_ShouldReturnSectors() throws Exception {
        List<String> sectors = Arrays.asList("Energy", "Technology", "Finance");
        when(investmentService.getSectors(any(User.class))).thenReturn(sectors);

        mockMvc.perform(get("/investments/metadata")
                .param("userId", "1")
                .param("data", "sectors"))
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
        when(investmentService.getIndustries(any(User.class))).thenReturn(industries);

        mockMvc.perform(get("/investments/metadata")
                .param("userId", "1")
                .param("data", "industries"))
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
        when(investmentService.getInvestmentTypes(any(User.class))).thenReturn(types);

        mockMvc.perform(get("/investments/metadata")
                .param("userId", "1")
                .param("data", "types"))
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
        when(investmentService.getInvestmentSubtypes(any(User.class), eq(Investment.InvestmentType.STOCK)))
                .thenReturn(subtypes);

        mockMvc.perform(get("/investments/metadata")
                .param("userId", "1")
                .param("data", "subtypes")
                .param("type", "STOCK"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("ORDINARY"))
                .andExpect(jsonPath("$[1]").value("PREFERRED"));
    }

    @Test
    void getInvestments_WithSubtypesMetadataWithoutType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/investments/metadata")
                .param("userId", "1")
                .param("data", "subtypes"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInvestments_WithMarketValueByTypeMetadata_ShouldReturnMarketValueByType() throws Exception {
        List<Object[]> marketValueList = new java.util.ArrayList<>();
        marketValueList.add(new Object[]{"STOCK", 5000.0});
        marketValueList.add(new Object[]{"FII", 3000.0});
        when(investmentService.getMarketValueByType(any(User.class))).thenReturn(marketValueList);

        mockMvc.perform(get("/investments/metadata")
                .param("userId", "1")
                .param("data", "market-value-by-type"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0][0]").value("STOCK"))
                .andExpect(jsonPath("$[0][1]").value(5000.0))
                .andExpect(jsonPath("$[1][0]").value("FII"))
                .andExpect(jsonPath("$[1][1]").value(3000.0));
    }

    @Test
    void getInvestments_WithTotalMarketValueMetadata_ShouldReturnTotalMarketValue() throws Exception {
        when(investmentService.getTotalMarketValue(any(User.class))).thenReturn(Optional.of(10000.0));

        mockMvc.perform(get("/investments/metadata")
                .param("userId", "1")
                .param("data", "total-market-value"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(10000.0));
    }

    @Test
    void getInvestments_WithInvalidDataType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/investments/metadata")
                .param("userId", "1")
                .param("data", "invalid-type"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInvestments_WithComplexFiltering_ShouldReturnFilteredResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getInvestmentsByType(any(User.class), eq(Investment.InvestmentType.STOCK)))
                .thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("userId", "1")
                .param("type", "STOCK")
                .param("sector", "Energy")
                .param("minPrice", "20.00")
                .param("maxPrice", "30.00")
                .param("minDividendYield", "5.0")
                .param("sortBy", "currentPrice")
                .param("sortDirection", "asc")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].investmentType").value("STOCK"));
    }

    @Test
    void getInvestments_WithPagination_ShouldReturnPagedResults() throws Exception {
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getAllInvestments(any(User.class))).thenReturn(investments);

        mockMvc.perform(get("/investments")
                .param("userId", "1")
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
