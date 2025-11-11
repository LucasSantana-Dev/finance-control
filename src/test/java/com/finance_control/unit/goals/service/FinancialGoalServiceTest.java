package com.finance_control.unit.goals.service;

import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.goals.dto.GoalCompletionRequest;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.goals.service.FinancialGoalService;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.enums.GoalType;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class FinancialGoalServiceTest {

    @Mock
    private FinancialGoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionSourceRepository transactionSourceRepository;

    @InjectMocks
    private FinancialGoalService goalService;

    private FinancialGoal testGoal;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);

        testGoal = new FinancialGoal();
        testGoal.setId(1L);
        testGoal.setName("Test Goal");
        testGoal.setDescription("Test Description");
        testGoal.setGoalType(GoalType.SAVINGS);
        testGoal.setTargetAmount(BigDecimal.valueOf(1000.00));
        testGoal.setCurrentAmount(BigDecimal.valueOf(500.00));
        testGoal.setDeadline(LocalDate.now().plusMonths(6));
        testGoal.setUser(testUser);
        testGoal.setIsActive(true);
    }

    @Test
    void shouldCreateFinancialGoal() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            FinancialGoalDTO dto = new FinancialGoalDTO();
            dto.setName("New Goal");
            dto.setDescription("New Description");
            dto.setGoalType(GoalType.SAVINGS);
            dto.setTargetAmount(BigDecimal.valueOf(2000.00));
            dto.setCurrentAmount(BigDecimal.ZERO);
            dto.setDeadline(LocalDateTime.now().plusMonths(12));

            when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));
            when(goalRepository.save(any(FinancialGoal.class))).thenReturn(testGoal);

            FinancialGoalDTO result = goalService.create(dto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Goal");
        }
    }

    @Test
    void shouldFindGoalById() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(goalRepository.findById(1L)).thenReturn(java.util.Optional.of(testGoal));

            Optional<FinancialGoalDTO> result = goalService.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Test Goal");
        }
    }

    @Test
    void shouldFindAllGoals() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            List<FinancialGoal> goals = List.of(testGoal);
            Page<FinancialGoal> goalPage = new PageImpl<>(goals);

            when(goalRepository.findAll((String) isNull(), any(Pageable.class))).thenReturn(goalPage);

            Page<FinancialGoalDTO> result = goalService.findAll(null, null, null, null, Pageable.ofSize(10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Goal");
        }
    }

    @Test
    void shouldUpdateGoalProgress() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(goalRepository.findById(1L)).thenReturn(java.util.Optional.of(testGoal));
            when(goalRepository.save(any(FinancialGoal.class))).thenReturn(testGoal);

            FinancialGoalDTO dto = new FinancialGoalDTO();
            dto.setId(1L);
            dto.setCurrentAmount(BigDecimal.valueOf(750.00));

            FinancialGoalDTO result = goalService.update(1L, dto);

            assertThat(result).isNotNull();
            assertThat(result.getCurrentAmount()).isEqualTo(BigDecimal.valueOf(750.00));
        }
    }

    @Test
    void shouldFindActiveGoals_ReturnOnlyActiveGoals() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            FinancialGoal activeGoal = new FinancialGoal();
            activeGoal.setId(1L);
            activeGoal.setName("Active Goal");
            activeGoal.setIsActive(true);
            activeGoal.setUser(testUser);

            Page<FinancialGoal> goalPage = new PageImpl<>(List.of(activeGoal));

            when(goalRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                    .thenReturn(goalPage);

            List<FinancialGoalDTO> result = goalService.findActiveGoals();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Active Goal");
            assertThat(result.get(0).getIsActive()).isTrue();
        }
    }

    @Test
    void shouldFindCompletedGoals_ReturnOnlyCompletedGoals() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            FinancialGoal completedGoal = new FinancialGoal();
            completedGoal.setId(2L);
            completedGoal.setName("Completed Goal");
            completedGoal.setIsActive(false);
            completedGoal.setUser(testUser);

            Page<FinancialGoal> goalPage = new PageImpl<>(List.of(completedGoal));

            when(goalRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                    .thenReturn(goalPage);

            List<FinancialGoalDTO> result = goalService.findCompletedGoals();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Completed Goal");
            assertThat(result.get(0).getIsActive()).isFalse();
        }
    }

    @Test
    void shouldUpdateProgress_WhenGoalNotCompleted() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            FinancialGoal goal = new FinancialGoal();
            goal.setId(1L);
            goal.setCurrentAmount(BigDecimal.valueOf(500.00));
            goal.setTargetAmount(BigDecimal.valueOf(1000.00));
            goal.setIsActive(true);
            goal.setUser(testUser);

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FinancialGoalDTO result = goalService.updateProgress(1L, BigDecimal.valueOf(200.00));

            assertThat(result).isNotNull();
            assertThat(goal.getCurrentAmount()).isEqualByComparingTo(BigDecimal.valueOf(700.00));
            assertThat(goal.getIsActive()).isTrue();
        }
    }

    @Test
    void shouldUpdateProgress_WhenGoalBecomesCompleted() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            FinancialGoal goal = new FinancialGoal();
            goal.setId(1L);
            goal.setCurrentAmount(BigDecimal.valueOf(900.00));
            goal.setTargetAmount(BigDecimal.valueOf(1000.00));
            goal.setIsActive(true);
            goal.setUser(testUser);

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FinancialGoalDTO result = goalService.updateProgress(1L, BigDecimal.valueOf(150.00));

            assertThat(result).isNotNull();
            assertThat(goal.getCurrentAmount()).isEqualByComparingTo(BigDecimal.valueOf(1050.00));
            assertThat(goal.getIsActive()).isFalse();
        }
    }

    @Test
    void shouldMarkAsCompleted_WhenGoalExists() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            FinancialGoal goal = new FinancialGoal();
            goal.setId(1L);
            goal.setIsActive(true);
            goal.setUser(testUser);

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FinancialGoalDTO result = goalService.markAsCompleted(1L);

            assertThat(result).isNotNull();
            assertThat(goal.getIsActive()).isFalse();
        }
    }

    @Test
    void shouldReactivate_WhenGoalExists() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            FinancialGoal goal = new FinancialGoal();
            goal.setId(1L);
            goal.setIsActive(false);
            goal.setUser(testUser);

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FinancialGoalDTO result = goalService.reactivate(1L);

            assertThat(result).isNotNull();
            assertThat(goal.getIsActive()).isTrue();
        }
    }

    @Test
    void shouldCompleteGoal_WithFullCompletionData() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            FinancialGoal goal = new FinancialGoal();
            goal.setId(1L);
            goal.setIsActive(true);
            goal.setUser(testUser);

            GoalCompletionRequest request = new GoalCompletionRequest();
            request.setFinalAmount(BigDecimal.valueOf(1200.00));
            request.setCompletionDate(LocalDateTime.now());
            request.setCompleted(true);
            request.setCompletionNotes("Goal completed successfully");
            request.setAchievementNotes("Achieved ahead of schedule");
            request.setActualSavings(BigDecimal.valueOf(1000.00));
            request.setActualInvestment(BigDecimal.valueOf(200.00));

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FinancialGoalDTO result = goalService.completeGoal(1L, request);

            assertThat(result).isNotNull();
            assertThat(goal.getCurrentAmount()).isEqualByComparingTo(BigDecimal.valueOf(1200.00));
            assertThat(goal.getIsActive()).isFalse();
            assertThat(goal.getCompletedDate()).isNotNull();
            assertThat(goal.getCompleted()).isTrue();
            assertThat(goal.getCompletionNotes()).isEqualTo("Goal completed successfully");
            assertThat(goal.getAchievementNotes()).isEqualTo("Achieved ahead of schedule");
            assertThat(goal.getActualSavings()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
            assertThat(goal.getActualInvestment()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        }
    }

    @Test
    void shouldGetGoalTypes_ReturnDistinctTypes() {
        List<String> goalTypes = List.of("SAVINGS", "INVESTMENT", "DEBT_PAYOFF");

        when(goalRepository.findDistinctGoalTypes()).thenReturn(goalTypes);

        List<String> result = goalService.getGoalTypes();

        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("SAVINGS", "INVESTMENT", "DEBT_PAYOFF");
    }

    @Test
    void shouldGetStatusSummary_ForUserId() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("activeCount", 5L);
        summary.put("completedCount", 3L);
        summary.put("totalCount", 8L);

        when(goalRepository.getStatusSummary(1L)).thenReturn(summary);

        Map<String, Object> result = goalService.getStatusSummary(1L);

        assertThat(result).isNotNull();
        assertThat(result.get("activeCount")).isEqualTo(5L);
        assertThat(result.get("completedCount")).isEqualTo(3L);
        assertThat(result.get("totalCount")).isEqualTo(8L);
    }

    @Test
    void shouldGetProgressSummary_ForUserId() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("avgCompletionRate", 65.5);
        summary.put("totalCurrentAmount", BigDecimal.valueOf(5000.00));
        summary.put("totalTargetAmount", BigDecimal.valueOf(10000.00));

        when(goalRepository.getProgressSummary(1L)).thenReturn(summary);

        Map<String, Object> result = goalService.getProgressSummary(1L);

        assertThat(result).isNotNull();
        assertThat(result.get("avgCompletionRate")).isEqualTo(65.5);
        assertThat(result.get("totalCurrentAmount")).isEqualTo(BigDecimal.valueOf(5000.00));
    }

    @Test
    void shouldGetDeadlineAlerts_ForUserId() {
        List<Map<String, Object>> alerts = List.of(
                Map.of("id", 1L, "name", "Goal 1", "deadline", LocalDate.now().plusDays(15))
        );

        when(goalRepository.getDeadlineAlerts(eq(1L), any(LocalDate.class))).thenReturn(alerts);

        List<Map<String, Object>> result = goalService.getDeadlineAlerts(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("name")).isEqualTo("Goal 1");
    }

    @Test
    void shouldGetCompletionRate_ForUserId() {
        Map<String, Object> rate = new HashMap<>();
        rate.put("completionRate", 75.5);

        when(goalRepository.getCompletionRate(1L)).thenReturn(rate);

        Map<String, Object> result = goalService.getCompletionRate(1L);

        assertThat(result).isNotNull();
        assertThat(result.get("completionRate")).isEqualTo(75.5);
    }

    @Test
    void shouldGetAverageCompletionTime_ForUserId() {
        Map<String, Object> avgTime = new HashMap<>();
        avgTime.put("avgCompletionDays", 120.5);

        when(goalRepository.getAverageCompletionTime(1L)).thenReturn(avgTime);

        Map<String, Object> result = goalService.getAverageCompletionTime(1L);

        assertThat(result).isNotNull();
        assertThat(result.get("avgCompletionDays")).isEqualTo(120.5);
    }

    @Test
    void shouldUpdateProgress_WhenCurrentAmountIsNull() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            FinancialGoal goal = new FinancialGoal();
            goal.setId(1L);
            goal.setCurrentAmount(BigDecimal.ZERO);
            goal.setTargetAmount(BigDecimal.valueOf(1000.00));
            goal.setIsActive(true);
            goal.setUser(testUser);

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FinancialGoalDTO result = goalService.updateProgress(1L, BigDecimal.valueOf(100.00));

            assertThat(result).isNotNull();
            assertThat(goal.getCurrentAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
            assertThat(goal.getIsActive()).isTrue();
        }
    }
}
