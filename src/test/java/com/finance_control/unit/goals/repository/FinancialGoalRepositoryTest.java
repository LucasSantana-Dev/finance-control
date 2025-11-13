package com.finance_control.unit.goals.repository;

import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.shared.enums.GoalType;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest repository layer test for FinancialGoalRepository.
 * Tests JPA repository operations with in-memory H2 database.
 *
 * This is the industry standard approach for testing repositories in Spring Boot.
 */
@DataJpaTest
@ActiveProfiles("test")
class FinancialGoalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FinancialGoalRepository goalRepository;

    private User testUser;
    private FinancialGoal testGoal;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = entityManager.persistAndFlush(testUser);

        // Create test goal
        testGoal = new FinancialGoal();
        testGoal.setName("Test Goal");
        testGoal.setDescription("Test Description");
        testGoal.setGoalType(GoalType.SAVINGS);
        testGoal.setTargetAmount(BigDecimal.valueOf(10000.00));
        testGoal.setCurrentAmount(BigDecimal.valueOf(3000.00));
        testGoal.setDeadline(LocalDate.now().plusMonths(12));
        testGoal.setIsActive(true);
        testGoal.setAutoCalculate(false);
        testGoal.setUser(testUser);
        testGoal.setCreatedAt(LocalDateTime.now());
        testGoal.setUpdatedAt(LocalDateTime.now());
        testGoal = entityManager.persistAndFlush(testGoal);
    }

    @Test
    void shouldFindGoalById() {
        // When
        Optional<FinancialGoal> found = goalRepository.findById(testGoal.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Goal");
        assertThat(found.get().getTargetAmount()).isEqualTo(BigDecimal.valueOf(10000.00));
        assertThat(found.get().getGoalType()).isEqualTo(GoalType.SAVINGS);
    }

    @Test
    void shouldFindAllGoals() {
        // Given - Create additional goal
        FinancialGoal anotherGoal = new FinancialGoal();
        anotherGoal.setName("Another Goal");
        anotherGoal.setDescription("Another Description");
        anotherGoal.setGoalType(GoalType.INVESTMENT);
        anotherGoal.setTargetAmount(BigDecimal.valueOf(20000.00));
        anotherGoal.setCurrentAmount(BigDecimal.valueOf(2000.00));
        anotherGoal.setDeadline(LocalDate.now().plusMonths(24));
        anotherGoal.setIsActive(true);
        anotherGoal.setAutoCalculate(false);
        anotherGoal.setUser(testUser);
        anotherGoal.setCreatedAt(LocalDateTime.now());
        anotherGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(anotherGoal);

        // When
        List<FinancialGoal> goals = goalRepository.findAll();

        // Then
        assertThat(goals).hasSize(2);
        assertThat(goals).extracting(FinancialGoal::getName)
                .containsExactlyInAnyOrder("Test Goal", "Another Goal");
    }

    @Test
    void shouldFindGoalsByUser() {
        // Given - Create another user with goal
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi");
        anotherUser.setIsActive(true);
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser.setUpdatedAt(LocalDateTime.now());
        anotherUser = entityManager.persistAndFlush(anotherUser);

        FinancialGoal anotherUserGoal = new FinancialGoal();
        anotherUserGoal.setName("Another User Goal");
        anotherUserGoal.setDescription("Another User Description");
        anotherUserGoal.setGoalType(GoalType.SAVINGS);
        anotherUserGoal.setTargetAmount(BigDecimal.valueOf(15000.00));
        anotherUserGoal.setCurrentAmount(BigDecimal.valueOf(1500.00));
        anotherUserGoal.setDeadline(LocalDate.now().plusMonths(18));
        anotherUserGoal.setIsActive(true);
        anotherUserGoal.setAutoCalculate(false);
        anotherUserGoal.setUser(anotherUser);
        anotherUserGoal.setCreatedAt(LocalDateTime.now());
        anotherUserGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(anotherUserGoal);

        // When - Use standard JPA method
        List<FinancialGoal> allGoals = goalRepository.findAll();
        List<FinancialGoal> userGoals = allGoals.stream()
                .filter(g -> g.getUser().getId().equals(testUser.getId()))
                .toList();

        // Then
        assertThat(userGoals).hasSize(1);
        assertThat(userGoals.get(0).getName()).isEqualTo("Test Goal");
        assertThat(userGoals.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindGoalsByType() {
        // Given - Create investment goal
        FinancialGoal investmentGoal = new FinancialGoal();
        investmentGoal.setName("Investment Goal");
        investmentGoal.setDescription("Investment Description");
        investmentGoal.setGoalType(GoalType.INVESTMENT);
        investmentGoal.setTargetAmount(BigDecimal.valueOf(50000.00));
        investmentGoal.setCurrentAmount(BigDecimal.valueOf(5000.00));
        investmentGoal.setDeadline(LocalDate.now().plusMonths(36));
        investmentGoal.setIsActive(true);
        investmentGoal.setAutoCalculate(false);
        investmentGoal.setUser(testUser);
        investmentGoal.setCreatedAt(LocalDateTime.now());
        investmentGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(investmentGoal);

        // When - Use standard JPA method
        List<FinancialGoal> allGoals = goalRepository.findAll();
        List<FinancialGoal> savingsGoals = allGoals.stream()
                .filter(g -> g.getGoalType() == GoalType.SAVINGS)
                .toList();

        // Then
        assertThat(savingsGoals).hasSize(1);
        assertThat(savingsGoals.get(0).getGoalType()).isEqualTo(GoalType.SAVINGS);
        assertThat(savingsGoals.get(0).getName()).isEqualTo("Test Goal");
    }

    @Test
    void shouldFindActiveGoals() {
        // Given - Create inactive goal
        FinancialGoal inactiveGoal = new FinancialGoal();
        inactiveGoal.setName("Inactive Goal");
        inactiveGoal.setDescription("Inactive Description");
        inactiveGoal.setGoalType(GoalType.SAVINGS);
        inactiveGoal.setTargetAmount(BigDecimal.valueOf(5000.00));
        inactiveGoal.setCurrentAmount(BigDecimal.valueOf(500.00));
        inactiveGoal.setDeadline(LocalDate.now().plusMonths(6));
        inactiveGoal.setIsActive(false);
        inactiveGoal.setAutoCalculate(false);
        inactiveGoal.setUser(testUser);
        inactiveGoal.setCreatedAt(LocalDateTime.now());
        inactiveGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(inactiveGoal);

        // When - Use standard JPA method
        List<FinancialGoal> allGoals = goalRepository.findAll();
        List<FinancialGoal> activeGoals = allGoals.stream()
                .filter(g -> g.getIsActive() == true)
                .toList();

        // Then
        assertThat(activeGoals).hasSize(1);
        assertThat(activeGoals.get(0).getName()).isEqualTo("Test Goal");
        assertThat(activeGoals.get(0).getIsActive()).isTrue();
    }

    @Test
    void shouldFindGoalsByTargetAmountRange() {
        // Given - Create goals with different target amounts
        FinancialGoal lowAmountGoal = new FinancialGoal();
        lowAmountGoal.setName("Low Amount Goal");
        lowAmountGoal.setDescription("Low Amount Description");
        lowAmountGoal.setGoalType(GoalType.SAVINGS);
        lowAmountGoal.setTargetAmount(BigDecimal.valueOf(5000.00));
        lowAmountGoal.setCurrentAmount(BigDecimal.valueOf(500.00));
        lowAmountGoal.setDeadline(LocalDate.now().plusMonths(6));
        lowAmountGoal.setIsActive(true);
        lowAmountGoal.setAutoCalculate(false);
        lowAmountGoal.setUser(testUser);
        lowAmountGoal.setCreatedAt(LocalDateTime.now());
        lowAmountGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(lowAmountGoal);

        FinancialGoal highAmountGoal = new FinancialGoal();
        highAmountGoal.setName("High Amount Goal");
        highAmountGoal.setDescription("High Amount Description");
        highAmountGoal.setGoalType(GoalType.INVESTMENT);
        highAmountGoal.setTargetAmount(BigDecimal.valueOf(50000.00));
        highAmountGoal.setCurrentAmount(BigDecimal.valueOf(5000.00));
        highAmountGoal.setDeadline(LocalDate.now().plusMonths(36));
        highAmountGoal.setIsActive(true);
        highAmountGoal.setAutoCalculate(false);
        highAmountGoal.setUser(testUser);
        highAmountGoal.setCreatedAt(LocalDateTime.now());
        highAmountGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(highAmountGoal);

        // When - Use standard JPA method
        List<FinancialGoal> allGoals = goalRepository.findAll();
        List<FinancialGoal> mediumAmountGoals = allGoals.stream()
                .filter(g -> g.getTargetAmount().compareTo(BigDecimal.valueOf(7500.00)) >= 0 &&
                           g.getTargetAmount().compareTo(BigDecimal.valueOf(15000.00)) <= 0)
                .toList();

        // Then
        assertThat(mediumAmountGoals).hasSize(1);
        assertThat(mediumAmountGoals.get(0).getName()).isEqualTo("Test Goal");
        assertThat(mediumAmountGoals.get(0).getTargetAmount()).isEqualTo(BigDecimal.valueOf(10000.00));
    }

    @Test
    void shouldFindGoalsWithPagination() {
        // Given - Create multiple goals
        for (int i = 1; i <= 5; i++) {
            FinancialGoal goal = new FinancialGoal();
            goal.setName("Goal " + i);
            goal.setDescription("Description " + i);
            goal.setGoalType(GoalType.SAVINGS);
            goal.setTargetAmount(BigDecimal.valueOf(10000.00 * i));
            goal.setCurrentAmount(BigDecimal.valueOf(1000.00 * i));
            goal.setDeadline(LocalDate.now().plusMonths(12 * i));
            goal.setIsActive(true);
            goal.setAutoCalculate(false);
            goal.setUser(testUser);
            goal.setCreatedAt(LocalDateTime.now());
            goal.setUpdatedAt(LocalDateTime.now());
            entityManager.persistAndFlush(goal);
        }

        // When
        Pageable pageable = PageRequest.of(0, 3, Sort.by("targetAmount").ascending());
        Page<FinancialGoal> page = goalRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(6); // 5 new + 1 original
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(3);
    }

    @Test
    void shouldSaveGoal() {
        // Given
        FinancialGoal newGoal = new FinancialGoal();
        newGoal.setName("New Goal");
        newGoal.setDescription("New Description");
        newGoal.setGoalType(GoalType.INVESTMENT);
        newGoal.setTargetAmount(BigDecimal.valueOf(25000.00));
        newGoal.setCurrentAmount(BigDecimal.valueOf(2500.00));
        newGoal.setDeadline(LocalDate.now().plusMonths(18));
        newGoal.setIsActive(true);
        newGoal.setAutoCalculate(false);
        newGoal.setUser(testUser);
        newGoal.setCreatedAt(LocalDateTime.now());
        newGoal.setUpdatedAt(LocalDateTime.now());

        // When
        FinancialGoal saved = goalRepository.save(newGoal);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Goal");
        assertThat(saved.getTargetAmount()).isEqualTo(BigDecimal.valueOf(25000.00));
        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldUpdateGoal() {
        // Given
        testGoal.setName("Updated Goal");
        testGoal.setTargetAmount(BigDecimal.valueOf(15000.00));

        // When
        FinancialGoal updated = goalRepository.save(testGoal);
        entityManager.flush();

        // Then
        assertThat(updated.getName()).isEqualTo("Updated Goal");
        assertThat(updated.getTargetAmount()).isEqualTo(BigDecimal.valueOf(15000.00));
        assertThat(updated.getId()).isEqualTo(testGoal.getId());
    }

    @Test
    void shouldDeleteGoal() {
        // Given
        Long goalId = testGoal.getId();

        // When
        goalRepository.delete(testGoal);
        entityManager.flush();

        // Then
        Optional<FinancialGoal> deleted = goalRepository.findById(goalId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void shouldCountGoalsByUser() {
        // Given - Create additional goal for same user
        FinancialGoal anotherGoal = new FinancialGoal();
        anotherGoal.setName("Another Goal");
        anotherGoal.setDescription("Another Description");
        anotherGoal.setGoalType(GoalType.INVESTMENT);
        anotherGoal.setTargetAmount(BigDecimal.valueOf(20000.00));
        anotherGoal.setCurrentAmount(BigDecimal.valueOf(2000.00));
        anotherGoal.setDeadline(LocalDate.now().plusMonths(24));
        anotherGoal.setIsActive(true);
        anotherGoal.setAutoCalculate(false);
        anotherGoal.setUser(testUser);
        anotherGoal.setCreatedAt(LocalDateTime.now());
        anotherGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(anotherGoal);

        // When - Use standard JPA method
        List<FinancialGoal> allGoals = goalRepository.findAll();
        long count = allGoals.stream()
                .filter(g -> g.getUser().getId().equals(testUser.getId()))
                .count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindGoalsByDeadlineRange() {
        // Given - Create goals with different deadlines
        FinancialGoal pastGoal = new FinancialGoal();
        pastGoal.setName("Past Goal");
        pastGoal.setDescription("Past Description");
        pastGoal.setGoalType(GoalType.SAVINGS);
        pastGoal.setTargetAmount(BigDecimal.valueOf(5000.00));
        pastGoal.setCurrentAmount(BigDecimal.valueOf(500.00));
        pastGoal.setDeadline(LocalDate.now().minusMonths(6));
        pastGoal.setIsActive(true);
        pastGoal.setAutoCalculate(false);
        pastGoal.setUser(testUser);
        pastGoal.setCreatedAt(LocalDateTime.now());
        pastGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(pastGoal);

        FinancialGoal futureGoal = new FinancialGoal();
        futureGoal.setName("Future Goal");
        futureGoal.setDescription("Future Description");
        futureGoal.setGoalType(GoalType.INVESTMENT);
        futureGoal.setTargetAmount(BigDecimal.valueOf(30000.00));
        futureGoal.setCurrentAmount(BigDecimal.valueOf(3000.00));
        futureGoal.setDeadline(LocalDate.now().plusMonths(36));
        futureGoal.setIsActive(true);
        futureGoal.setAutoCalculate(false);
        futureGoal.setUser(testUser);
        futureGoal.setCreatedAt(LocalDateTime.now());
        futureGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(futureGoal);

        // When - Use standard JPA method
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = LocalDate.now().plusMonths(18);
        List<FinancialGoal> allGoals = goalRepository.findAll();
        List<FinancialGoal> recentGoals = allGoals.stream()
                .filter(g -> g.getDeadline().isAfter(startDate) && g.getDeadline().isBefore(endDate))
                .toList();

        // Then
        assertThat(recentGoals).hasSize(1);
        assertThat(recentGoals.get(0).getName()).isEqualTo("Test Goal");
    }

    @Test
    void shouldFindGoalsByProgress() {
        // Given - Create goals with different progress levels
        FinancialGoal lowProgressGoal = new FinancialGoal();
        lowProgressGoal.setName("Low Progress Goal");
        lowProgressGoal.setDescription("Low Progress Description");
        lowProgressGoal.setGoalType(GoalType.SAVINGS);
        lowProgressGoal.setTargetAmount(BigDecimal.valueOf(10000.00));
        lowProgressGoal.setCurrentAmount(BigDecimal.valueOf(1000.00)); // 10% progress
        lowProgressGoal.setDeadline(LocalDate.now().plusMonths(12));
        lowProgressGoal.setIsActive(true);
        lowProgressGoal.setAutoCalculate(false);
        lowProgressGoal.setUser(testUser);
        lowProgressGoal.setCreatedAt(LocalDateTime.now());
        lowProgressGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(lowProgressGoal);

        FinancialGoal highProgressGoal = new FinancialGoal();
        highProgressGoal.setName("High Progress Goal");
        highProgressGoal.setDescription("High Progress Description");
        highProgressGoal.setGoalType(GoalType.SAVINGS);
        highProgressGoal.setTargetAmount(BigDecimal.valueOf(10000.00));
        highProgressGoal.setCurrentAmount(BigDecimal.valueOf(8000.00)); // 80% progress
        highProgressGoal.setDeadline(LocalDate.now().plusMonths(12));
        highProgressGoal.setIsActive(true);
        highProgressGoal.setAutoCalculate(false);
        highProgressGoal.setUser(testUser);
        highProgressGoal.setCreatedAt(LocalDateTime.now());
        highProgressGoal.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(highProgressGoal);

        // When - Use standard JPA method
        BigDecimal minProgress = BigDecimal.valueOf(2000.00); // 20% of 10000
        BigDecimal maxProgress = BigDecimal.valueOf(6000.00); // 60% of 10000
        List<FinancialGoal> allGoals = goalRepository.findAll();
        List<FinancialGoal> mediumProgressGoals = allGoals.stream()
                .filter(g -> g.getCurrentAmount().compareTo(minProgress) >= 0 &&
                           g.getCurrentAmount().compareTo(maxProgress) <= 0)
                .toList();

        // Then
        assertThat(mediumProgressGoals).hasSize(1);
        assertThat(mediumProgressGoals.get(0).getName()).isEqualTo("Test Goal");
        assertThat(mediumProgressGoals.get(0).getCurrentAmount()).isEqualTo(BigDecimal.valueOf(3000.00));
    }
}
