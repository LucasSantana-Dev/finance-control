package com.finance_control.integration.brazilian_market.controller;

import com.finance_control.brazilian_market.dto.InvestmentDTO;
import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.model.InvestmentType;
import com.finance_control.brazilian_market.model.InvestmentSubtype;
import com.finance_control.brazilian_market.repository.InvestmentRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import com.finance_control.shared.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for InvestmentController.
 * Tests the complete flow from HTTP request to database operations.
 * Uses TestContainers PostgreSQL for full database compatibility.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration,org.springframework.boot.actuate.autoconfigure.data.redis.RedisReactiveHealthContributorAutoConfiguration,org.springframework.boot.actuate.autoconfigure.data.redis.RedisHealthContributorAutoConfiguration,org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration,org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration,org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration",
    "app.security.jwt.secret=mySecretKeyThatIsAtLeast256BitsLongForTestingPurposesOnly123456789012345678901234567890"
})
class InvestmentControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("finance_control_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @MockitoBean
    private CacheManager cacheManager;

    @MockitoBean
    private com.finance_control.brazilian_market.service.BrazilianMarketDataService brazilianMarketDataService;

    @MockitoBean
    @Qualifier("brazilianMarketDataProvider")
    private com.finance_control.brazilian_market.client.MarketDataProvider brazilianMarketDataProvider;

    @MockitoBean
    @Qualifier("usMarketDataProvider")
    private com.finance_control.brazilian_market.client.MarketDataProvider usMarketDataProvider;

    @MockitoBean
    private RestTemplate marketDataRestTemplate;

    private User testUser;
    private InvestmentDTO testInvestmentDTO;

    @BeforeEach
    void setUp() {
        // Create test user in a separate transaction to ensure it's committed
        testUser = createTestUser();

        // Create test investment DTO
        testInvestmentDTO = new InvestmentDTO();
        testInvestmentDTO.setTicker("PETR4");
        testInvestmentDTO.setName("Petrobras");
        testInvestmentDTO.setInvestmentType(InvestmentType.STOCK);
        testInvestmentDTO.setInvestmentSubtype(InvestmentSubtype.ORDINARY);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        if (testUser != null) {
            // Delete all investments for the test user (both active and inactive)
            List<Investment> userInvestments = investmentRepository.findByUser_Id(testUser.getId());
            investmentRepository.deleteAll(userInvestments);

            // Delete the test user
            userRepository.delete(testUser);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private User createTestUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setIsActive(true);
        User savedUser = userRepository.save(user);
        System.out.println("Created test user with ID: " + savedUser.getId());

        // Verify the user exists in the database
        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
        System.out.println("Found user in database: " + (foundUser != null ? "YES" : "NO"));
        if (foundUser != null) {
            System.out.println("User details - ID: " + foundUser.getId() + ", Email: " + foundUser.getEmail() + ", Active: " + foundUser.getIsActive());
        }

        return savedUser;
    }

    private String getAuthToken() {
        return jwtUtils.generateToken(testUser.getId());
    }


    @Test
    void createInvestment_ShouldCreateInvestmentInDatabase() throws Exception {
        // When
        String token = getAuthToken();
        String requestBody = objectMapper.writeValueAsString(testInvestmentDTO);

        mockMvc.perform(post("/api/investments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.name").value("Petrobras"))
                .andExpect(jsonPath("$.investmentType").value("STOCK"));

        // Then
        assertThat(investmentRepository.findByTickerAndUser_IdAndIsActiveTrue("PETR4", testUser.getId()))
                .isPresent();
    }

    @Test
    void getInvestmentByTicker_ShouldReturnInvestmentFromDatabase() throws Exception {
        // Given
        Investment investment = new Investment();
        investment.setTicker("PETR4");
        investment.setName("Petrobras");
        investment.setInvestmentType(InvestmentType.STOCK);
        investment.setInvestmentSubtype(InvestmentSubtype.ORDINARY);
        investment.setCurrentPrice(BigDecimal.valueOf(26.00));
        investment.setIsActive(true);
        investment.setUser(testUser);
        investment.setCreatedAt(LocalDateTime.now());
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);

        // When & Then
        String token = getAuthToken();
        mockMvc.perform(get("/api/investments/ticker/PETR4")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.name").value("Petrobras"))
                .andExpect(jsonPath("$.investmentType").value("STOCK"));
    }

    @Test
    void getAllInvestments_ShouldReturnAllInvestmentsFromDatabase() throws Exception {
        // Given
        Investment investment1 = new Investment();
        investment1.setTicker("PETR4");
        investment1.setName("Petrobras");
        investment1.setInvestmentType(InvestmentType.STOCK);
        investment1.setInvestmentSubtype(InvestmentSubtype.ORDINARY);
        investment1.setCurrentPrice(BigDecimal.valueOf(26.00));
        investment1.setIsActive(true);
        investment1.setUser(testUser);
        investment1.setCreatedAt(LocalDateTime.now());
        investment1.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment1);

        Investment investment2 = new Investment();
        investment2.setTicker("VALE3");
        investment2.setName("Vale");
        investment2.setInvestmentType(InvestmentType.STOCK);
        investment2.setInvestmentSubtype(InvestmentSubtype.ORDINARY);
        investment2.setCurrentPrice(BigDecimal.valueOf(62.00));
        investment2.setIsActive(true);
        investment2.setUser(testUser);
        investment2.setCreatedAt(LocalDateTime.now());
        investment2.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment2);

        // When & Then
        String token = getAuthToken();
        String response = mockMvc.perform(get("/api/investments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isNotNull();
        // The response should contain both tickers
        assertThat(response).contains("PETR4");
        assertThat(response).contains("VALE3");
    }

    @Test
    void updateInvestment_ShouldUpdateInvestmentInDatabase() throws Exception {
        // Given
        Investment investment = new Investment();
        investment.setTicker("PETR4");
        investment.setName("Petrobras");
        investment.setInvestmentType(InvestmentType.STOCK);
        investment.setInvestmentSubtype(InvestmentSubtype.ORDINARY);
        investment.setCurrentPrice(BigDecimal.valueOf(26.00));
        investment.setIsActive(true);
        investment.setUser(testUser);
        investment.setCreatedAt(LocalDateTime.now());
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);


        // When
        String token = getAuthToken();
        String requestBody = objectMapper.writeValueAsString(testInvestmentDTO);

        mockMvc.perform(put("/api/investments/" + investment.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void deleteInvestment_ShouldSoftDeleteInvestmentInDatabase() throws Exception {
        // Given
        Investment investment = new Investment();
        investment.setTicker("PETR4");
        investment.setName("Petrobras");
        investment.setInvestmentType(InvestmentType.STOCK);
        investment.setInvestmentSubtype(InvestmentSubtype.ORDINARY);
        investment.setCurrentPrice(BigDecimal.valueOf(26.00));
        investment.setIsActive(true);
        investment.setUser(testUser);
        investment.setCreatedAt(LocalDateTime.now());
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);

        // When
        String token = getAuthToken();
        mockMvc.perform(delete("/api/investments/" + investment.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Then
        assertThat(investmentRepository.findByTickerAndUser_IdAndIsActiveTrue("PETR4", testUser.getId()))
                .isEmpty();
    }

    @Test
    void getInvestmentsByType_ShouldReturnInvestmentsOfSpecificType() throws Exception {
        // Given
        Investment stockInvestment = new Investment();
        stockInvestment.setTicker("PETR4");
        stockInvestment.setName("Petrobras");
        stockInvestment.setInvestmentType(InvestmentType.STOCK);
        stockInvestment.setInvestmentSubtype(InvestmentSubtype.ORDINARY);
        stockInvestment.setCurrentPrice(BigDecimal.valueOf(26.00));
        stockInvestment.setIsActive(true);
        stockInvestment.setUser(testUser);
        stockInvestment.setCreatedAt(LocalDateTime.now());
        stockInvestment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(stockInvestment);

        Investment fiiInvestment = new Investment();
        fiiInvestment.setTicker("HGLG11");
        fiiInvestment.setName("CSHG Logística");
        fiiInvestment.setInvestmentType(InvestmentType.FII);
        fiiInvestment.setInvestmentSubtype(InvestmentSubtype.TIJOLO);
        fiiInvestment.setCurrentPrice(BigDecimal.valueOf(105.00));
        fiiInvestment.setIsActive(true);
        fiiInvestment.setUser(testUser);
        fiiInvestment.setCreatedAt(LocalDateTime.now());
        fiiInvestment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(fiiInvestment);

        // When & Then
        String token = getAuthToken();
        String responseBody = mockMvc.perform(get("/api/investments")
                        .param("type", "STOCK")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(responseBody).isNotNull();

        // Parse the JSON response
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = (Map<String, Object>) objectMapper.readValue(responseBody, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) responseMap.get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("investmentType")).isEqualTo("STOCK");
    }

    @Test
    void getInvestmentsByTypeAndSubtype_ShouldReturnInvestmentsOfSpecificTypeAndSubtype() throws Exception {
        // Given
        Investment commonStock = new Investment();
        commonStock.setTicker("PETR4");
        commonStock.setName("Petrobras");
        commonStock.setInvestmentType(InvestmentType.STOCK);
        commonStock.setInvestmentSubtype(InvestmentSubtype.ORDINARY);
        commonStock.setCurrentPrice(BigDecimal.valueOf(26.00));
        commonStock.setIsActive(true);
        commonStock.setUser(testUser);
        commonStock.setCreatedAt(LocalDateTime.now());
        commonStock.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(commonStock);

        Investment preferredStock = new Investment();
        preferredStock.setTicker("PETR3");
        preferredStock.setName("Petrobras");
        preferredStock.setInvestmentType(InvestmentType.STOCK);
        preferredStock.setInvestmentSubtype(InvestmentSubtype.PREFERRED);
        preferredStock.setCurrentPrice(BigDecimal.valueOf(31.00));
        preferredStock.setIsActive(true);
        preferredStock.setUser(testUser);
        preferredStock.setCreatedAt(LocalDateTime.now());
        preferredStock.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(preferredStock);

        // When & Then
        String token = getAuthToken();
        String responseBody = mockMvc.perform(get("/api/investments")
                        .param("type", "STOCK")
                        .param("subtype", "ORDINARY")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(responseBody).isNotNull();

        // Parse the JSON response
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = (Map<String, Object>) objectMapper.readValue(responseBody, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) responseMap.get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("investmentType")).isEqualTo("STOCK");
        assertThat(content.get(0).get("investmentSubtype")).isEqualTo("ORDINARY");
    }

    @Test
    void createInvestment_ShouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // When & Then
        String requestBody = objectMapper.writeValueAsString(testInvestmentDTO);

        mockMvc.perform(post("/api/investments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void createInvestment_ShouldReturnBadRequestForInvalidData() throws Exception {
        // Given
        InvestmentDTO invalidDTO = new InvestmentDTO();
        String requestBody = objectMapper.writeValueAsString(invalidDTO);

        // When & Then
        String token = getAuthToken();
        mockMvc.perform(post("/api/investments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
