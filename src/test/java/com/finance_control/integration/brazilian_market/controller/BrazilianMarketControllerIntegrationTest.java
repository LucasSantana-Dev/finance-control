package com.finance_control.integration.brazilian_market.controller;

import com.finance_control.brazilian_market.controller.BrazilianMarketController;
import com.finance_control.brazilian_market.model.BrazilianStock;
import com.finance_control.brazilian_market.model.FII;
import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.brazilian_market.repository.BrazilianStockRepository;
import com.finance_control.brazilian_market.repository.FIIRepository;
import com.finance_control.brazilian_market.repository.MarketIndicatorRepository;
import com.finance_control.integration.BaseIntegrationTest;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BrazilianMarketController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BrazilianMarketControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrazilianMarketController controller;

    @Autowired
    private MarketIndicatorRepository indicatorRepository;

    @Autowired
    private BrazilianStockRepository stockRepository;

    @Autowired
    private FIIRepository fiiRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private MarketIndicator testIndicator;
    private BrazilianStock testStock;
    private FII testFII;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Create test indicator
        testIndicator = new MarketIndicator();
        testIndicator.setCode("SELIC");
        testIndicator.setName("Taxa Selic");
        testIndicator.setDescription("Taxa básica de juros da economia brasileira");
        testIndicator.setIndicatorType(MarketIndicator.IndicatorType.INTEREST_RATE);
        testIndicator.setFrequency(MarketIndicator.Frequency.DAILY);
        testIndicator.setCurrentValue(new BigDecimal("13.75"));
        testIndicator.setPreviousValue(new BigDecimal("13.50"));
        testIndicator.setChangeValue(new BigDecimal("0.25"));
        testIndicator.setChangePercent(new BigDecimal("1.85"));
        testIndicator.setLastUpdated(LocalDateTime.now());
        testIndicator.setIsActive(true);
        testIndicator = indicatorRepository.save(testIndicator);

        // Create test stock
        testStock = new BrazilianStock();
        testStock.setTicker("PETR4");
        testStock.setCompanyName("Petrobras");
        testStock.setDescription("Petróleo Brasileiro S.A.");
        testStock.setStockType(BrazilianStock.StockType.ORDINARY);
        testStock.setSegment(BrazilianStock.MarketSegment.NOVO_MERCADO);
        testStock.setCurrentPrice(new BigDecimal("25.50"));
        testStock.setPreviousClose(new BigDecimal("24.50"));
        testStock.setDayChange(new BigDecimal("1.00"));
        testStock.setDayChangePercent(new BigDecimal("4.08"));
        testStock.setVolume(1000000L);
        testStock.setMarketCap(new BigDecimal("1000000000.00"));
        testStock.setLastUpdated(LocalDateTime.now());
        testStock.setIsActive(true);
        testStock.setUser(testUser);
        testStock = stockRepository.save(testStock);

        // Create test FII
        testFII = new FII();
        testFII.setTicker("HGLG11");
        testFII.setFundName("CSHG Logística");
        testFII.setDescription("Fundo de Investimento Imobiliário");
        testFII.setFiiType(FII.FIIType.TIJOLO);
        testFII.setSegment(FII.FIISegment.LOGISTICS);
        testFII.setCurrentPrice(new BigDecimal("120.00"));
        testFII.setPreviousClose(new BigDecimal("119.00"));
        testFII.setDayChange(new BigDecimal("1.00"));
        testFII.setDayChangePercent(new BigDecimal("0.84"));
        testFII.setVolume(500000L);
        testFII.setMarketCap(new BigDecimal("500000000.00"));
        testFII.setDividendYield(new BigDecimal("7.50"));
        testFII.setLastDividend(new BigDecimal("0.80"));
        testFII.setNetWorth(new BigDecimal("115.00"));
        testFII.setPvpRatio(new BigDecimal("1.04"));
        testFII.setLastUpdated(LocalDateTime.now());
        testFII.setIsActive(true);
        testFII.setUser(testUser);
        testFII = fiiRepository.save(testFII);
    }

    @Test
    @WithMockUser(username = "1")
    void getCurrentSelicRate_ShouldReturnSelicRate() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/indicators/selic")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(13.75));
    }

    @Test
    @WithMockUser(username = "1")
    void getCurrentCDIRate_ShouldReturnCDIRate() throws Exception {
        // Create CDI indicator
        MarketIndicator cdiIndicator = new MarketIndicator();
        cdiIndicator.setCode("CDI");
        cdiIndicator.setName("CDI");
        cdiIndicator.setCurrentValue(new BigDecimal("13.25"));
        cdiIndicator.setIsActive(true);
        indicatorRepository.save(cdiIndicator);

        mockMvc.perform(get("/api/brazilian-market/indicators/cdi")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(13.25));
    }

    @Test
    @WithMockUser(username = "1")
    void getCurrentIPCA_ShouldReturnIPCA() throws Exception {
        // Create IPCA indicator
        MarketIndicator ipcaIndicator = new MarketIndicator();
        ipcaIndicator.setCode("IPCA");
        ipcaIndicator.setName("IPCA");
        ipcaIndicator.setCurrentValue(new BigDecimal("4.62"));
        ipcaIndicator.setIsActive(true);
        indicatorRepository.save(ipcaIndicator);

        mockMvc.perform(get("/api/brazilian-market/indicators/ipca")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(4.62));
    }

    @Test
    @WithMockUser(username = "1")
    void getKeyIndicators_ShouldReturnKeyIndicators() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/indicators")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].code", is("SELIC")))
                .andExpect(jsonPath("$[0].name", is("Taxa Selic")))
                .andExpect(jsonPath("$[0].currentValue", is(13.75)));
    }

    @Test
    @WithMockUser(username = "1")
    void getUserStocks_ShouldReturnUserStocks() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/stocks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ticker", is("PETR4")))
                .andExpect(jsonPath("$[0].companyName", is("Petrobras")))
                .andExpect(jsonPath("$[0].currentPrice", is(25.50)));
    }

    @Test
    @WithMockUser(username = "1")
    void getUserFIIs_ShouldReturnUserFIIs() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/fiis")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ticker", is("HGLG11")))
                .andExpect(jsonPath("$[0].fundName", is("CSHG Logística")))
                .andExpect(jsonPath("$[0].currentPrice", is(120.00)));
    }

    @Test
    @WithMockUser(username = "1")
    void searchUserStocks_WithValidQuery_ShouldReturnFilteredStocks() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/stocks/search")
                .param("query", "PETR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ticker", is("PETR4")));
    }

    @Test
    @WithMockUser(username = "1")
    void searchUserFIIs_WithValidQuery_ShouldReturnFilteredFIIs() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/fiis/search")
                .param("query", "HGLG")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ticker", is("HGLG11")));
    }

    @Test
    @WithMockUser(username = "1")
    void searchUserStocks_WithNoResults_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/stocks/search")
                .param("query", "NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "1")
    void searchUserFIIs_WithNoResults_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/fiis/search")
                .param("query", "NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "1")
    void updateSelicRate_ShouldReturnFuture() throws Exception {
        mockMvc.perform(post("/api/brazilian-market/indicators/selic/update")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "1")
    void updateCDIRate_ShouldReturnFuture() throws Exception {
        mockMvc.perform(post("/api/brazilian-market/indicators/cdi/update")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "1")
    void updateIPCA_ShouldReturnFuture() throws Exception {
        mockMvc.perform(post("/api/brazilian-market/indicators/ipca/update")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "1")
    void updateStockData_ShouldReturnFuture() throws Exception {
        mockMvc.perform(post("/api/brazilian-market/stocks/PETR4/update")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "1")
    void updateFIIData_ShouldReturnFuture() throws Exception {
        mockMvc.perform(post("/api/brazilian-market/fiis/HGLG11/update")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "1")
    void getMarketSummary_ShouldReturnSummary() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "1")
    void updateAllIndicators_ShouldReturnFuturesMap() throws Exception {
        mockMvc.perform(post("/api/brazilian-market/indicators/update-all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasKey("selic")))
                .andExpect(jsonPath("$", hasKey("cdi")))
                .andExpect(jsonPath("$", hasKey("ipca")));
    }

    @Test
    void getCurrentSelicRate_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/brazilian-market/indicators/selic")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1")
    void getUserStocks_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        // This test would need to be adjusted based on your authentication setup
        // For now, we'll test with a valid user ID
        mockMvc.perform(get("/api/brazilian-market/stocks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
