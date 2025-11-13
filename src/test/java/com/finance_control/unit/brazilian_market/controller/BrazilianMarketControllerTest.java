package com.finance_control.unit.brazilian_market.controller;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.brazilian_market.service.BrazilianMarketDataService;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.finance_control.shared.config.AppProperties;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.security.publicEndpoints=/api/brazilian-market/indicators/**,/api/brazilian-market/summary"
})
@DisplayName("BrazilianMarketController Integration Tests")
class BrazilianMarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrazilianMarketDataService marketDataService;

    @MockitoBean
    private InvestmentService investmentService;

    @MockitoBean
    private UserRepository userRepository;

    private User testUser;
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
    @DisplayName("GET /api/brazilian-market/indicators/selic should return current Selic rate")
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

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public AppProperties appProperties() {
            return new AppProperties(
                new AppProperties.Database("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", "sa", "", "org.h2.Driver", "", "testdb", new AppProperties.Pool(2, 5, 1, 300000, 10000, 300000, 60000)),
                new AppProperties.Security(
                    new AppProperties.Jwt("testSecretKeyWithMinimumLengthOf256BitsForJWT", 86400000L, 604800000L, "test-issuer", "test-audience"),
                    new AppProperties.Cors(List.of("*"), List.of("GET", "POST", "PUT", "DELETE"), List.of("*"), true, 3600),
                        List.of("/api/brazilian-market/indicators/**", "/api/brazilian-market/summary")
                ),
                new AppProperties.Server(0, "", "/", 8192, 2097152, 20000, 30000, 30000),
                new AppProperties.Logging("INFO", "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n", "logs", "finance-control.log", "finance-control-error.log", 10, 30, 256, false),
                new AppProperties.Jpa("create-drop", "org.hibernate.dialect.H2Dialect", false, false, false, "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl", false, new AppProperties.Properties("false", "false", "20", "true", "true", "true", "20", "16")),
                new AppProperties.Flyway(false, List.of("classpath:db/migration"), "false", "0", "true", "false", "true", "false"),
                new AppProperties.Actuator(false, List.of("health"), "/actuator", false, false, false),
                new AppProperties.OpenApi("Finance Control API - Test", "API for managing personal finances - Test Environment", "1.0.0-test", "Finance Control Team", "test@finance-control.com", "https://github.com/LucasSantana/finance-control", "MIT License", "https://opensource.org/licenses/MIT", "http://localhost:0", "Test server"),
                new AppProperties.Pagination(5, 20, "id", "ASC"),
                new AppProperties.Redis("localhost", 6379, "", 0, 2000, new AppProperties.RedisPool(8, 8, 0, -1)),
                new AppProperties.Cache(true, 900000, 300000, 1800000),
                new AppProperties.RateLimit(true, 100, 200, 60),
                new AppProperties.Ai(),
                new AppProperties.Supabase(false, "", "", "", "", new AppProperties.SupabaseDatabase(), new AppProperties.Storage(), new AppProperties.Realtime(false, List.of("transactions", "dashboard", "goals"))),
                new AppProperties.Monitoring(true, new AppProperties.Sentry(true, "", "dev", "1.0.0", 0.1, 0.1, false, true, true), new AppProperties.HealthCheck(30, true))
            );
        }
    }

}
