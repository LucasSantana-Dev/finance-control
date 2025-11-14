package com.finance_control.unit.goals.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.goals.controller.FinancialGoalController;
import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.goals.dto.GoalCompletionRequest;
import com.finance_control.goals.service.FinancialGoalService;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.exception.GlobalExceptionHandler;
import com.finance_control.shared.monitoring.SentryService;
import com.finance_control.users.model.User;
import com.finance_control.shared.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FinancialGoalControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private FinancialGoalService financialGoalService;

    @Mock
    private SentryService sentryService;

    @InjectMocks
    private FinancialGoalController financialGoalController;

    private User testUser;
    private CustomUserDetails testUserDetails;
    private FinancialGoalDTO testGoalDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Create custom argument resolver for @AuthenticationPrincipal
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(CustomUserDetails.class) &&
                       parameter.hasParameterAnnotation(org.springframework.security.core.annotation.AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testUserDetails;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(financialGoalController)
                        .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver(), authPrincipalResolver)
                        .setControllerAdvice(new GlobalExceptionHandler(sentryService))
                        .build();

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");

        testUserDetails = new CustomUserDetails(testUser);

        // Setup test goal DTO
        testGoalDTO = new FinancialGoalDTO();
        testGoalDTO.setId(1L);
        testGoalDTO.setName("Test Goal");
        testGoalDTO.setDescription("Test Description");
        testGoalDTO.setTargetAmount(BigDecimal.valueOf(1000.00));
        testGoalDTO.setCurrentAmount(BigDecimal.valueOf(500.00));
        testGoalDTO.setIsActive(true);
        // Note: progress is calculated in service layer, not stored in DTO
        testGoalDTO.setCreatedAt(LocalDateTime.now());
        testGoalDTO.setUpdatedAt(LocalDateTime.now());

        pageable = PageRequest.of(0, 20);
    }

    @Test
    void shouldGetAllGoals_WithDefaultParameters_ShouldReturnOk() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(any(), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        // When & Then
        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Test Goal"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));

        verify(financialGoalService).findAll(any(), anyMap(), any(), any(), any());
    }

    @Test
    void shouldGetAllGoals_WithSearchParameter_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(eq("savings"), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        // When & Then
        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .param("search", "savings")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));

        verify(financialGoalService).findAll(eq("savings"), anyMap(), any(), any(), any());
    }

    @Test
    void shouldGetAllGoals_WithGoalTypeFilter_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(any(), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        // When & Then
        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .param("goalType", "SAVINGS")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));

        verify(financialGoalService).findAll(any(), anyMap(), any(), any(), any());
    }

    @Test
    void shouldGetAllGoals_WithStatusFilter_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(any(), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        // When & Then
        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .param("status", "active")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));

        verify(financialGoalService).findAll(any(), anyMap(), any(), any(), any());
    }

    @Test
    void shouldGetAllGoals_WithAmountRangeFilters_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(any(), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        // When & Then
        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .param("minTargetAmount", "500.00")
                .param("maxTargetAmount", "2000.00")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));

        verify(financialGoalService).findAll(any(), anyMap(), any(), any(), any());
    }

    @Test
    void shouldGetAllGoals_WithInvalidAmountFilter_ShouldIgnoreInvalidFilter() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(any(), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        // When & Then
        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .param("minTargetAmount", "invalid-amount")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));

        verify(financialGoalService).findAll(any(), anyMap(), any(), any(), any());
    }

    @Test
    void shouldGetAllGoalsFiltered_WithAllParameters_ShouldReturnFilteredResults() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(any(), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        // When & Then
        mockMvc.perform(get("/financial-goals/filtered")
                .with(user(testUserDetails))
                .param("search", "emergency")
                .param("goalType", "SAVINGS")
                .param("status", "active")
                .param("minTargetAmount", "1000.00")
                .param("maxTargetAmount", "5000.00")
                .param("deadlineStart", "2024-01-01")
                .param("deadlineEnd", "2024-12-31")
                .param("isActive", "true")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDirection", "desc")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));

        verify(financialGoalService).findAll(any(), anyMap(), any(), any(), any());
    }

    @Test
    void shouldGetActiveGoals_ShouldReturnOnlyActiveGoals() throws Exception {
        // Given
        List<FinancialGoalDTO> activeGoals = List.of(testGoalDTO);

        when(financialGoalService.findActiveGoals()).thenReturn(activeGoals);

        // When & Then
        mockMvc.perform(get("/financial-goals/active")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Test Goal"))
            .andExpect(jsonPath("$[0].isActive").value(true));

        verify(financialGoalService).findActiveGoals();
    }

    @Test
    void shouldGetCompletedGoals_ShouldReturnOnlyCompletedGoals() throws Exception {
        // Given
        FinancialGoalDTO completedGoal = new FinancialGoalDTO();
        completedGoal.setId(2L);
        completedGoal.setName("Completed Goal");
        completedGoal.setIsActive(false);

        List<FinancialGoalDTO> completedGoals = List.of(completedGoal);

        when(financialGoalService.findCompletedGoals()).thenReturn(completedGoals);

        // When & Then
        mockMvc.perform(get("/financial-goals/completed")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].name").value("Completed Goal"))
            .andExpect(jsonPath("$[0].isActive").value(false));

        verify(financialGoalService).findCompletedGoals();
    }

    @Test
    void shouldUpdateProgress_WithValidAmount_ShouldReturnUpdatedGoal() throws Exception {
        // Given
        FinancialGoalDTO updatedGoal = new FinancialGoalDTO();
        updatedGoal.setId(1L);
        updatedGoal.setName("Test Goal");
        updatedGoal.setCurrentAmount(BigDecimal.valueOf(750.00));
        // Note: progress is calculated in service layer

        when(financialGoalService.updateProgress(eq(1L), any(BigDecimal.class)))
            .thenReturn(updatedGoal);

        // When & Then
        mockMvc.perform(post("/financial-goals/{id}/progress", 1L)
                .with(user(testUserDetails))
                .param("amount", "250.00")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.currentAmount").value(750.00));

        verify(financialGoalService).updateProgress(eq(1L), any(BigDecimal.class));
    }

    @Test
    void shouldMarkAsCompleted_WithValidId_ShouldReturnCompletedGoal() throws Exception {
        // Given
        FinancialGoalDTO completedGoal = new FinancialGoalDTO();
        completedGoal.setId(1L);
        completedGoal.setName("Test Goal");
        completedGoal.setIsActive(false);

        when(financialGoalService.markAsCompleted(1L)).thenReturn(completedGoal);

        // When & Then
        mockMvc.perform(post("/financial-goals/{id}/complete", 1L)
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.isActive").value(false));

        verify(financialGoalService).markAsCompleted(1L);
    }

    @Test
    void shouldReactivate_WithValidId_ShouldReturnReactivatedGoal() throws Exception {
        // Given
        FinancialGoalDTO reactivatedGoal = new FinancialGoalDTO();
        reactivatedGoal.setId(1L);
        reactivatedGoal.setName("Test Goal");
        reactivatedGoal.setIsActive(true);

        when(financialGoalService.reactivate(1L)).thenReturn(reactivatedGoal);

        // When & Then
        mockMvc.perform(post("/financial-goals/{id}/reactivate", 1L)
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.isActive").value(true));

        verify(financialGoalService).reactivate(1L);
    }

    @Test
    void shouldCompleteGoal_WithValidRequest_ShouldReturnCompletedGoal() throws Exception {
        // Given
        GoalCompletionRequest request = new GoalCompletionRequest();
        request.setCompletionNotes("Goal achieved successfully");
        request.setFinalAmount(BigDecimal.valueOf(1000.00));
        request.setCompletionDate(LocalDateTime.now());
        request.setCompleted(true);

        FinancialGoalDTO completedGoal = new FinancialGoalDTO();
        completedGoal.setId(1L);
        completedGoal.setName("Test Goal");
        completedGoal.setIsActive(false);

        when(financialGoalService.completeGoal(eq(1L), any()))
            .thenReturn(completedGoal);

        // When & Then
        mockMvc.perform(put("/financial-goals/{id}/complete", 1L)
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.isActive").value(false));

        verify(financialGoalService).completeGoal(eq(1L), any());
    }

    @Test
    void shouldCompleteGoal_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - invalid request (missing required fields)

        // When & Then
        mockMvc.perform(put("/financial-goals/{id}/complete", 1L)
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest()); // Validation should fail

        verify(financialGoalService, never()).completeGoal(eq(1L), any());
    }

    @Test
    void shouldGetGoalTypes_ShouldReturnListOfTypes() throws Exception {
        // Given
        List<String> goalTypes = List.of("SAVINGS", "INVESTMENT", "DEBT_PAYMENT");

        when(financialGoalService.getGoalTypes()).thenReturn(goalTypes);

        // When & Then
        mockMvc.perform(get("/financial-goals/metadata/types")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0]").value("SAVINGS"))
            .andExpect(jsonPath("$[1]").value("INVESTMENT"))
            .andExpect(jsonPath("$[2]").value("DEBT_PAYMENT"));

        verify(financialGoalService).getGoalTypes();
    }

    @Test
    void shouldGetStatusSummary_ShouldReturnStatusSummary() throws Exception {
        // Given
        Map<String, Object> statusSummary = Map.of(
            "totalGoals", 5,
            "activeGoals", 3,
            "completedGoals", 2,
            "completionRate", 40.0
        );

        when(financialGoalService.getStatusSummary(1L)).thenReturn(statusSummary);

        // When & Then
        mockMvc.perform(get("/financial-goals/metadata/status-summary")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalGoals").value(5))
            .andExpect(jsonPath("$.activeGoals").value(3))
            .andExpect(jsonPath("$.completedGoals").value(2))
            .andExpect(jsonPath("$.completionRate").value(40.0));

        verify(financialGoalService).getStatusSummary(1L);
    }

    @Test
    void shouldGetProgressSummary_ShouldReturnProgressSummary() throws Exception {
        // Given
        Map<String, Object> progressSummary = Map.of(
            "totalTargetAmount", 10000.0,
            "totalCurrentAmount", 4500.0,
            "overallProgress", 45.0,
            "goalsOnTrack", 2
        );

        when(financialGoalService.getProgressSummary(1L)).thenReturn(progressSummary);

        // When & Then
        mockMvc.perform(get("/financial-goals/metadata/progress-summary")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalTargetAmount").value(10000.0))
            .andExpect(jsonPath("$.totalCurrentAmount").value(4500.0))
            .andExpect(jsonPath("$.overallProgress").value(45.0))
            .andExpect(jsonPath("$.goalsOnTrack").value(2));

        verify(financialGoalService).getProgressSummary(1L);
    }

    @Test
    void shouldGetDeadlineAlerts_ShouldReturnDeadlineAlerts() throws Exception {
        // Given
        List<Map<String, Object>> deadlineAlerts = List.of(
            Map.of(
                "goalId", 1L,
                "goalName", "Emergency Fund",
                "daysUntilDeadline", 7,
                "progress", 85.0
            )
        );

        when(financialGoalService.getDeadlineAlerts(1L)).thenReturn(deadlineAlerts);

        // When & Then
        mockMvc.perform(get("/financial-goals/metadata/deadline-alerts")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].goalId").value(1))
            .andExpect(jsonPath("$[0].goalName").value("Emergency Fund"))
            .andExpect(jsonPath("$[0].daysUntilDeadline").value(7))
            .andExpect(jsonPath("$[0].progress").value(85.0));

        verify(financialGoalService).getDeadlineAlerts(1L);
    }

    @Test
    void shouldGetCompletionRate_ShouldReturnCompletionRate() throws Exception {
        // Given
        Map<String, Object> completionRate = Map.of(
            "completionRate", 60.0,
            "totalCompleted", 6,
            "totalGoals", 10,
            "averageCompletionTime", 45
        );

        when(financialGoalService.getCompletionRate(1L)).thenReturn(completionRate);

        // When & Then
        mockMvc.perform(get("/financial-goals/metadata/completion-rate")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.completionRate").value(60.0))
            .andExpect(jsonPath("$.totalCompleted").value(6))
            .andExpect(jsonPath("$.totalGoals").value(10))
            .andExpect(jsonPath("$.averageCompletionTime").value(45));

        verify(financialGoalService).getCompletionRate(1L);
    }

    @Test
    void shouldGetAverageCompletionTime_ShouldReturnAverageCompletionTime() throws Exception {
        // Given
        Map<String, Object> avgCompletionTime = Map.of(
            "averageDays", 42,
            "averageMonths", 1.4,
            "totalCompletedGoals", 8,
            "fastestCompletion", 15,
            "slowestCompletion", 120
        );

        when(financialGoalService.getAverageCompletionTime(1L)).thenReturn(avgCompletionTime);

        // When & Then
        mockMvc.perform(get("/financial-goals/metadata/average-completion-time")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.averageDays").value(42))
            .andExpect(jsonPath("$.averageMonths").value(1.4))
            .andExpect(jsonPath("$.totalCompletedGoals").value(8))
            .andExpect(jsonPath("$.fastestCompletion").value(15))
            .andExpect(jsonPath("$.slowestCompletion").value(120));

        verify(financialGoalService).getAverageCompletionTime(1L);
    }

    // Error handling tests

    @Test
    void shouldUpdateProgress_WithInvalidAmount_ShouldReturnBadRequest() throws Exception {
        // Given
        when(financialGoalService.updateProgress(eq(1L), any(BigDecimal.class)))
            .thenThrow(new IllegalArgumentException("Amount must be positive"));

        // When & Then
        mockMvc.perform(post("/financial-goals/{id}/progress", 1L)
                .with(user(testUserDetails))
                .param("amount", "-100.00")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Amount must be positive"));

        verify(financialGoalService).updateProgress(eq(1L), any(BigDecimal.class));
    }

    @Test
    void shouldUpdateProgress_WithNonexistentGoal_ShouldReturnNotFound() throws Exception {
        // Given
        when(financialGoalService.updateProgress(eq(999L), any(BigDecimal.class)))
            .thenThrow(new EntityNotFoundException("Goal not found with id: 999"));

        // When & Then
        mockMvc.perform(post("/financial-goals/{id}/progress", 999L)
                .with(user(testUserDetails))
                .param("amount", "100.00")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Goal not found with id: 999"));

        verify(financialGoalService).updateProgress(eq(999L), any(BigDecimal.class));
    }

    @Test
    void shouldCompleteGoal_WithNonexistentGoal_ShouldReturnNotFound() throws Exception {
        // Given
        GoalCompletionRequest request = new GoalCompletionRequest();
        request.setCompletionNotes("Completed");
        request.setFinalAmount(BigDecimal.valueOf(1000.00));
        request.setCompletionDate(LocalDateTime.now());
        request.setCompleted(true);

        when(financialGoalService.completeGoal(eq(999L), any(GoalCompletionRequest.class)))
            .thenThrow(new EntityNotFoundException("Goal not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/financial-goals/{id}/complete", 999L)
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Goal not found with id: 999"));

        verify(financialGoalService).completeGoal(eq(999L), any(GoalCompletionRequest.class));
    }

    // Pagination tests

    @Test
    void shouldGetAllGoals_WithPagination_ShouldReturnPagedResults() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, PageRequest.of(1, 5), 15);

        when(financialGoalService.findAll(any(), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        // When & Then
        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .param("page", "1")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(3))
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.size").value(5));

        verify(financialGoalService).findAll(any(), anyMap(), any(), any(), any());
    }

    @Test
    void shouldGetAllGoals_WithSorting_ShouldReturnSortedResults() throws Exception {
        // Given
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(any(), anyMap(), eq("name"), eq("desc"), any()))
            .thenReturn(goalPage);

        // When & Then
        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .param("sortBy", "name")
                .param("sortDirection", "desc")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));

        verify(financialGoalService).findAll(any(), anyMap(), eq("name"), eq("desc"), any());
    }

    // Additional error handling tests

    @Test
    @DisplayName("updateProgress_WithZeroAmount_ShouldReturnBadRequest")
    void updateProgress_WithZeroAmount_ShouldReturnBadRequest() throws Exception {
        when(financialGoalService.updateProgress(eq(1L), any(BigDecimal.class)))
            .thenThrow(new IllegalArgumentException("Amount must be positive"));

        mockMvc.perform(post("/financial-goals/{id}/progress", 1L)
                .with(user(testUserDetails))
                .param("amount", "0.00")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Amount must be positive"));

        verify(financialGoalService).updateProgress(eq(1L), any(BigDecimal.class));
    }

    @Test
    @DisplayName("updateProgress_WithNullAmount_ShouldReturnError")
    void updateProgress_WithNullAmount_ShouldReturnError() throws Exception {
        // When amount parameter is missing, Spring tries to convert null to BigDecimal
        // This causes a type conversion error (500) rather than validation error (400)
        // because the parameter is required but null conversion fails
        mockMvc.perform(post("/financial-goals/{id}/progress", 1L)
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is5xxServerError()); // Type conversion error for null BigDecimal
    }

    @Test
    @DisplayName("markAsCompleted_WithNonexistentGoal_ShouldReturnNotFound")
    void markAsCompleted_WithNonexistentGoal_ShouldReturnNotFound() throws Exception {
        when(financialGoalService.markAsCompleted(999L))
            .thenThrow(new EntityNotFoundException("Goal not found with id: 999"));

        mockMvc.perform(post("/financial-goals/{id}/complete", 999L)
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Goal not found with id: 999"));

        verify(financialGoalService).markAsCompleted(999L);
    }

    @Test
    @DisplayName("reactivate_WithNonexistentGoal_ShouldReturnNotFound")
    void reactivate_WithNonexistentGoal_ShouldReturnNotFound() throws Exception {
        when(financialGoalService.reactivate(999L))
            .thenThrow(new EntityNotFoundException("Goal not found with id: 999"));

        mockMvc.perform(post("/financial-goals/{id}/reactivate", 999L)
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Goal not found with id: 999"));

        verify(financialGoalService).reactivate(999L);
    }

    @Test
    @DisplayName("completeGoal_WithInvalidFinalAmount_ShouldReturnBadRequest")
    void completeGoal_WithInvalidFinalAmount_ShouldReturnBadRequest() throws Exception {
        GoalCompletionRequest request = new GoalCompletionRequest();
        request.setFinalAmount(BigDecimal.valueOf(-100.00));
        request.setCompletionDate(LocalDateTime.now());
        request.setCompleted(true);

        // The validation might happen at DTO level or service level
        // Negative amount might pass DTO validation but fail service validation
        // Let's test that service is called and throws exception, or validation catches it
        when(financialGoalService.completeGoal(eq(1L), any(GoalCompletionRequest.class)))
            .thenThrow(new IllegalArgumentException("Final amount must be positive"));

        mockMvc.perform(put("/financial-goals/{id}/complete", 1L)
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Service validation or exception handler
    }

    @Test
    @DisplayName("completeGoal_WithNullFinalAmount_ShouldReturnBadRequest")
    void completeGoal_WithNullFinalAmount_ShouldReturnBadRequest() throws Exception {
        GoalCompletionRequest request = new GoalCompletionRequest();
        request.setFinalAmount(null);
        request.setCompletionDate(LocalDateTime.now());
        request.setCompleted(true);

        mockMvc.perform(put("/financial-goals/{id}/complete", 1L)
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(financialGoalService, never()).completeGoal(eq(1L), any());
    }

    @Test
    @DisplayName("getStatusSummary_WithServiceException_ShouldReturnInternalServerError")
    void getStatusSummary_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        when(financialGoalService.getStatusSummary(1L))
            .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/financial-goals/metadata/status-summary")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(financialGoalService).getStatusSummary(1L);
    }

    @Test
    @DisplayName("getProgressSummary_WithServiceException_ShouldReturnInternalServerError")
    void getProgressSummary_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        when(financialGoalService.getProgressSummary(1L))
            .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/financial-goals/metadata/progress-summary")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(financialGoalService).getProgressSummary(1L);
    }

    @Test
    @DisplayName("getDeadlineAlerts_WithEmptyList_ShouldReturnEmptyArray")
    void getDeadlineAlerts_WithEmptyList_ShouldReturnEmptyArray() throws Exception {
        when(financialGoalService.getDeadlineAlerts(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/financial-goals/metadata/deadline-alerts")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));

        verify(financialGoalService).getDeadlineAlerts(1L);
    }

    @Test
    @DisplayName("getCompletionRate_WithNoGoals_ShouldReturnZeroRate")
    void getCompletionRate_WithNoGoals_ShouldReturnZeroRate() throws Exception {
        Map<String, Object> completionRate = Map.of(
            "completionRate", 0.0,
            "totalCompleted", 0,
            "totalGoals", 0,
            "averageCompletionTime", 0
        );

        when(financialGoalService.getCompletionRate(1L)).thenReturn(completionRate);

        mockMvc.perform(get("/financial-goals/metadata/completion-rate")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completionRate").value(0.0))
            .andExpect(jsonPath("$.totalGoals").value(0));

        verify(financialGoalService).getCompletionRate(1L);
    }

    @Test
    @DisplayName("getAverageCompletionTime_WithNoCompletedGoals_ShouldReturnZero")
    void getAverageCompletionTime_WithNoCompletedGoals_ShouldReturnZero() throws Exception {
        Map<String, Object> avgCompletionTime = Map.of(
            "averageDays", 0,
            "averageMonths", 0.0,
            "totalCompletedGoals", 0,
            "fastestCompletion", 0,
            "slowestCompletion", 0
        );

        when(financialGoalService.getAverageCompletionTime(1L)).thenReturn(avgCompletionTime);

        mockMvc.perform(get("/financial-goals/metadata/average-completion-time")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCompletedGoals").value(0))
            .andExpect(jsonPath("$.averageDays").value(0));

        verify(financialGoalService).getAverageCompletionTime(1L);
    }

    @Test
    @DisplayName("getActiveGoals_WithEmptyList_ShouldReturnEmptyArray")
    void getActiveGoals_WithEmptyList_ShouldReturnEmptyArray() throws Exception {
        when(financialGoalService.findActiveGoals()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/financial-goals/active")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));

        verify(financialGoalService).findActiveGoals();
    }

    @Test
    @DisplayName("getCompletedGoals_WithEmptyList_ShouldReturnEmptyArray")
    void getCompletedGoals_WithEmptyList_ShouldReturnEmptyArray() throws Exception {
        when(financialGoalService.findCompletedGoals()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/financial-goals/completed")
                .with(user(testUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));

        verify(financialGoalService).findCompletedGoals();
    }

    @Test
    @DisplayName("getAllGoals_WithInvalidPageNumber_ShouldHandleGracefully")
    void getAllGoals_WithInvalidPageNumber_ShouldHandleGracefully() throws Exception {
        // Spring may handle negative page numbers differently - could return 400 or default to 0
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(any(), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        // Spring might normalize negative page to 0 or return 400
        // Let's test that it doesn't crash - Spring typically normalizes negative pages to 0
        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .param("page", "-1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()); // Spring normalizes negative page to 0
    }

    @Test
    @DisplayName("getAllGoals_WithInvalidPageSize_ShouldHandleGracefully")
    void getAllGoals_WithInvalidPageSize_ShouldHandleGracefully() throws Exception {
        // Spring may handle zero or negative page size differently
        // In standalone MockMvc, Spring normalizes 0 to default size (20)
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(any(), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        // Spring normalizes 0 size to default (20), so it returns 200 OK
        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()); // Spring normalizes 0 to default size
    }

    @Test
    @DisplayName("getAllGoals_WithInvalidSortDirection_ShouldUseDefault")
    void getAllGoals_WithInvalidSortDirection_ShouldUseDefault() throws Exception {
        List<FinancialGoalDTO> goals = List.of(testGoalDTO);
        Page<FinancialGoalDTO> goalPage = new PageImpl<>(goals, pageable, 1);

        when(financialGoalService.findAll(any(), anyMap(), any(), any(), any()))
            .thenReturn(goalPage);

        mockMvc.perform(get("/financial-goals")
                .with(user(testUserDetails))
                .param("sortDirection", "invalid")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));

        verify(financialGoalService).findAll(any(), anyMap(), any(), any(), any());
    }
}
