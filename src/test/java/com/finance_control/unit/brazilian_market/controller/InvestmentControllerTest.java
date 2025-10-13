package com.finance_control.unit.brazilian_market.controller;

import com.finance_control.brazilian_market.dto.InvestmentDTO;
import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.finance_control.brazilian_market.service.ExternalMarketDataService;
import com.finance_control.users.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for InvestmentController.
 * Tests the REST API endpoints for investment management.
 */
@ExtendWith(MockitoExtension.class)
class InvestmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InvestmentService investmentService;

    @Mock
    private ExternalMarketDataService externalMarketDataService;

    @InjectMocks
    private TestInvestmentController investmentController;

    private ObjectMapper objectMapper;

    private User testUser;
    private Investment testInvestment;
    private Investment testInvestmentDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime support

        mockMvc = MockMvcBuilders.standaloneSetup(investmentController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        // Create test investment
        testInvestment = new Investment();
        testInvestment.setId(1L);
        testInvestment.setTicker("PETR4");
        testInvestment.setName("Petrobras");
        testInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        testInvestment.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        testInvestment.setCurrentPrice(BigDecimal.valueOf(26.00));
        testInvestment.setIsActive(true);
        testInvestment.setUser(testUser);
        testInvestment.setCreatedAt(LocalDateTime.now());
        testInvestment.setUpdatedAt(LocalDateTime.now());

        // Create test investment DTO
        testInvestmentDTO = new Investment();
        testInvestmentDTO.setTicker("PETR4");
        testInvestmentDTO.setName("Petrobras");
        testInvestmentDTO.setInvestmentType(Investment.InvestmentType.STOCK);
        testInvestmentDTO.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        testInvestmentDTO.setCurrentPrice(BigDecimal.valueOf(26.00));
    }

    @Test
    void createInvestment_ShouldCreateInvestmentSuccessfully() throws Exception {
        // Given
        when(investmentService.createInvestment(any(InvestmentDTO.class), any(User.class)))
                .thenReturn(testInvestment);

        // When & Then
        mockMvc.perform(post("/investments")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInvestmentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.name").value("Petrobras"))
                .andExpect(jsonPath("$.investmentType").value("STOCK"));

        verify(investmentService).createInvestment(any(InvestmentDTO.class), any(User.class));
    }

    @Test
    void createInvestment_ShouldReturnConflictWhenInvestmentExists() throws Exception {
        // Given
        when(investmentService.investmentExists(eq("PETR4"), any(User.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/investments")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInvestmentDTO)))
                .andExpect(status().isConflict());

        verify(investmentService).investmentExists(eq("PETR4"), any(User.class));
        verify(investmentService, never()).createInvestment(any(InvestmentDTO.class), any(User.class));
    }

    @Test
    void getAllInvestments_ShouldReturnInvestmentsWithPagination() throws Exception {
        // Given
        when(investmentService.getAllInvestments(any(User.class)))
                .thenReturn(List.of(testInvestment));

        // When & Then
        mockMvc.perform(get("/investments")
                        .param("userId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));

        verify(investmentService).getAllInvestments(any(User.class));
    }

    @Test
    void getInvestmentById_ShouldReturnInvestmentWhenFound() throws Exception {
        // Given
        when(investmentService.getInvestmentById(eq(1L), any(User.class)))
                .thenReturn(Optional.of(testInvestment));

        // When & Then
        mockMvc.perform(get("/investments/1")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.name").value("Petrobras"));

        verify(investmentService).getInvestmentById(eq(1L), any(User.class));
    }

    @Test
    void getInvestmentById_ShouldReturnNotFoundWhenInvestmentNotFound() throws Exception {
        // Given
        when(investmentService.getInvestmentById(eq(999L), any(User.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/investments/999")
                        .param("userId", "1"))
                .andExpect(status().isNotFound());

        verify(investmentService).getInvestmentById(eq(999L), any(User.class));
    }

    @Test
    void updateInvestment_ShouldUpdateInvestmentSuccessfully() throws Exception {
        // Given
        testInvestment.setName("Petrobras Updated");
        when(investmentService.updateInvestment(eq(1L), any(InvestmentDTO.class), any(User.class)))
                .thenReturn(testInvestment);

        // When & Then
        mockMvc.perform(put("/investments/1")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInvestmentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.name").value("Petrobras Updated"));

        verify(investmentService).updateInvestment(eq(1L), any(InvestmentDTO.class), any(User.class));
    }

    @Test
    void deleteInvestment_ShouldDeleteInvestmentSuccessfully() throws Exception {
        // Given
        doNothing().when(investmentService).deleteInvestment(eq(1L), any(User.class));

        // When & Then
        mockMvc.perform(delete("/investments/1")
                        .param("userId", "1"))
                .andExpect(status().isNoContent());

        verify(investmentService).deleteInvestment(eq(1L), any(User.class));
    }

    @Test
    void getInvestmentsByType_ShouldReturnInvestmentsOfSpecificType() throws Exception {
        // Given
        when(investmentService.getInvestmentsByType(any(User.class), eq(Investment.InvestmentType.STOCK)))
                .thenReturn(List.of(testInvestment));

        // When & Then
        mockMvc.perform(get("/investments/type/STOCK")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$[0].investmentType").value("STOCK"));

        verify(investmentService).getInvestmentsByType(any(User.class), eq(Investment.InvestmentType.STOCK));
    }

    @Test
    void getInvestmentsByTypeAndSubtype_ShouldReturnFilteredInvestments() throws Exception {
        // Given
        when(investmentService.getInvestmentsByTypeAndSubtype(any(User.class), eq(Investment.InvestmentType.STOCK), eq(Investment.InvestmentSubtype.ORDINARY)))
                .thenReturn(List.of(testInvestment));

        // When & Then
        mockMvc.perform(get("/investments/type/STOCK/subtype/ORDINARY")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$[0].investmentType").value("STOCK"))
                .andExpect(jsonPath("$[0].investmentSubtype").value("ORDINARY"));

        verify(investmentService).getInvestmentsByTypeAndSubtype(any(User.class), eq(Investment.InvestmentType.STOCK), eq(Investment.InvestmentSubtype.ORDINARY));
    }

    @Test
    void searchInvestments_ShouldReturnMatchingInvestments() throws Exception {
        // Given
        when(investmentService.searchInvestments(any(User.class), eq("Petrobras")))
                .thenReturn(List.of(testInvestment));

        // When & Then
        mockMvc.perform(get("/investments/search")
                        .param("q", "Petrobras")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$[0].name").value("Petrobras"));

        verify(investmentService).searchInvestments(any(User.class), eq("Petrobras"));
    }

    @Test
    void getSectors_ShouldReturnUniqueSectors() throws Exception {
        // Given
        when(investmentService.getSectors(any(User.class)))
                .thenReturn(List.of("Energy", "Technology"));

        // When & Then
        mockMvc.perform(get("/investments/sectors")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Energy"))
                .andExpect(jsonPath("$[1]").value("Technology"));

        verify(investmentService).getSectors(any(User.class));
    }

    @Test
    void getIndustries_ShouldReturnUniqueIndustries() throws Exception {
        // Given
        when(investmentService.getIndustries(any(User.class)))
                .thenReturn(List.of("Oil & Gas", "Software"));

        // When & Then
        mockMvc.perform(get("/investments/industries")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Oil & Gas"))
                .andExpect(jsonPath("$[1]").value("Software"));

        verify(investmentService).getIndustries(any(User.class));
    }

    @Test
    void getInvestmentTypes_ShouldReturnUniqueInvestmentTypes() throws Exception {
        // Given
        when(investmentService.getInvestmentTypes(any(User.class)))
                .thenReturn(List.of(Investment.InvestmentType.STOCK, Investment.InvestmentType.FII));

        // When & Then
        mockMvc.perform(get("/investments/types")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("STOCK"))
                .andExpect(jsonPath("$[1]").value("FII"));

        verify(investmentService).getInvestmentTypes(any(User.class));
    }

    @Test
    void getInvestmentSubtypes_ShouldReturnUniqueInvestmentSubtypes() throws Exception {
        // Given
        when(investmentService.getInvestmentSubtypes(any(User.class), eq(Investment.InvestmentType.STOCK)))
                .thenReturn(List.of(Investment.InvestmentSubtype.ORDINARY, Investment.InvestmentSubtype.PREFERRED));

        // When & Then
        mockMvc.perform(get("/investments/types/STOCK/subtypes")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("ORDINARY"))
                .andExpect(jsonPath("$[1]").value("PREFERRED"));

        verify(investmentService).getInvestmentSubtypes(any(User.class), eq(Investment.InvestmentType.STOCK));
    }

    @Test
    void getTopPerformers_ShouldReturnTopPerformingInvestments() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(investmentService.getTopPerformers(any(User.class), any(Pageable.class)))
                .thenReturn(List.of(testInvestment));

        // When & Then
        mockMvc.perform(get("/investments")
                        .param("userId", "1")
                        .param("sort", "performance")
                        .param("order", "top")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));

        verify(investmentService).getTopPerformers(any(User.class), any(Pageable.class));
    }

    @Test
    void getWorstPerformers_ShouldReturnWorstPerformingInvestments() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(investmentService.getWorstPerformers(any(User.class), any(Pageable.class)))
                .thenReturn(List.of(testInvestment));

        // When & Then
        mockMvc.perform(get("/investments")
                        .param("userId", "1")
                        .param("sort", "performance")
                        .param("order", "worst")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));

        verify(investmentService).getWorstPerformers(any(User.class), any(Pageable.class));
    }

    @Test
    void getTopDividendYield_ShouldReturnTopDividendYieldInvestments() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(investmentService.getTopDividendYield(any(User.class), any(Pageable.class)))
                .thenReturn(List.of(testInvestment));

        // When & Then
        mockMvc.perform(get("/investments")
                        .param("userId", "1")
                        .param("sort", "dividend-yield")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));

        verify(investmentService).getTopDividendYield(any(User.class), any(Pageable.class));
    }

    @Test
    void getTotalMarketValue_ShouldReturnTotalMarketValue() throws Exception {
        // Given
        when(investmentService.getTotalMarketValue(any(User.class)))
                .thenReturn(Optional.of(2600.0));

        // When & Then
        mockMvc.perform(get("/investments/total-market-value")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2600.0));

        verify(investmentService).getTotalMarketValue(any(User.class));
    }

    @Test
    void getMarketValueByType_ShouldReturnMarketValueByType() throws Exception {
        // Given
        when(investmentService.getMarketValueByType(any(User.class)))
                .thenReturn(List.<Object[]>of(new Object[]{"STOCK", 2600.0}));

        // When & Then
        mockMvc.perform(get("/investments/market-value-by-type")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0][0]").value("STOCK"))
                .andExpect(jsonPath("$[0][1]").value(2600.0));

        verify(investmentService).getMarketValueByType(any(User.class));
    }
}
