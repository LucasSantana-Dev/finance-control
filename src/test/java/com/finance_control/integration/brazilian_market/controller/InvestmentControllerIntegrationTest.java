package com.finance_control.integration.brazilian_market.controller;

import com.finance_control.brazilian_market.dto.InvestmentDTO;
import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.repository.InvestmentRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for InvestmentController.
 * Tests the complete flow from HTTP request to database operations.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class InvestmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private InvestmentDTO testInvestmentDTO;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Create test investment DTO
        testInvestmentDTO = new InvestmentDTO();
        testInvestmentDTO.setTicker("PETR4");
        testInvestmentDTO.setName("Petrobras");
        testInvestmentDTO.setInvestmentType(Investment.InvestmentType.STOCK);
        testInvestmentDTO.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createInvestment_ShouldCreateInvestmentInDatabase() throws Exception {
        // When
        mockMvc.perform(post("/api/investments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInvestmentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.name").value("Petrobras"))
                .andExpect(jsonPath("$.investmentType").value("STOCK"));

        // Then
        assertThat(investmentRepository.findByTickerAndUser_IdAndIsActiveTrue("PETR4", testUser.getId()))
                .isPresent();
    }

    @Test
    @WithMockUser(username = "test@example.com")
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
        mockMvc.perform(get("/api/investments/PETR4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.name").value("Petrobras"))
                .andExpect(jsonPath("$.investmentType").value("STOCK"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
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
        mockMvc.perform(get("/api/investments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$[1].ticker").value("VALE3"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
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

        // Update the DTO

        // When
        mockMvc.perform(put("/api/investments/PETR4")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInvestmentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(150));

        // Then
        Investment updatedInvestment = investmentRepository.findByTickerAndUser_IdAndIsActiveTrue("PETR4", testUser.getId())
                .orElseThrow();
    }

    @Test
    @WithMockUser(username = "test@example.com")
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
        mockMvc.perform(delete("/api/investments/PETR4")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Then
        assertThat(investmentRepository.findByTickerAndUser_IdAndIsActiveTrue("PETR4", testUser.getId()))
                .isEmpty();
    }

    @Test
    @WithMockUser(username = "test@example.com")
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
        mockMvc.perform(get("/api/investments/type/STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].investmentType").value("STOCK"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
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
        mockMvc.perform(get("/api/investments/type/STOCK/subtype/COMMON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].investmentType").value("STOCK"))
                .andExpect(jsonPath("$[0].investmentSubtype").value("COMMON"));
    }

    @Test
    void createInvestment_ShouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/investments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInvestmentDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createInvestment_ShouldReturnBadRequestForInvalidData() throws Exception {
        // Given
        InvestmentDTO invalidDTO = new InvestmentDTO();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/investments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }
}
