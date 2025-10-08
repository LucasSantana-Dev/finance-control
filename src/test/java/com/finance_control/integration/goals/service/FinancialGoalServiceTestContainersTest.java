package com.finance_control.integration.goals.service;

import com.finance_control.FinanceControlApplication;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TestContainers integration test for FinancialGoalService.
 * Tests real database operations with PostgreSQL container.
 *
 * This is the industry standard approach for integration testing in Spring Boot.
 */
@SpringBootTest(classes = FinanceControlApplication.class)
@Testcontainers
@ActiveProfiles("test")
@Transactional
class FinancialGoalServiceTestContainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private FinancialGoalService financialGoalService;

    @Autowired
    private FinancialGoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // "password"
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Set user context
        UserContext.setCurrentUserId(testUser.getId());
    }

    @Test
    void shouldCreateFinancialGoalWithRealDatabase() {
        // Given
        FinancialGoalDTO dto = new FinancialGoalDTO();
        dto.setName("Test Goal");
        dto.setDescription("Test Description");
        dto.setGoalType(GoalType.SAVINGS);
        dto.setTargetAmount(BigDecimal.valueOf(10000.00));
        dto.setCurrentAmount(BigDecimal.valueOf(1000.00));
        dto.setTargetDate(LocalDateTime.now().plusMonths(12));
        dto.setDeadline(LocalDateTime.now().plusMonths(12));
        dto.setIsActive(true);
        dto.setAutoCalculate(false);

        // When
        FinancialGoalDTO result = financialGoalService.create(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Goal");
        assertThat(result.getTargetAmount()).isEqualTo(BigDecimal.valueOf(10000.00));
        assertThat(result.getCurrentAmount()).isEqualTo(BigDecimal.valueOf(1000.00));

        // Verify in database
        List<FinancialGoal> goals = goalRepository.findAll();
        assertThat(goals).hasSize(1);
        assertThat(goals.get(0).getName()).isEqualTo("Test Goal");
        assertThat(goals.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindFinancialGoalById() {
        // Given - Create a goal
        FinancialGoal goal = new FinancialGoal();
        goal.setName("Test Goal");
        goal.setDescription("Test Description");
        goal.setGoalType(GoalType.SAVINGS);
        goal.setTargetAmount(BigDecimal.valueOf(10000.00));
        goal.setCurrentAmount(BigDecimal.valueOf(1000.00));
        goal.setDeadline(LocalDate.now().plusMonths(12));
        goal.setIsActive(true);
        goal.setAutoCalculate(false);
        goal.setUser(testUser);
        goal = goalRepository.save(goal);

        // When
        FinancialGoalDTO result = financialGoalService.findById(goal.getId()).orElse(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Goal");
        assertThat(result.getTargetAmount()).isEqualTo(BigDecimal.valueOf(10000.00));
    }

    @Test
    void shouldFindAllFinancialGoals() {
        // Given - Create multiple goals
        FinancialGoal goal1 = new FinancialGoal();
        goal1.setName("Goal 1");
        goal1.setDescription("Description 1");
        goal1.setGoalType(GoalType.SAVINGS);
        goal1.setTargetAmount(BigDecimal.valueOf(10000.00));
        goal1.setCurrentAmount(BigDecimal.valueOf(1000.00));
        goal1.setDeadline(LocalDate.now().plusMonths(12));
        goal1.setIsActive(true);
        goal1.setAutoCalculate(false);
        goal1.setUser(testUser);
        goalRepository.save(goal1);

        FinancialGoal goal2 = new FinancialGoal();
        goal2.setName("Goal 2");
        goal2.setDescription("Description 2");
        goal2.setGoalType(GoalType.INVESTMENT);
        goal2.setTargetAmount(BigDecimal.valueOf(20000.00));
        goal2.setCurrentAmount(BigDecimal.valueOf(2000.00));
        goal2.setDeadline(LocalDate.now().plusMonths(24));
        goal2.setIsActive(true);
        goal2.setAutoCalculate(false);
        goal2.setUser(testUser);
        goalRepository.save(goal2);

        // When
        Page<FinancialGoalDTO> results = financialGoalService.findAll(null, null, null, null, null);

        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting(FinancialGoalDTO::getName)
                .containsExactlyInAnyOrder("Goal 1", "Goal 2");
    }

    @Test
    void shouldUpdateFinancialGoal() {
        // Given - Create a goal
        FinancialGoal goal = new FinancialGoal();
        goal.setName("Original Goal");
        goal.setDescription("Original Description");
        goal.setGoalType(GoalType.SAVINGS);
        goal.setTargetAmount(BigDecimal.valueOf(10000.00));
        goal.setCurrentAmount(BigDecimal.valueOf(1000.00));
        goal.setDeadline(LocalDate.now().plusMonths(12));
        goal.setIsActive(true);
        goal.setAutoCalculate(false);
        goal.setUser(testUser);
        goal = goalRepository.save(goal);

        // When - Update the goal
        FinancialGoalDTO updateDTO = new FinancialGoalDTO();
        updateDTO.setId(goal.getId());
        updateDTO.setName("Updated Goal");
        updateDTO.setDescription("Updated Description");
        updateDTO.setGoalType(GoalType.INVESTMENT);
        updateDTO.setTargetAmount(BigDecimal.valueOf(15000.00));
        updateDTO.setCurrentAmount(BigDecimal.valueOf(1500.00));
        updateDTO.setTargetDate(LocalDateTime.now().plusMonths(18));
        updateDTO.setDeadline(LocalDateTime.now().plusMonths(18));
        updateDTO.setIsActive(true);
        updateDTO.setAutoCalculate(false);

        FinancialGoalDTO result = financialGoalService.update(goal.getId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Goal");
        assertThat(result.getTargetAmount()).isEqualTo(BigDecimal.valueOf(15000.00));

        // Verify in database
        FinancialGoal updatedGoal = goalRepository.findById(goal.getId()).orElse(null);
        assertThat(updatedGoal).isNotNull();
        assertThat(updatedGoal.getName()).isEqualTo("Updated Goal");
        assertThat(updatedGoal.getTargetAmount()).isEqualTo(BigDecimal.valueOf(15000.00));
    }

    @Test
    void shouldDeleteFinancialGoal() {
        // Given - Create a goal
        FinancialGoal goal = new FinancialGoal();
        goal.setName("Test Goal");
        goal.setDescription("Test Description");
        goal.setGoalType(GoalType.SAVINGS);
        goal.setTargetAmount(BigDecimal.valueOf(10000.00));
        goal.setCurrentAmount(BigDecimal.valueOf(1000.00));
        goal.setDeadline(LocalDate.now().plusMonths(12));
        goal.setIsActive(true);
        goal.setAutoCalculate(false);
        goal.setUser(testUser);
        goal = goalRepository.save(goal);

        // When
        financialGoalService.delete(goal.getId());

        // Then
        assertThat(goalRepository.findById(goal.getId())).isEmpty();
    }

    @Test
    void shouldUpdateGoalProgress() {
        // Given - Create a goal
        FinancialGoal goal = new FinancialGoal();
        goal.setName("Test Goal");
        goal.setDescription("Test Description");
        goal.setGoalType(GoalType.SAVINGS);
        goal.setTargetAmount(BigDecimal.valueOf(10000.00));
        goal.setCurrentAmount(BigDecimal.valueOf(1000.00));
        goal.setDeadline(LocalDate.now().plusMonths(12));
        goal.setIsActive(true);
        goal.setAutoCalculate(false);
        goal.setUser(testUser);
        goal = goalRepository.save(goal);

        // When - Update progress
        FinancialGoalDTO updateDTO = new FinancialGoalDTO();
        updateDTO.setId(goal.getId());
        updateDTO.setName("Test Goal");
        updateDTO.setDescription("Test Description");
        updateDTO.setGoalType(GoalType.SAVINGS);
        updateDTO.setTargetAmount(BigDecimal.valueOf(10000.00));
        updateDTO.setCurrentAmount(BigDecimal.valueOf(5000.00)); // Updated progress
        updateDTO.setTargetDate(LocalDateTime.now().plusMonths(12));
        updateDTO.setDeadline(LocalDateTime.now().plusMonths(12));
        updateDTO.setIsActive(true);
        updateDTO.setAutoCalculate(false);

        FinancialGoalDTO result = financialGoalService.update(goal.getId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentAmount()).isEqualTo(BigDecimal.valueOf(5000.00));

        // Verify in database
        FinancialGoal updatedGoal = goalRepository.findById(goal.getId()).orElse(null);
        assertThat(updatedGoal).isNotNull();
        assertThat(updatedGoal.getCurrentAmount()).isEqualTo(BigDecimal.valueOf(5000.00));
    }
}
