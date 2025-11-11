package com.finance_control.unit.brazilian_market.controller;

import com.finance_control.brazilian_market.controller.BrazilianMarketController;
import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.brazilian_market.service.BrazilianMarketDataService;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.finance_control.shared.security.CustomUserDetails;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.security.public-endpoints=/api/brazilian-market/indicators/**,/api/brazilian-market/summary"
})
@DisplayName("BrazilianMarketController Unit Tests")
class BrazilianMarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrazilianMarketDataService marketDataService;

    @MockitoBean
    private InvestmentService investmentService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private CustomUserDetails testUserDetails;
    private Investment testInvestment;
    private MarketIndicator selicIndicator;
    private MarketIndicator cdiIndicator;
    private MarketIndicator ipcaIndicator;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        testUserDetails = new CustomUserDetails(testUser);

        testInvestment = new Investment();
        testInvestment.setId(1L);
        testInvestment.setTicker("PETR4");
        testInvestment.setName("Petrobras");
        testInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        testInvestment.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        testInvestment.setCurrentPrice(BigDecimal.valueOf(26.00));
        testInvestment.setUser(testUser);
        testInvestment.setIsActive(true);
        testInvestment.setCreatedAt(LocalDateTime.now());
        testInvestment.setUpdatedAt(LocalDateTime.now());

        selicIndicator = new MarketIndicator();
        selicIndicator.setId(1L);
        selicIndicator.setCode("SELIC");
        selicIndicator.setName("Taxa Selic");
        selicIndicator.setCurrentValue(new BigDecimal("13.75"));
        selicIndicator.setIndicatorType(MarketIndicator.IndicatorType.INTEREST_RATE);
        selicIndicator.setFrequency(MarketIndicator.Frequency.DAILY);
        selicIndicator.setReferenceDate(LocalDate.now());

        cdiIndicator = new MarketIndicator();
        cdiIndicator.setId(2L);
        cdiIndicator.setCode("CDI");
        cdiIndicator.setName("CDI");
        cdiIndicator.setCurrentValue(new BigDecimal("13.25"));
        cdiIndicator.setIndicatorType(MarketIndicator.IndicatorType.INTEREST_RATE);
        cdiIndicator.setFrequency(MarketIndicator.Frequency.DAILY);
        cdiIndicator.setReferenceDate(LocalDate.now());

        ipcaIndicator = new MarketIndicator();
        ipcaIndicator.setId(3L);
        ipcaIndicator.setCode("IPCA");
        ipcaIndicator.setName("IPCA");
        ipcaIndicator.setCurrentValue(new BigDecimal("4.62"));
        ipcaIndicator.setIndicatorType(MarketIndicator.IndicatorType.INFLATION);
        ipcaIndicator.setFrequency(MarketIndicator.Frequency.MONTHLY);
        ipcaIndicator.setReferenceDate(LocalDate.now());
    }

    @AfterEach
    void tearDown() {
        reset(marketDataService, investmentService, userRepository);
    }

    @Test
    @DisplayName("GET /brazilian-market/indicators/selic should return current Selic rate")
    void getCurrentSelicRate_ShouldReturnOk() throws Exception {
        BigDecimal expectedRate = new BigDecimal("13.75");
        when(marketDataService.getCurrentSelicRate()).thenReturn(expectedRate);

        mockMvc.perform(get("/api/brazilian-market/indicators/selic"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(13.75));

        verify(marketDataService).getCurrentSelicRate();
    }

    @Test
    @DisplayName("GET /brazilian-market/indicators/cdi should return current CDI rate")
    void getCurrentCDIRate_ShouldReturnOk() throws Exception {
        BigDecimal expectedRate = new BigDecimal("13.25");
        when(marketDataService.getCurrentCDIRate()).thenReturn(expectedRate);

        mockMvc.perform(get("/api/brazilian-market/indicators/cdi"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(13.25));

        verify(marketDataService).getCurrentCDIRate();
    }

    @Test
    @DisplayName("GET /brazilian-market/indicators/ipca should return current IPCA")
    void getCurrentIPCA_ShouldReturnOk() throws Exception {
        BigDecimal expectedIPCA = new BigDecimal("4.62");
        when(marketDataService.getCurrentIPCA()).thenReturn(expectedIPCA);

        mockMvc.perform(get("/api/brazilian-market/indicators/ipca"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(4.62));

        verify(marketDataService).getCurrentIPCA();
    }

    @Test
    @DisplayName("GET /brazilian-market/indicators should return all key indicators")
    void getKeyIndicators_ShouldReturnOk() throws Exception {
        List<MarketIndicator> indicators = Arrays.asList(selicIndicator, cdiIndicator, ipcaIndicator);
        when(marketDataService.getKeyIndicators()).thenReturn(indicators);

        mockMvc.perform(get("/api/brazilian-market/indicators"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].code").value("SELIC"))
                .andExpect(jsonPath("$[1].code").value("CDI"))
                .andExpect(jsonPath("$[2].code").value("IPCA"));

        verify(marketDataService).getKeyIndicators();
    }

    @Test
    @DisplayName("POST /brazilian-market/indicators/selic/update should update Selic rate")
    void updateSelicRate_ShouldReturnOk() throws Exception {
        CompletableFuture<MarketIndicator> future = CompletableFuture.completedFuture(selicIndicator);
        when(marketDataService.updateSelicRate()).thenReturn(future);

        mockMvc.perform(post("/api/brazilian-market/indicators/selic/update"))
                .andExpect(status().isOk());

        verify(marketDataService).updateSelicRate();
    }

    @Test
    @DisplayName("POST /brazilian-market/indicators/cdi/update should update CDI rate")
    void updateCDIRate_ShouldReturnOk() throws Exception {
        CompletableFuture<MarketIndicator> future = CompletableFuture.completedFuture(cdiIndicator);
        when(marketDataService.updateCDIRate()).thenReturn(future);

        mockMvc.perform(post("/api/brazilian-market/indicators/cdi/update"))
                .andExpect(status().isOk());

        verify(marketDataService).updateCDIRate();
    }

    @Test
    @DisplayName("POST /brazilian-market/indicators/ipca/update should update IPCA")
    void updateIPCA_ShouldReturnOk() throws Exception {
        CompletableFuture<MarketIndicator> future = CompletableFuture.completedFuture(ipcaIndicator);
        when(marketDataService.updateIPCA()).thenReturn(future);

        mockMvc.perform(post("/api/brazilian-market/indicators/ipca/update"))
                .andExpect(status().isOk());

        verify(marketDataService).updateIPCA();
    }

    @Test
    @DisplayName("POST /brazilian-market/indicators/update-all should update all indicators")
    void updateAllIndicators_ShouldReturnOk() throws Exception {
        CompletableFuture<MarketIndicator> selicFuture = CompletableFuture.completedFuture(selicIndicator);
        CompletableFuture<MarketIndicator> cdiFuture = CompletableFuture.completedFuture(cdiIndicator);
        CompletableFuture<MarketIndicator> ipcaFuture = CompletableFuture.completedFuture(ipcaIndicator);

        when(marketDataService.updateSelicRate()).thenReturn(selicFuture);
        when(marketDataService.updateCDIRate()).thenReturn(cdiFuture);
        when(marketDataService.updateIPCA()).thenReturn(ipcaFuture);

        mockMvc.perform(post("/api/brazilian-market/indicators/update-all"))
                .andExpect(status().isOk());

        verify(marketDataService).updateSelicRate();
        verify(marketDataService).updateCDIRate();
        verify(marketDataService).updateIPCA();
    }

    @Test
    @DisplayName("GET /brazilian-market/investments should return user investments")
    void getUserInvestments_WithAuthenticatedUser_ShouldReturnOk() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.getAllInvestments(testUser)).thenReturn(investments);

        mockMvc.perform(get("/api/brazilian-market/investments")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));

        verify(userRepository).findById(1L);
        verify(investmentService).getAllInvestments(testUser);
    }

    @Test
    @DisplayName("GET /brazilian-market/investments should return 403 when user not authenticated")
    void getUserInvestments_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/investments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /brazilian-market/investments/stocks should return user stocks")
    void getUserStocks_WithAuthenticatedUser_ShouldReturnOk() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        List<Investment> stocks = Arrays.asList(testInvestment);
        when(investmentService.getInvestmentsByType(testUser, Investment.InvestmentType.STOCK)).thenReturn(stocks);

        mockMvc.perform(get("/api/brazilian-market/investments/stocks")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$[0].investmentType").value("STOCK"));

        verify(userRepository).findById(1L);
        verify(investmentService).getInvestmentsByType(testUser, Investment.InvestmentType.STOCK);
    }

    @Test
    @DisplayName("GET /brazilian-market/investments/fiis should return user FIIs")
    void getUserFIIs_WithAuthenticatedUser_ShouldReturnOk() throws Exception {
        Investment fii = new Investment();
        fii.setTicker("HGLG11");
        fii.setName("CSHG Logística");
        fii.setInvestmentType(Investment.InvestmentType.FII);
        fii.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        List<Investment> fiis = Arrays.asList(fii);
        when(investmentService.getInvestmentsByType(testUser, Investment.InvestmentType.FII)).thenReturn(fiis);

        mockMvc.perform(get("/api/brazilian-market/investments/fiis")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticker").value("HGLG11"))
                .andExpect(jsonPath("$[0].investmentType").value("FII"));

        verify(userRepository).findById(1L);
        verify(investmentService).getInvestmentsByType(testUser, Investment.InvestmentType.FII);
    }

    @Test
    @DisplayName("GET /brazilian-market/investments/search should return filtered investments")
    void searchUserInvestments_WithQuery_ShouldReturnOk() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        List<Investment> investments = Arrays.asList(testInvestment);
        when(investmentService.searchInvestments(testUser, "PETR")).thenReturn(investments);

        mockMvc.perform(get("/api/brazilian-market/investments/search")
                        .param("query", "PETR")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticker").value("PETR4"));

        verify(userRepository).findById(1L);
        verify(investmentService).searchInvestments(testUser, "PETR");
    }

    @Test
    @DisplayName("POST /brazilian-market/investments/{ticker}/update should update investment data")
    void updateInvestmentData_WithValidTicker_ShouldReturnOk() throws Exception {
        Investment updatedInvestment = new Investment();
        updatedInvestment.setId(1L);
        updatedInvestment.setTicker("PETR4");
        updatedInvestment.setCurrentPrice(BigDecimal.valueOf(27.00));
        updatedInvestment.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(investmentService.getInvestmentByTicker("PETR4", testUser)).thenReturn(Optional.of(testInvestment));
        when(investmentService.updateMarketData(testInvestment)).thenReturn(updatedInvestment);

        mockMvc.perform(post("/api/brazilian-market/investments/PETR4/update")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.currentPrice").value(27.00));

        verify(userRepository).findById(1L);
        verify(investmentService).getInvestmentByTicker("PETR4", testUser);
        verify(investmentService).updateMarketData(testInvestment);
    }

    @Test
    @DisplayName("POST /brazilian-market/investments/{ticker}/update should return 400 when investment not found")
    void updateInvestmentData_WithInvalidTicker_ShouldReturnBadRequest() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(investmentService.getInvestmentByTicker("INVALID", testUser)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/brazilian-market/investments/INVALID/update")
                        .with(user(testUserDetails)))
                .andExpect(status().isBadRequest());

        verify(userRepository).findById(1L);
        verify(investmentService).getInvestmentByTicker("INVALID", testUser);
        verify(investmentService, never()).updateMarketData(any());
    }

    @Test
    @DisplayName("GET /brazilian-market/summary should return market summary")
    void getMarketSummary_ShouldReturnOk() throws Exception {
        Map<String, Object> summary = Map.of(
                "totalInvestments", 100,
                "totalValue", 50000.00
        );
        when(marketDataService.getMarketSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/brazilian-market/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalInvestments").value(100))
                .andExpect(jsonPath("$.totalValue").value(50000.00));

        verify(marketDataService).getMarketSummary();
    }

    @Test
    @DisplayName("GET /brazilian-market/investments should return 400 when user not found")
    void getUserInvestments_WithInvalidUser_ShouldReturnBadRequest() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User invalidUser = new User();
        invalidUser.setId(999L);
        invalidUser.setEmail("invalid@example.com");
        CustomUserDetails invalidUserDetails = new CustomUserDetails(invalidUser);

        mockMvc.perform(get("/api/brazilian-market/investments")
                        .with(user(invalidUserDetails)))
                .andExpect(status().isBadRequest());

        verify(userRepository).findById(999L);
        verify(investmentService, never()).getAllInvestments(any());
    }
}
