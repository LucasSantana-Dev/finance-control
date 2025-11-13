package com.finance_control.goals.controller;

import com.finance_control.shared.enums.GoalType;
import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.goals.dto.GoalCompletionRequest;
import com.finance_control.goals.service.FinancialGoalService;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import com.finance_control.shared.security.CustomUserDetails;
import com.finance_control.shared.context.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinancialGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinancialGoalService financialGoalService;

    @MockitoBean
    private FinancialGoalRepository financialGoalRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TransactionSourceRepository transactionSourceRepository;

    @Autowired
    private ObjectMapper objectMapper;

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


    @Test
    void getFinancialGoals_WithValidParameters_ShouldReturnOk() throws Exception {
        // Mock the service to return our sample data
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
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
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
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
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
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
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
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
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
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

        mockMvc.perform(get("/api/financial-goals/metadata/types")
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

        mockMvc.perform(get("/api/financial-goals/metadata/status-summary")
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

        mockMvc.perform(get("/api/financial-goals/metadata/progress-summary")
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

        mockMvc.perform(get("/api/financial-goals/metadata/deadline-alerts")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Goal 1"))
                .andExpect(jsonPath("$[0].deadline").value("2024-02-01"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Goal 2"))
                .andExpect(jsonPath("$[1].deadline").value("2024-02-15"));
    }

    @Test
    void getFinancialGoals_WithCompletionRateData_ShouldReturnOk() throws Exception {
        Map<String, Object> completionRate = Map.of("completionRate", 75.5, "totalGoals", 10, "completedGoals", 7);
        when(financialGoalService.getCompletionRate(anyLong()))
                .thenReturn(completionRate);

        mockMvc.perform(get("/api/financial-goals/metadata/completion-rate")
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

        mockMvc.perform(get("/api/financial-goals/metadata/average-completion-time")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageDays").value(120.5))
                .andExpect(jsonPath("$.totalCompleted").value(5));
    }

    @Test
    void getFinancialGoalsMetadata_WithInvalidData_ShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/api/financial-goals/metadata/invalid-type")
                .with(user(testUserDetails)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getActiveGoals_ShouldReturnActiveGoals() throws Exception {
        List<FinancialGoalDTO> activeGoals = Arrays.asList(sampleGoal);
        when(financialGoalService.findActiveGoals()).thenReturn(activeGoals);

        mockMvc.perform(get("/api/financial-goals/active")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void getCompletedGoals_ShouldReturnCompletedGoals() throws Exception {
        FinancialGoalDTO completedGoal = new FinancialGoalDTO();
        completedGoal.setId(2L);
        completedGoal.setIsActive(false);
        List<FinancialGoalDTO> completedGoals = Arrays.asList(completedGoal);
        when(financialGoalService.findCompletedGoals()).thenReturn(completedGoals);

        mockMvc.perform(get("/api/financial-goals/completed")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].isActive").value(false));
    }

    @Test
    void findAllFiltered_WithValidParameters_ShouldReturnOk() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/filtered")
                .param("goalType", "SAVINGS")
                .param("status", "active")
                .param("minTargetAmount", "1000")
                .param("maxTargetAmount", "50000")
                .param("deadlineStart", "2024-01-01")
                .param("deadlineEnd", "2024-12-31")
                .param("isActive", "true")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "deadline")
                .param("sortDirection", "asc")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void findAllFiltered_WithStatusCompleted_ShouldReturnOk() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/filtered")
                .param("status", "completed")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void findAll_WithInvalidMinTargetAmount_ShouldHandleGracefully() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
                .param("minTargetAmount", "invalid-number")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void findAll_WithInvalidMaxTargetAmount_ShouldHandleGracefully() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
                .param("maxTargetAmount", "not-a-number")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void findAll_WithInvalidDeadlineStart_ShouldHandleGracefully() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
                .param("deadlineStart", "invalid-date")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void findAll_WithInvalidDeadlineEnd_ShouldHandleGracefully() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
                .param("deadlineEnd", "not-a-date")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void findAll_WithEmptyStatus_ShouldNotAddFilter() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
                .param("status", "")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void findAll_WithWhitespaceStatus_ShouldNotAddFilter() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals")
                .param("status", "   ")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void updateProgress_WithValidAmount_ShouldReturnOk() throws Exception {
        when(financialGoalService.updateProgress(1L, new BigDecimal("100.00")))
                .thenReturn(sampleGoal);

        mockMvc.perform(post("/api/financial-goals/1/progress")
                .param("amount", "100.00")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void markAsCompleted_ShouldReturnOk() throws Exception {
        FinancialGoalDTO completedGoal = new FinancialGoalDTO();
        completedGoal.setId(1L);
        completedGoal.setIsActive(false);
        when(financialGoalService.markAsCompleted(1L))
                .thenReturn(completedGoal);

        mockMvc.perform(post("/api/financial-goals/1/complete")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void reactivate_ShouldReturnOk() throws Exception {
        FinancialGoalDTO reactivatedGoal = new FinancialGoalDTO();
        reactivatedGoal.setId(1L);
        reactivatedGoal.setIsActive(true);
        when(financialGoalService.reactivate(1L))
                .thenReturn(reactivatedGoal);

        mockMvc.perform(post("/api/financial-goals/1/reactivate")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void completeGoal_WithValidRequest_ShouldReturnOk() throws Exception {
        GoalCompletionRequest request = new GoalCompletionRequest();
        request.setFinalAmount(new BigDecimal("10000.00"));
        request.setCompletionDate(LocalDateTime.now());
        request.setCompleted(true);

        FinancialGoalDTO completedGoal = new FinancialGoalDTO();
        completedGoal.setId(1L);
        completedGoal.setIsActive(false);
        when(financialGoalService.completeGoal(1L, request))
                .thenReturn(completedGoal);

        mockMvc.perform(put("/api/financial-goals/1/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void completeGoal_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        GoalCompletionRequest request = new GoalCompletionRequest();
        request.setFinalAmount(null);

        mockMvc.perform(put("/api/financial-goals/1/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(user(testUserDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllFiltered_WithAllFiltersCombined_ShouldReturnOk() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/filtered")
                .param("search", "savings")
                .param("goalType", "SAVINGS")
                .param("status", "active")
                .param("minTargetAmount", "1000")
                .param("maxTargetAmount", "50000")
                .param("deadlineStart", "2024-01-01")
                .param("deadlineEnd", "2024-12-31")
                .param("isActive", "true")
                .param("sortBy", "deadline")
                .param("sortDirection", "asc")
                .param("page", "0")
                .param("size", "20")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void findAllFiltered_WithPartialFilters_ShouldReturnOk() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/filtered")
                .param("goalType", "SAVINGS")
                .param("status", "active")
                .param("isActive", "true")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void findAllFiltered_WithOnlyAmountFilters_ShouldReturnOk() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/filtered")
                .param("minTargetAmount", "1000")
                .param("maxTargetAmount", "50000")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void findAllFiltered_WithOnlyDeadlineFilters_ShouldReturnOk() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/filtered")
                .param("deadlineStart", "2024-01-01")
                .param("deadlineEnd", "2024-12-31")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void findAllFiltered_WithEmptyStringFilters_ShouldNotAddFilters() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/filtered")
                .param("goalType", "")
                .param("status", "   ")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void findAllFiltered_WithNullSortBy_ShouldUseDefaultSort() throws Exception {
        when(financialGoalService.findAll(nullable(String.class), anyMap(), nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/financial-goals/filtered")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }
}
