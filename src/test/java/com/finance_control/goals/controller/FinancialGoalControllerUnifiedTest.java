package com.finance_control.goals.controller;

import com.finance_control.shared.enums.GoalType;
import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.goals.service.FinancialGoalService;
import com.finance_control.goals.controller.FinancialGoalController;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import com.finance_control.shared.security.CustomUserDetails;
import com.finance_control.shared.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mockStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinancialGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinancialGoalService financialGoalService;

    @MockBean
    private FinancialGoalRepository financialGoalRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransactionSourceRepository transactionSourceRepository;

    private FinancialGoalDTO sampleGoal;
    private Page<FinancialGoalDTO> samplePage;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        // Set up UserContext for the test thread
        UserContext.setCurrentUserId(1L);
        System.out.println("UserContext set to: " + UserContext.getCurrentUserId());

        // Create a test user for authentication
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        testUserDetails = new CustomUserDetails(testUser);

        sampleGoal = new FinancialGoalDTO();
        sampleGoal.setId(1L);
        sampleGoal.setName("Test Goal");
        sampleGoal.setDescription("Test Description");
        sampleGoal.setGoalType(GoalType.SAVINGS);
        sampleGoal.setTargetAmount(new BigDecimal("10000.00"));
        sampleGoal.setCurrentAmount(new BigDecimal("5000.00"));
        sampleGoal.setDeadline(LocalDateTime.now().plusMonths(6));
        sampleGoal.setIsActive(true);
        sampleGoal.setCreatedAt(LocalDateTime.now());

        List<FinancialGoalDTO> goals = Arrays.asList(sampleGoal);
        samplePage = new PageImpl<>(goals, PageRequest.of(0, 20), 1);
    }

    @AfterEach
    void tearDown() {
        // Clear UserContext after each test
        UserContext.clear();
    }

    private com.finance_control.goals.model.FinancialGoal createTestGoal() {
        com.finance_control.goals.model.FinancialGoal goal = new com.finance_control.goals.model.FinancialGoal();
        goal.setId(1L);
        goal.setName("Test Goal");
        goal.setDescription("Test Description");
        goal.setGoalType(GoalType.SAVINGS);
        goal.setTargetAmount(new BigDecimal("10000.00"));
        goal.setCurrentAmount(new BigDecimal("5000.00"));
        goal.setDeadline(LocalDate.now().plusMonths(6));
        goal.setIsActive(true);
        goal.setCreatedAt(LocalDateTime.now());
        return goal;
    }

    @Test
    void getFinancialGoals_WithValidParameters_ShouldReturnOk() throws Exception {
        // Mock the service to return our sample data
        when(financialGoalService.findAll(anyString(), any(Map.class), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .param("goalType", "SAVINGS")
                .param("status", "active")
                .param("sortBy", "deadline")
                .param("sortDirection", "asc")
                .param("page", "0")
                .param("size", "20")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Goal"))
                .andExpect(jsonPath("$.content[0].goalType").value("SAVINGS"))
                .andExpect(jsonPath("$.content[0].targetAmount").value(10000.00))
                .andExpect(jsonPath("$.content[0].currentAmount").value(5000.00))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getFinancialGoals_WithSearchParameter_ShouldReturnFilteredResults() throws Exception {
        when(financialGoalService.findAll(anyString(), any(Map.class), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .param("search", "vacation")
                .param("page", "0")
                .param("size", "20")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getFinancialGoals_WithAmountRange_ShouldReturnFilteredResults() throws Exception {
        when(financialGoalService.findAll(anyString(), any(Map.class), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .param("minTargetAmount", "1000.00")
                .param("maxTargetAmount", "50000.00")
                .param("page", "0")
                .param("size", "20")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getFinancialGoals_WithDeadlineRange_ShouldReturnFilteredResults() throws Exception {
        when(financialGoalService.findAll(anyString(), any(Map.class), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .param("deadlineStart", "2024-01-01")
                .param("deadlineEnd", "2024-12-31")
                .param("page", "0")
                .param("size", "20")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getFinancialGoals_WithDefaultParameters_ShouldReturnOk() throws Exception {
        when(financialGoalService.findAll(anyString(), any(Map.class), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getFinancialGoalsMetadata_WithTypesData_ShouldReturnOk() throws Exception {
        when(financialGoalService.getGoalTypes())
                .thenReturn(Arrays.asList("SAVINGS", "INVESTMENT", "DEBT_PAYOFF"));

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("data", "types")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("SAVINGS"))
                .andExpect(jsonPath("$[1]").value("INVESTMENT"))
                .andExpect(jsonPath("$[2]").value("DEBT_PAYOFF"));
    }

    @Test
    void getFinancialGoalsMetadata_WithStatusSummaryData_ShouldReturnOk() throws Exception {
        Map<String, Object> statusSummary = Map.of(
                "active", 5,
                "completed", 3,
                "total", 8
        );
        when(financialGoalService.getStatusSummary(anyLong()))
                .thenReturn(statusSummary);

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .param("data", "status-summary")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.active").value(5))
                .andExpect(jsonPath("$.completed").value(3))
                .andExpect(jsonPath("$.total").value(8));
    }

    @Test
    void getFinancialGoalsMetadata_WithProgressSummaryData_ShouldReturnOk() throws Exception {
        Map<String, Object> progressSummary = Map.of(
                "onTrack", 3,
                "behind", 2,
                "completed", 1
        );
        when(financialGoalService.getProgressSummary(anyLong()))
                .thenReturn(progressSummary);

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .param("data", "progress-summary")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.onTrack").value(3))
                .andExpect(jsonPath("$.behind").value(2))
                .andExpect(jsonPath("$.completed").value(1));
    }

    @Test
    void getFinancialGoalsMetadata_WithDeadlineAlertsData_ShouldReturnOk() throws Exception {
        List<Map<String, Object>> deadlineAlerts = Arrays.asList(
                Map.of("id", 1L, "name", "Goal 1", "deadline", "2024-02-01"),
                Map.of("id", 2L, "name", "Goal 2", "deadline", "2024-02-15")
        );
        when(financialGoalService.getDeadlineAlerts(anyLong()))
                .thenReturn(deadlineAlerts);

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .param("data", "deadline-alerts")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Goal 1"))
                .andExpect(jsonPath("$[0].deadline").value("2024-02-01"));
    }

    @Test
    void getFinancialGoals_WithCompletionRateData_ShouldReturnOk() throws Exception {
        Map<String, Object> completionRate = Map.of("completionRate", 75.5, "totalGoals", 10, "completedGoals", 7);
        when(financialGoalService.getCompletionRate(anyLong()))
                .thenReturn(completionRate);

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .param("data", "completion-rate")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.completionRate").value(75.5))
                .andExpect(jsonPath("$.totalGoals").value(10))
                .andExpect(jsonPath("$.completedGoals").value(7));
    }

    @Test
    void getFinancialGoals_WithAverageCompletionTimeData_ShouldReturnOk() throws Exception {
        Map<String, Object> avgCompletionTime = Map.of("averageDays", 120.5, "totalCompleted", 5);
        when(financialGoalService.getAverageCompletionTime(anyLong()))
                .thenReturn(avgCompletionTime);

        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .param("data", "average-completion-time")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageDays").value(120.5))
                .andExpect(jsonPath("$.totalCompleted").value(5));
    }

    @Test
    void getFinancialGoalsMetadata_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/financial-goals/unified")
                .param("userId", "1")
                .param("data", "invalid-type")
                .with(user(testUserDetails)))
                .andExpect(status().isBadRequest());
    }
}
