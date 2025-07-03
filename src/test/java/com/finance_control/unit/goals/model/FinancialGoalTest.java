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

    private static final String TARGET_AMOUNT = "5000.00";
    private static final String CURRENT_AMOUNT = "1500.00";
    private static final String EXCEEDING_AMOUNT = "6000.00";

    private FinancialGoal financialGoal;
    private TransactionSourceEntity testAccount;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);

        testAccount = new TransactionSourceEntity();
        testAccount.setId(1L);
        testAccount.setName("Savings Account");

        financialGoal = new FinancialGoal();
        financialGoal.setId(1L);
        financialGoal.setName("Vacation Fund");
        financialGoal.setDescription("Save for summer vacation");
        financialGoal.setGoalType(GoalType.SAVINGS);
        financialGoal.setTargetAmount(new BigDecimal(TARGET_AMOUNT));
        financialGoal.setCurrentAmount(new BigDecimal(CURRENT_AMOUNT));
        financialGoal.setDeadline(LocalDate.of(2024, 6, 30));
        financialGoal.setIsActive(true);
        financialGoal.setAutoCalculate(false);
        financialGoal.setUser(testUser);
        financialGoal.setAccount(testAccount);
    }

    @Test
    void getProgressPercentageShouldCalculateCorrectly() {
        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(new BigDecimal("30.0000"));
    }

    @Test
    void getProgressPercentageShouldReturnZeroWhenTargetAmountIsZero() {
        financialGoal.setTargetAmount(BigDecimal.ZERO);

        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getProgressPercentageShouldReturnZeroWhenTargetAmountIsNull() {
        financialGoal.setTargetAmount(null);

        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getProgressPercentageShouldHandleNullCurrentAmount() {
        financialGoal.setCurrentAmount(null);

        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getProgressPercentageShouldReturn100WhenCurrentEqualsTarget() {
        financialGoal.setCurrentAmount(new BigDecimal(TARGET_AMOUNT));

        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(new BigDecimal("100.0000"));
    }

    @Test
    void getProgressPercentageShouldReturn100WhenCurrentExceedsTarget() {
        financialGoal.setCurrentAmount(new BigDecimal(EXCEEDING_AMOUNT));

        BigDecimal progress = financialGoal.getProgressPercentage();

        assertThat(progress).isEqualByComparingTo(new BigDecimal("120.0000"));
    }

    @Test
    void getRemainingAmountShouldCalculateCorrectly() {
        BigDecimal remaining = financialGoal.getRemainingAmount();

        assertThat(remaining).isEqualByComparingTo(new BigDecimal("3500.00"));
    }

    @Test
    void getRemainingAmountShouldHandleNullCurrentAmount() {
        financialGoal.setCurrentAmount(null);

        BigDecimal remaining = financialGoal.getRemainingAmount();

        assertThat(remaining).isEqualByComparingTo(new BigDecimal(TARGET_AMOUNT));
    }

    @Test
    void getRemainingAmountShouldReturnZeroWhenCurrentExceedsTarget() {
        financialGoal.setCurrentAmount(new BigDecimal(EXCEEDING_AMOUNT));

        BigDecimal remaining = financialGoal.getRemainingAmount();

        assertThat(remaining).isEqualByComparingTo(new BigDecimal("-1000.00"));
    }

    @Test
    void isCompletedShouldReturnTrueWhenCurrentEqualsTarget() {
        financialGoal.setCurrentAmount(new BigDecimal(TARGET_AMOUNT));

        boolean completed = financialGoal.isCompleted();

        assertThat(completed).isTrue();
    }

    @Test
    void isCompletedShouldReturnTrueWhenCurrentExceedsTarget() {
        financialGoal.setCurrentAmount(new BigDecimal(EXCEEDING_AMOUNT));

        boolean completed = financialGoal.isCompleted();

        assertThat(completed).isTrue();
    }

    @Test
    void isCompletedShouldReturnFalseWhenCurrentLessThanTarget() {
        financialGoal.setCurrentAmount(new BigDecimal(CURRENT_AMOUNT));

        boolean completed = financialGoal.isCompleted();

        assertThat(completed).isFalse();
    }

    @Test
    void isCompletedShouldReturnFalseWhenCurrentAmountIsNull() {
        financialGoal.setCurrentAmount(null);

        boolean completed = financialGoal.isCompleted();

        assertThat(completed).isFalse();
    }

    @Test
    void isCompletedShouldReturnFalseWhenCurrentAmountIsZero() {
        financialGoal.setCurrentAmount(BigDecimal.ZERO);

        boolean completed = financialGoal.isCompleted();

        assertThat(completed).isFalse();
    }

    @Test
    void goalPropertiesShouldBeSetCorrectly() {
        assertThat(financialGoal.getId()).isEqualTo(1L);
        assertThat(financialGoal.getName()).isEqualTo("Vacation Fund");
        assertThat(financialGoal.getDescription()).isEqualTo("Save for summer vacation");
        assertThat(financialGoal.getGoalType()).isEqualTo(GoalType.SAVINGS);
        assertThat(financialGoal.getTargetAmount()).isEqualByComparingTo(new BigDecimal(TARGET_AMOUNT));
        assertThat(financialGoal.getCurrentAmount()).isEqualByComparingTo(new BigDecimal(CURRENT_AMOUNT));
        assertThat(financialGoal.getDeadline()).isEqualTo(LocalDate.of(2024, 6, 30));
        assertThat(financialGoal.getIsActive()).isTrue();
        assertThat(financialGoal.getAutoCalculate()).isFalse();
        assertThat(financialGoal.getUser().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(financialGoal.getAccount()).isEqualTo(testAccount);
    }
} 