package com.finance_control.dashboard.controller;

import com.finance_control.dashboard.dto.DashboardSummaryDTO;
import com.finance_control.dashboard.dto.FinancialMetricsDTO;
import com.finance_control.dashboard.dto.CategorySpendingDTO;
import com.finance_control.dashboard.dto.MonthlyTrendDTO;
import com.finance_control.dashboard.service.DashboardService;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.security.CustomUserDetails;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.users.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private FinancialGoalRepository financialGoalRepository;

    @MockitoBean
    private MetricsService metricsService;

    @Autowired
    private ObjectMapper objectMapper;

    private DashboardSummaryDTO sampleSummary;
    private FinancialMetricsDTO sampleMetrics;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        // Set up UserContext for the test thread
        UserContext.setCurrentUserId(1L);

        // Create a test user for authentication
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        testUserDetails = new CustomUserDetails(testUser);

        sampleSummary = DashboardSummaryDTO.builder()
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpenses(new BigDecimal("3000.00"))
                .netWorth(new BigDecimal("2000.00"))
                .monthlyBalance(new BigDecimal("2000.00"))
                .savingsRate(new BigDecimal("40.0"))
                .activeGoals(5)
                .completedGoals(3)
                .build();

        sampleMetrics = FinancialMetricsDTO.builder()
                .monthlyIncome(new BigDecimal("5000.00"))
                .monthlyExpenses(new BigDecimal("3000.00"))
                .monthlySavings(new BigDecimal("2000.00"))
                .savingsRate(new BigDecimal("40.0"))
                .build();
    }

    @AfterEach
    void tearDown() {
        // Clear UserContext after each test
        UserContext.clear();
    }

    @Test
    void getDashboardData_WithSummaryData_ShouldReturnOk() throws Exception {
        when(dashboardService.getDashboardSummary())
                .thenReturn(sampleSummary);

        mockMvc.perform(get("/api/dashboard")
                .param("data", "summary")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalIncome").value(5000.00))
                .andExpect(jsonPath("$.totalExpenses").value(3000.00))
                .andExpect(jsonPath("$.netWorth").value(2000.00))
                .andExpect(jsonPath("$.monthlyBalance").value(2000.00))
                .andExpect(jsonPath("$.activeGoals").value(5))
                .andExpect(jsonPath("$.completedGoals").value(3));
    }

    @Test
    void getDashboardData_WithMetricsData_ShouldReturnOk() throws Exception {
        when(dashboardService.getFinancialMetrics(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(sampleMetrics);

        mockMvc.perform(get("/api/dashboard")
                .param("data", "metrics")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.monthlyIncome").value(5000.00))
                .andExpect(jsonPath("$.monthlyExpenses").value(3000.00))
                .andExpect(jsonPath("$.monthlySavings").value(2000.00))
                .andExpect(jsonPath("$.savingsRate").value(40.0));
    }

    @Test
    void getDashboardData_WithMetricsDataWithoutDates_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                .param("data", "metrics")
                .with(user(testUserDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDashboardData_WithSpendingCategoriesData_ShouldReturnOk() throws Exception {
        List<CategorySpendingDTO> spendingCategories = Arrays.asList(
                CategorySpendingDTO.builder()
                        .categoryName("Food & Dining")
                        .amount(new BigDecimal("800.00"))
                        .percentage(new BigDecimal("26.7"))
                        .build(),
                CategorySpendingDTO.builder()
                        .categoryName("Transportation")
                        .amount(new BigDecimal("600.00"))
                        .percentage(new BigDecimal("20.0"))
                        .build()
        );
        when(dashboardService.getTopSpendingCategories(anyLong(), anyInt()))
                .thenReturn(spendingCategories);

        mockMvc.perform(get("/api/dashboard")
                .param("data", "spending-categories")
                .param("limit", "10")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].categoryName").value("Food & Dining"))
                .andExpect(jsonPath("$[0].amount").value(800.00))
                .andExpect(jsonPath("$[0].percentage").value(26.7));
    }

    @Test
    void getDashboardData_WithMonthlyTrendsData_ShouldReturnOk() throws Exception {
        List<MonthlyTrendDTO> monthlyTrends = Arrays.asList(
                MonthlyTrendDTO.builder()
                        .month(LocalDate.of(2024, 1, 1))
                        .income(new BigDecimal("5000.00"))
                        .expenses(new BigDecimal("3000.00"))
                        .build(),
                MonthlyTrendDTO.builder()
                        .month(LocalDate.of(2024, 2, 1))
                        .income(new BigDecimal("5200.00"))
                        .expenses(new BigDecimal("2800.00"))
                        .build()
        );
        when(dashboardService.getMonthlyTrends(anyLong(), anyInt()))
                .thenReturn(monthlyTrends);

        mockMvc.perform(get("/api/dashboard")
                .param("data", "monthly-trends")
                .param("months", "12")
                .with(user(testUserDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].month").value("2024-01-01"))
                .andExpect(jsonPath("$[0].income").value(5000.00))
                .andExpect(jsonPath("$[0].expenses").value(3000.00));
    }

    @Test
    void getDashboardData_WithCurrentMonthMetricsData_ShouldReturnOk() throws Exception {
        when(dashboardService.getFinancialMetrics(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(sampleMetrics);

        mockMvc.perform(get("/api/dashboard")
                .param("data", "current-month-metrics")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.monthlyIncome").value(5000.00))
                .andExpect(jsonPath("$.monthlyExpenses").value(3000.00))
                .andExpect(jsonPath("$.monthlySavings").value(2000.00));
    }

    @Test
    void getDashboardData_WithYearToDateMetricsData_ShouldReturnOk() throws Exception {
        when(dashboardService.getFinancialMetrics(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(sampleMetrics);

        mockMvc.perform(get("/api/dashboard")
                .param("data", "year-to-date-metrics")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.monthlyIncome").value(5000.00))
                .andExpect(jsonPath("$.monthlyExpenses").value(3000.00))
                .andExpect(jsonPath("$.monthlySavings").value(2000.00));
    }

    @Test
    void getDashboardData_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                .param("data", "invalid-type")
                .with(user(testUserDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDashboardSummary_ShouldReturnOk() throws Exception {
        when(dashboardService.getDashboardSummary())
                .thenReturn(sampleSummary);

        mockMvc.perform(get("/api/dashboard/summary")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalIncome").value(5000.00))
                .andExpect(jsonPath("$.totalExpenses").value(3000.00))
                .andExpect(jsonPath("$.netWorth").value(2000.00));
    }

    @Test
    void getFinancialMetrics_WithValidDates_ShouldReturnOk() throws Exception {
        when(dashboardService.getFinancialMetrics(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))
                .thenReturn(sampleMetrics);

        mockMvc.perform(get("/api/dashboard/metrics")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.monthlyIncome").value(5000.00))
                .andExpect(jsonPath("$.monthlyExpenses").value(3000.00));
    }

    @Test
    void getTopSpendingCategories_WithDefaultLimit_ShouldReturnOk() throws Exception {
        List<CategorySpendingDTO> categories = Arrays.asList(
                CategorySpendingDTO.builder()
                        .categoryName("Food")
                        .amount(new BigDecimal("500.00"))
                        .percentage(new BigDecimal("50.0"))
                        .build()
        );
        when(dashboardService.getTopSpendingCategories(anyLong(), eq(5)))
                .thenReturn(categories);

        mockMvc.perform(get("/api/dashboard/spending-categories")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].categoryName").value("Food"));
    }

    @Test
    void getMonthlyTrends_WithDefaultMonths_ShouldReturnOk() throws Exception {
        List<MonthlyTrendDTO> trends = Arrays.asList(
                MonthlyTrendDTO.builder()
                        .month(LocalDate.of(2024, 1, 1))
                        .income(new BigDecimal("5000.00"))
                        .expenses(new BigDecimal("3000.00"))
                        .build()
        );
        when(dashboardService.getMonthlyTrends(anyLong(), eq(12)))
                .thenReturn(trends);

        mockMvc.perform(get("/api/dashboard/monthly-trends")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getCurrentMonthMetrics_ShouldReturnOk() throws Exception {
        when(dashboardService.getFinancialMetrics(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(sampleMetrics);

        mockMvc.perform(get("/api/dashboard/current-month-metrics")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.monthlyIncome").value(5000.00));
    }

    @Test
    void getYearToDateMetrics_ShouldReturnOk() throws Exception {
        when(dashboardService.getFinancialMetrics(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(sampleMetrics);

        mockMvc.perform(get("/api/dashboard/year-to-date-metrics")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.monthlyIncome").value(5000.00));
    }
}
