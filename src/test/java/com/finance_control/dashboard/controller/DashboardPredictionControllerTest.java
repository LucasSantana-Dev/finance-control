package com.finance_control.dashboard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.dashboard.dto.FinancialPredictionRequest;
import com.finance_control.dashboard.dto.FinancialPredictionResponse;
import com.finance_control.dashboard.dto.ForecastedMonthDTO;
import com.finance_control.dashboard.dto.PredictionRecommendationDTO;
import com.finance_control.dashboard.service.FinancialPredictionService;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.security.CustomUserDetails;
import com.finance_control.users.model.User;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardPredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ObjectProvider<FinancialPredictionService> financialPredictionServiceProvider;

    @MockitoBean
    private FinancialPredictionService financialPredictionService;

    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);
        User user = new User();
        user.setId(1L);
        user.setEmail("prediction@test.com");
        user.setPassword("password");
        user.setIsActive(true);
        testUserDetails = new CustomUserDetails(user);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void generateFinancialPredictions_ShouldReturnOk() throws Exception {
        FinancialPredictionResponse response = FinancialPredictionResponse.builder()
                .summary("Forecast remains stable")
                .forecast(List.of(ForecastedMonthDTO.builder()
                        .month(YearMonth.parse("2025-12"))
                        .projectedIncome(BigDecimal.valueOf(5100.25))
                        .projectedExpenses(BigDecimal.valueOf(3400.10))
                        .projectedNet(BigDecimal.valueOf(1700.15))
                        .notes("Expect seasonal variation in expenses.")
                        .build()))
                .recommendations(List.of(PredictionRecommendationDTO.builder()
                        .title("Increase savings allocation")
                        .description("Allocate an additional 3% of income towards emergency savings.")
                        .build()))
                .rawModelResponse("{\"summary\":\"Forecast remains stable\"}")
                .build();

        when(financialPredictionServiceProvider.getIfAvailable()).thenReturn(financialPredictionService);
        when(financialPredictionService.generatePrediction(any(FinancialPredictionRequest.class))).thenReturn(response);

        FinancialPredictionRequest request = FinancialPredictionRequest.builder()
                .historyMonths(6)
                .forecastMonths(3)
                .build();

        mockMvc.perform(post("/api/dashboard/predictions")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.summary").value("Forecast remains stable"))
                .andExpect(jsonPath("$.forecast[0].projectedIncome").value(5100.25))
                .andExpect(jsonPath("$.recommendations[0].title").value("Increase savings allocation"));
    }

    @Test
    void generateFinancialPredictions_DisabledFeature_ShouldReturnServiceUnavailable() throws Exception {
        when(financialPredictionServiceProvider.getIfAvailable()).thenReturn(null);

        FinancialPredictionRequest request = FinancialPredictionRequest.builder()
                .historyMonths(6)
                .forecastMonths(3)
                .build();

        mockMvc.perform(post("/api/dashboard/predictions")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isServiceUnavailable());
    }
}
