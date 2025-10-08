package com.finance_control.unit.goals.service;

import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.goals.service.FinancialGoalService;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.enums.GoalType;
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
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class FinancialGoalServiceTest {

    @Mock
    private FinancialGoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FinancialGoalService goalService;

    private FinancialGoal testGoal;
    private FinancialGoalDTO testGoalDTO;
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

        testGoalDTO = new FinancialGoalDTO();
        testGoalDTO.setId(1L);
        testGoalDTO.setName("Test Goal");
        testGoalDTO.setDescription("Test Description");
        testGoalDTO.setGoalType(GoalType.SAVINGS);
        testGoalDTO.setTargetAmount(BigDecimal.valueOf(1000.00));
        testGoalDTO.setCurrentAmount(BigDecimal.valueOf(500.00));
        testGoalDTO.setTargetDate(LocalDateTime.now().plusMonths(6));
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
            dto.setTargetDate(LocalDateTime.now().plusMonths(12));

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
}
