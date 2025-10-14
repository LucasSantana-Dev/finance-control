package com.finance_control.integration.brazilian_market.controller;

import com.finance_control.brazilian_market.dto.InvestmentDTO;
import com.finance_control.brazilian_market.model.Investment;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for InvestmentController.
 * Tests the complete flow from HTTP request to database operations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration,org.springframework.boot.actuate.autoconfigure.data.redis.RedisReactiveHealthContributorAutoConfiguration,org.springframework.boot.actuate.autoconfigure.data.redis.RedisHealthContributorAutoConfiguration,org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration",
    "app.security.jwt.secret=mySecretKeyThatIsAtLeast256BitsLongForTestingPurposesOnly123456789012345678901234567890"
})
class InvestmentControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @MockitoBean
    private RedisConnectionFactory redisConnectionFactory;

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
        testInvestmentDTO.setInvestmentType(Investment.InvestmentType.STOCK);
        testInvestmentDTO.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
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

    private HttpHeaders getAuthenticatedHeaders() {
        String jwtToken = jwtUtils.generateToken(testUser.getId());
        System.out.println("Generated JWT token for user ID " + testUser.getId() + ": " + jwtToken);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        return headers;
    }


    @Test
    void createInvestment_ShouldCreateInvestmentInDatabase() throws Exception {
        // When
        HttpHeaders headers = getAuthenticatedHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InvestmentDTO> request = new HttpEntity<>(testInvestmentDTO, headers);

        ResponseEntity<InvestmentDTO> response = restTemplate.postForEntity(
                "/api/investments", request, InvestmentDTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTicker()).isEqualTo("PETR4");
        assertThat(response.getBody().getName()).isEqualTo("Petrobras");
        assertThat(response.getBody().getInvestmentType()).isEqualTo(Investment.InvestmentType.STOCK);

        assertThat(investmentRepository.findByTickerAndUser_IdAndIsActiveTrue("PETR4", testUser.getId()))
                .isPresent();
    }

    @Test
    void getInvestmentByTicker_ShouldReturnInvestmentFromDatabase() throws Exception {
        // Given
        Investment investment = new Investment();
        investment.setTicker("PETR4");
        investment.setName("Petrobras");
        investment.setInvestmentType(Investment.InvestmentType.STOCK);
        investment.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        investment.setCurrentPrice(BigDecimal.valueOf(26.00));
        investment.setIsActive(true);
        investment.setUser(testUser);
        investment.setCreatedAt(LocalDateTime.now());
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);

        // When & Then
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<InvestmentDTO> response = restTemplate.exchange(
                "/api/investments/ticker/PETR4", HttpMethod.GET, request, InvestmentDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTicker()).isEqualTo("PETR4");
        assertThat(response.getBody().getName()).isEqualTo("Petrobras");
        assertThat(response.getBody().getInvestmentType()).isEqualTo(Investment.InvestmentType.STOCK);
    }

    @Test
    void getAllInvestments_ShouldReturnAllInvestmentsFromDatabase() throws Exception {
        // Given
        Investment investment1 = new Investment();
        investment1.setTicker("PETR4");
        investment1.setName("Petrobras");
        investment1.setInvestmentType(Investment.InvestmentType.STOCK);
        investment1.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        investment1.setCurrentPrice(BigDecimal.valueOf(26.00));
        investment1.setIsActive(true);
        investment1.setUser(testUser);
        investment1.setCreatedAt(LocalDateTime.now());
        investment1.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment1);

        Investment investment2 = new Investment();
        investment2.setTicker("VALE3");
        investment2.setName("Vale");
        investment2.setInvestmentType(Investment.InvestmentType.STOCK);
        investment2.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        investment2.setCurrentPrice(BigDecimal.valueOf(62.00));
        investment2.setIsActive(true);
        investment2.setUser(testUser);
        investment2.setCreatedAt(LocalDateTime.now());
        investment2.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment2);

        // When & Then
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/investments", HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // The response should contain both tickers
        assertThat(response.getBody()).contains("PETR4");
        assertThat(response.getBody()).contains("VALE3");
    }

    @Test
    void updateInvestment_ShouldUpdateInvestmentInDatabase() throws Exception {
        // Given
        Investment investment = new Investment();
        investment.setTicker("PETR4");
        investment.setName("Petrobras");
        investment.setInvestmentType(Investment.InvestmentType.STOCK);
        investment.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        investment.setCurrentPrice(BigDecimal.valueOf(26.00));
        investment.setIsActive(true);
        investment.setUser(testUser);
        investment.setCreatedAt(LocalDateTime.now());
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);


        // When
        HttpHeaders headers = getAuthenticatedHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InvestmentDTO> request = new HttpEntity<>(testInvestmentDTO, headers);

        ResponseEntity<InvestmentDTO> response = restTemplate.exchange(
                "/api/investments/" + investment.getId(), HttpMethod.PUT, request, InvestmentDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Then
        Investment updatedInvestment = investmentRepository.findByTickerAndUser_IdAndIsActiveTrue("PETR4", testUser.getId())
                .orElseThrow();
    }

    @Test
    void deleteInvestment_ShouldSoftDeleteInvestmentInDatabase() throws Exception {
        // Given
        Investment investment = new Investment();
        investment.setTicker("PETR4");
        investment.setName("Petrobras");
        investment.setInvestmentType(Investment.InvestmentType.STOCK);
        investment.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        investment.setCurrentPrice(BigDecimal.valueOf(26.00));
        investment.setIsActive(true);
        investment.setUser(testUser);
        investment.setCreatedAt(LocalDateTime.now());
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);

        // When
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/investments/" + investment.getId(), HttpMethod.DELETE, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

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
        stockInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        stockInvestment.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        stockInvestment.setCurrentPrice(BigDecimal.valueOf(26.00));
        stockInvestment.setIsActive(true);
        stockInvestment.setUser(testUser);
        stockInvestment.setCreatedAt(LocalDateTime.now());
        stockInvestment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(stockInvestment);

        Investment fiiInvestment = new Investment();
        fiiInvestment.setTicker("HGLG11");
        fiiInvestment.setName("CSHG Logística");
        fiiInvestment.setInvestmentType(Investment.InvestmentType.FII);
        fiiInvestment.setInvestmentSubtype(Investment.InvestmentSubtype.TIJOLO);
        fiiInvestment.setCurrentPrice(BigDecimal.valueOf(105.00));
        fiiInvestment.setIsActive(true);
        fiiInvestment.setUser(testUser);
        fiiInvestment.setCreatedAt(LocalDateTime.now());
        fiiInvestment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(fiiInvestment);

        // When & Then
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/investments?type=STOCK", HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Parse the JSON response
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
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
        commonStock.setInvestmentType(Investment.InvestmentType.STOCK);
        commonStock.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        commonStock.setCurrentPrice(BigDecimal.valueOf(26.00));
        commonStock.setIsActive(true);
        commonStock.setUser(testUser);
        commonStock.setCreatedAt(LocalDateTime.now());
        commonStock.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(commonStock);

        Investment preferredStock = new Investment();
        preferredStock.setTicker("PETR3");
        preferredStock.setName("Petrobras");
        preferredStock.setInvestmentType(Investment.InvestmentType.STOCK);
        preferredStock.setInvestmentSubtype(Investment.InvestmentSubtype.PREFERRED);
        preferredStock.setCurrentPrice(BigDecimal.valueOf(31.00));
        preferredStock.setIsActive(true);
        preferredStock.setUser(testUser);
        preferredStock.setCreatedAt(LocalDateTime.now());
        preferredStock.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(preferredStock);

        // When & Then
        HttpHeaders headers = getAuthenticatedHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/investments?type=STOCK&subtype=ORDINARY", HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Parse the JSON response
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) responseMap.get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("investmentType")).isEqualTo("STOCK");
        assertThat(content.get(0).get("investmentSubtype")).isEqualTo("ORDINARY");
    }

    @Test
    void createInvestment_ShouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // When & Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InvestmentDTO> request = new HttpEntity<>(testInvestmentDTO, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/investments", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createInvestment_ShouldReturnBadRequestForInvalidData() throws Exception {
        // Given
        InvestmentDTO invalidDTO = new InvestmentDTO();

        // When & Then
        HttpHeaders headers = getAuthenticatedHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InvestmentDTO> request = new HttpEntity<>(invalidDTO, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/investments", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
