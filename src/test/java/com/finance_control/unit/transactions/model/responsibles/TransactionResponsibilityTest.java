package com.finance_control.unit.transactions.model.responsibles;

import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles.TransactionResponsibility;
import com.finance_control.unit.BaseUnitTest;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionResponsibility Calculation Tests")
class TransactionResponsibilityTest extends BaseUnitTest {

    private Transaction transaction;
    private TransactionResponsibles responsible;
    private TransactionCategory category;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        category = new TransactionCategory();
        category.setId(1L);
        category.setName("Test Category");

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(1000.00));
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setSubtype(TransactionSubtype.VARIABLE);
        transaction.setSource(TransactionSource.CASH);

        responsible = new TransactionResponsibles();
        responsible.setId(1L);
        responsible.setName("Test Responsible");
    }

    @Test
    @DisplayName("calculateAmount_ShouldCalculateCorrectly_WhenAllFieldsPresent")
    void calculateAmount_ShouldCalculateCorrectly_WhenAllFieldsPresent() {
        TransactionResponsibility responsibility = new TransactionResponsibility(transaction, responsible, BigDecimal.valueOf(50.00));

        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }

    @Test
    @DisplayName("calculateAmount_ShouldHandle100Percent")
    void calculateAmount_ShouldHandle100Percent() {
        TransactionResponsibility responsibility = new TransactionResponsibility(transaction, responsible, BigDecimal.valueOf(100.00));

        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }

    @Test
    @DisplayName("calculateAmount_ShouldHandleDecimalPercentages")
    void calculateAmount_ShouldHandleDecimalPercentages() {
        TransactionResponsibility responsibility = new TransactionResponsibility(transaction, responsible, BigDecimal.valueOf(33.33));

        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(333.30));
    }

    @Test
    @DisplayName("calculateAmount_ShouldRoundCorrectly")
    void calculateAmount_ShouldRoundCorrectly() {
        transaction.setAmount(BigDecimal.valueOf(100.00));
        TransactionResponsibility responsibility = new TransactionResponsibility(transaction, responsible, BigDecimal.valueOf(33.33));

        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(33.33));
    }

    @Test
    @DisplayName("calculateAmount_ShouldNotCalculate_WhenTransactionIsNull")
    void calculateAmount_ShouldNotCalculate_WhenTransactionIsNull() {
        TransactionResponsibility responsibility = new TransactionResponsibility();
        responsibility.setTransaction(null);
        responsibility.setResponsible(responsible);
        responsibility.setPercentage(BigDecimal.valueOf(50.00));
        responsibility.calculateAmount();

        assertThat(responsibility.getCalculatedAmount()).isNull();
    }

    @Test
    @DisplayName("calculateAmount_ShouldNotCalculate_WhenAmountIsNull")
    void calculateAmount_ShouldNotCalculate_WhenAmountIsNull() {
        transaction.setAmount(null);
        TransactionResponsibility responsibility = new TransactionResponsibility();
        responsibility.setTransaction(transaction);
        responsibility.setResponsible(responsible);
        responsibility.setPercentage(BigDecimal.valueOf(50.00));
        responsibility.calculateAmount();

        assertThat(responsibility.getCalculatedAmount()).isNull();
    }

    @Test
    @DisplayName("calculateAmount_ShouldNotCalculate_WhenPercentageIsNull")
    void calculateAmount_ShouldNotCalculate_WhenPercentageIsNull() {
        TransactionResponsibility responsibility = new TransactionResponsibility();
        responsibility.setTransaction(transaction);
        responsibility.setResponsible(responsible);
        responsibility.setPercentage(null);
        responsibility.calculateAmount();

        assertThat(responsibility.getCalculatedAmount()).isNull();
    }

    @Test
    @DisplayName("calculateAmount_ShouldRecalculate_WhenCalledMultipleTimes")
    void calculateAmount_ShouldRecalculate_WhenCalledMultipleTimes() {
        TransactionResponsibility responsibility = new TransactionResponsibility(transaction, responsible, BigDecimal.valueOf(50.00));
        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.00));

        transaction.setAmount(BigDecimal.valueOf(2000.00));
        responsibility.calculateAmount();

        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }

    @Test
    @DisplayName("constructor_ShouldCalculateAmount_OnCreation")
    void constructor_ShouldCalculateAmount_OnCreation() {
        TransactionResponsibility responsibility = new TransactionResponsibility(transaction, responsible, BigDecimal.valueOf(75.00));

        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(750.00));
        assertThat(responsibility.getTransaction()).isEqualTo(transaction);
        assertThat(responsibility.getResponsible()).isEqualTo(responsible);
        assertThat(responsibility.getPercentage()).isEqualByComparingTo(BigDecimal.valueOf(75.00));
    }

    @Test
    @DisplayName("calculateAmount_ShouldHandleSmallAmounts")
    void calculateAmount_ShouldHandleSmallAmounts() {
        transaction.setAmount(BigDecimal.valueOf(1.00));
        TransactionResponsibility responsibility = new TransactionResponsibility(transaction, responsible, BigDecimal.valueOf(50.00));

        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.50));
    }

    @Test
    @DisplayName("calculateAmount_ShouldHandleLargeAmounts")
    void calculateAmount_ShouldHandleLargeAmounts() {
        transaction.setAmount(BigDecimal.valueOf(1000000.00));
        TransactionResponsibility responsibility = new TransactionResponsibility(transaction, responsible, BigDecimal.valueOf(25.00));

        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(250000.00));
    }

    @Test
    @DisplayName("calculateAmount_ShouldHandleVerySmallPercentages")
    void calculateAmount_ShouldHandleVerySmallPercentages() {
        TransactionResponsibility responsibility = new TransactionResponsibility(transaction, responsible, BigDecimal.valueOf(0.01));

        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.10));
    }

    @Test
    @DisplayName("calculateAmount_ShouldUseHalfUpRounding")
    void calculateAmount_ShouldUseHalfUpRounding() {
        transaction.setAmount(BigDecimal.valueOf(100.00));
        TransactionResponsibility responsibility = new TransactionResponsibility(transaction, responsible, BigDecimal.valueOf(33.33));

        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(33.33));
    }
}
