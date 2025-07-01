package com.finance_control.unit.goals.model;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.shared.enums.GoalType;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialGoalTest {

    private FinancialGoal financialGoal;
    private User testUser;
    private TransactionSourceEntity testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("John Doe");

        testAccount = new TransactionSourceEntity();
        testAccount.setId(1L);
        testAccount.setName("Savings Account");

        financialGoal = new FinancialGoal();
        financialGoal.setId(1L);
        financialGoal.setName("Vacation Fund");
        financialGoal.setDescription("Save for summer vacation");
        financialGoal.setGoalType(GoalType.SAVINGS);
        financialGoal.setTargetAmount(new BigDecimal("5000.00"));
        financialGoal.setCurrentAmount(new BigDecimal("1500.00"));
        financialGoal.setDeadline(LocalDate.of(2024, 6, 30));
        financialGoal.setIsActive(true);
        financialGoal.setAutoCalculate(false);
        financialGoal.setUser(testUser);
        financialGoal.setAccount(testAccount);
    }

    @Test
    void getProgressPercentage_ShouldCalculateCorrectly() {
        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(new BigDecimal("30.0000"));
    }

    @Test
    void getProgressPercentage_ShouldReturnZero_WhenTargetAmountIsZero() {
        financialGoal.setTargetAmount(BigDecimal.ZERO);

        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getProgressPercentage_ShouldReturnZero_WhenTargetAmountIsNull() {
        financialGoal.setTargetAmount(null);

        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getProgressPercentage_ShouldHandleNullCurrentAmount() {
        financialGoal.setCurrentAmount(null);

        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getProgressPercentage_ShouldReturn100_WhenCurrentEqualsTarget() {
        financialGoal.setCurrentAmount(new BigDecimal("5000.00"));

        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(new BigDecimal("100.0000"));
    }

    @Test
    void getProgressPercentage_ShouldReturn100_WhenCurrentExceedsTarget() {
        financialGoal.setCurrentAmount(new BigDecimal("6000.00"));

        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(new BigDecimal("120.0000"));
    }

    @Test
    void getRemainingAmount_ShouldCalculateCorrectly() {
        BigDecimal remaining = financialGoal.getRemainingAmount();

        assertThat(remaining).isEqualByComparingTo(new BigDecimal("3500.00"));
    }

    @Test
    void getRemainingAmount_ShouldHandleNullCurrentAmount() {
        financialGoal.setCurrentAmount(null);

        BigDecimal remaining = financialGoal.getRemainingAmount();

        assertThat(remaining).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    void getRemainingAmount_ShouldReturnZero_WhenCurrentExceedsTarget() {
        financialGoal.setCurrentAmount(new BigDecimal("6000.00"));

        BigDecimal remaining = financialGoal.getRemainingAmount();

        assertThat(remaining).isEqualByComparingTo(new BigDecimal("-1000.00"));
    }

    @Test
    void isCompleted_ShouldReturnTrue_WhenCurrentEqualsTarget() {
        financialGoal.setCurrentAmount(new BigDecimal("5000.00"));

        boolean completed = financialGoal.isCompleted();

        assertThat(completed).isTrue();
    }

    @Test
    void isCompleted_ShouldReturnTrue_WhenCurrentExceedsTarget() {
        financialGoal.setCurrentAmount(new BigDecimal("6000.00"));

        boolean completed = financialGoal.isCompleted();

        assertThat(completed).isTrue();
    }

    @Test
    void isCompleted_ShouldReturnFalse_WhenCurrentLessThanTarget() {
        financialGoal.setCurrentAmount(new BigDecimal("1500.00"));

        boolean completed = financialGoal.isCompleted();

        assertThat(completed).isFalse();
    }

    @Test
    void isCompleted_ShouldReturnFalse_WhenCurrentAmountIsNull() {
        financialGoal.setCurrentAmount(null);

        boolean completed = financialGoal.isCompleted();

        assertThat(completed).isFalse();
    }

    @Test
    void isCompleted_ShouldReturnFalse_WhenCurrentAmountIsZero() {
        financialGoal.setCurrentAmount(BigDecimal.ZERO);

        boolean completed = financialGoal.isCompleted();

        assertThat(completed).isFalse();
    }

    @Test
    void goalProperties_ShouldBeSetCorrectly() {
        assertThat(financialGoal.getId()).isEqualTo(1L);
        assertThat(financialGoal.getName()).isEqualTo("Vacation Fund");
        assertThat(financialGoal.getDescription()).isEqualTo("Save for summer vacation");
        assertThat(financialGoal.getGoalType()).isEqualTo(GoalType.SAVINGS);
        assertThat(financialGoal.getTargetAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(financialGoal.getCurrentAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(financialGoal.getDeadline()).isEqualTo(LocalDate.of(2024, 6, 30));
        assertThat(financialGoal.getIsActive()).isTrue();
        assertThat(financialGoal.getAutoCalculate()).isFalse();
        assertThat(financialGoal.getUser().getFullName()).isEqualTo("John Doe");
        assertThat(financialGoal.getAccount()).isEqualTo(testAccount);
    }
} 