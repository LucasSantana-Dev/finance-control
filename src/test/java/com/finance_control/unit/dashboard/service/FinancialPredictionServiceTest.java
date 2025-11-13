package com.finance_control.unit.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.dashboard.dto.CategorySpendingDTO;
import com.finance_control.dashboard.dto.FinancialMetricsDTO;
import com.finance_control.dashboard.dto.FinancialPredictionRequest;
import com.finance_control.dashboard.dto.FinancialPredictionResponse;
import com.finance_control.dashboard.dto.MonthlyTrendDTO;
import com.finance_control.dashboard.service.DashboardService;
import com.finance_control.dashboard.service.FinancialPredictionService;
import com.finance_control.dashboard.service.PredictionModelClient;
import com.finance_control.unit.TestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinancialPredictionServiceTest {

    @Mock
    private PredictionModelClient predictionModelClient;

    @Mock
    private DashboardService dashboardService;

    private FinancialPredictionService financialPredictionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        TestUtils.setupUserContext(1L);
        financialPredictionService = new FinancialPredictionService(predictionModelClient, dashboardService, objectMapper);
    }

    @AfterEach
    void tearDown() {
        TestUtils.clearUserContext();
    }

    @Test
    void generatePrediction_ShouldReturnStructuredForecast() {
        when(dashboardService.getMonthlyTrends(eq(1L), anyInt())).thenReturn(List.of(
                MonthlyTrendDTO.builder()
                        .month(YearMonth.of(2025, 8).atDay(1))
                        .income(BigDecimal.valueOf(5000))
                        .expenses(BigDecimal.valueOf(3500))
                        .balance(BigDecimal.valueOf(1500))
                        .transactionCount(42)
                        .build()
        ));

        when(dashboardService.getTopSpendingCategories(eq(1L), anyInt())).thenReturn(List.of(
                CategorySpendingDTO.builder()
                        .categoryName("Housing")
                        .amount(BigDecimal.valueOf(1200))
                        .percentage(BigDecimal.valueOf(30))
                        .transactionCount(5)
                        .build()
        ));

        when(dashboardService.getFinancialMetrics(any(LocalDate.class), any(LocalDate.class))).thenReturn(
                FinancialMetricsDTO.builder()
                        .monthlyIncome(BigDecimal.valueOf(5000))
                        .monthlyExpenses(BigDecimal.valueOf(3500))
                        .monthlySavings(BigDecimal.valueOf(1500))
                        .savingsRate(BigDecimal.valueOf(30))
                        .totalTransactions(42)
                        .incomeTransactions(20)
                        .expenseTransactions(22)
                        .periodStart(LocalDate.of(2025, 8, 1))
                        .periodEnd(LocalDate.of(2025, 8, 31))
                        .build()
        );

        String rawModelResponse = """
                {
                  "summary": "Cash flow remains healthy. Maintain savings momentum.",
                  "forecast": [
                    {
                      "month": "2025-12",
                      "projectedIncome": 5100.50,
                      "projectedExpenses": 3400.40,
                      "projectedNet": 1700.10,
                      "notes": "Expect seasonal bonuses to improve liquidity."
                    }
                  ],
                  "recommendations": [
                    {
                      "title": "Allocate bonus wisely",
                      "description": "Channel year-end bonuses into emergency savings before discretionary spending."
                    }
                  ]
                }
                """;

        when(predictionModelClient.generateForecast(any(String.class))).thenReturn(rawModelResponse);

        FinancialPredictionResponse response = financialPredictionService.generatePrediction(
                FinancialPredictionRequest.builder()
                        .historyMonths(6)
                        .forecastMonths(3)
                        .financialGoal("Increase savings by 15%")
                        .build());

        assertThat(response.getSummary()).contains("Cash flow remains healthy");
        assertThat(response.getForecast()).hasSize(1);
        assertThat(response.getForecast().get(0).getProjectedIncome()).isEqualByComparingTo("5100.50");
        assertThat(response.getRecommendations()).extracting("title")
                .contains("Allocate bonus wisely");
        assertThat(response.getRawModelResponse()).contains("projectedIncome");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(predictionModelClient).generateForecast(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("Historical monthly summary").contains("Increase savings by 15%");
    }

    @Test
    void generatePrediction_ShouldHandleMalformedJsonGracefully() {
        when(dashboardService.getMonthlyTrends(eq(1L), anyInt())).thenReturn(List.of());
        when(dashboardService.getTopSpendingCategories(eq(1L), anyInt())).thenReturn(List.of());
        when(dashboardService.getFinancialMetrics(any(LocalDate.class), any(LocalDate.class))).thenReturn(
                FinancialMetricsDTO.builder().build()
        );
        when(predictionModelClient.generateForecast(any(String.class))).thenReturn("not-json");

        FinancialPredictionResponse response = financialPredictionService.generatePrediction(new FinancialPredictionRequest());

        assertThat(response.getSummary()).contains("could not be parsed");
        assertThat(response.getForecast()).isEmpty();
        assertThat(response.getRawModelResponse()).isEqualTo("not-json");
    }
}
