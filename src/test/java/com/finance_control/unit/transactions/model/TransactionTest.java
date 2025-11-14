package com.finance_control.unit.transactions.model;

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
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transaction Model Business Logic Tests")
class TransactionTest extends BaseUnitTest {

    private Transaction transaction;
    private TransactionResponsibles responsible1;
    private TransactionResponsibles responsible2;
    private User testUser;
    private TransactionCategory testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testCategory = new TransactionCategory();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(1000.00));
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(testUser);
        transaction.setCategory(testCategory);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setSubtype(TransactionSubtype.VARIABLE);
        transaction.setSource(TransactionSource.CASH);
        transaction.setResponsibilities(new ArrayList<>());

        responsible1 = new TransactionResponsibles();
        responsible1.setId(1L);
        responsible1.setName("Responsible 1");

        responsible2 = new TransactionResponsibles();
        responsible2.setId(2L);
        responsible2.setName("Responsible 2");
    }

    @Test
    @DisplayName("getTotalPercentage_ShouldReturnZero_WhenNoResponsibilities")
    void getTotalPercentage_ShouldReturnZero_WhenNoResponsibilities() {
        BigDecimal total = transaction.getTotalPercentage();
        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getTotalPercentage_ShouldReturnCorrectTotal_WhenSingleResponsibility")
    void getTotalPercentage_ShouldReturnCorrectTotal_WhenSingleResponsibility() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(100.00));
        BigDecimal total = transaction.getTotalPercentage();
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("getTotalPercentage_ShouldReturnCorrectTotal_WhenMultipleResponsibilities")
    void getTotalPercentage_ShouldReturnCorrectTotal_WhenMultipleResponsibilities() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(60.00));
        transaction.addResponsible(responsible2, BigDecimal.valueOf(40.00));
        BigDecimal total = transaction.getTotalPercentage();
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("getTotalPercentage_ShouldHandleDecimalPercentages")
    void getTotalPercentage_ShouldHandleDecimalPercentages() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(33.33));
        transaction.addResponsible(responsible2, BigDecimal.valueOf(66.67));
        BigDecimal total = transaction.getTotalPercentage();
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("isPercentageValid_ShouldReturnFalse_WhenNoResponsibilities")
    void isPercentageValid_ShouldReturnFalse_WhenNoResponsibilities() {
        assertThat(transaction.isPercentageValid()).isFalse();
    }

    @Test
    @DisplayName("isPercentageValid_ShouldReturnTrue_WhenTotalIs100Percent")
    void isPercentageValid_ShouldReturnTrue_WhenTotalIs100Percent() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(100.00));
        assertThat(transaction.isPercentageValid()).isTrue();
    }

    @Test
    @DisplayName("isPercentageValid_ShouldReturnTrue_WhenMultipleResponsibilitiesTotal100")
    void isPercentageValid_ShouldReturnTrue_WhenMultipleResponsibilitiesTotal100() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(50.00));
        transaction.addResponsible(responsible2, BigDecimal.valueOf(50.00));
        assertThat(transaction.isPercentageValid()).isTrue();
    }

    @Test
    @DisplayName("isPercentageValid_ShouldReturnFalse_WhenTotalLessThan100")
    void isPercentageValid_ShouldReturnFalse_WhenTotalLessThan100() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(50.00));
        assertThat(transaction.isPercentageValid()).isFalse();
    }

    @Test
    @DisplayName("isPercentageValid_ShouldReturnFalse_WhenTotalGreaterThan100")
    void isPercentageValid_ShouldReturnFalse_WhenTotalGreaterThan100() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(60.00));
        transaction.addResponsible(responsible2, BigDecimal.valueOf(50.00));
        assertThat(transaction.isPercentageValid()).isFalse();
    }

    @Test
    @DisplayName("getAmountForResponsible_ShouldReturnZero_WhenNoResponsibilities")
    void getAmountForResponsible_ShouldReturnZero_WhenNoResponsibilities() {
        BigDecimal amount = transaction.getAmountForResponsible(responsible1);
        assertThat(amount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getAmountForResponsible_ShouldReturnCorrectAmount_WhenSingleResponsibility")
    void getAmountForResponsible_ShouldReturnCorrectAmount_WhenSingleResponsibility() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(100.00));
        BigDecimal amount = transaction.getAmountForResponsible(responsible1);
        assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }

    @Test
    @DisplayName("getAmountForResponsible_ShouldReturnCorrectAmount_WhenMultipleResponsibilities")
    void getAmountForResponsible_ShouldReturnCorrectAmount_WhenMultipleResponsibilities() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(60.00));
        transaction.addResponsible(responsible2, BigDecimal.valueOf(40.00));
        BigDecimal amount = transaction.getAmountForResponsible(responsible1);
        assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(600.00));
    }

    @Test
    @DisplayName("getAmountForResponsible_ShouldReturnZero_WhenResponsibleNotFound")
    void getAmountForResponsible_ShouldReturnZero_WhenResponsibleNotFound() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(100.00));
        BigDecimal amount = transaction.getAmountForResponsible(responsible2);
        assertThat(amount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getAmountForResponsible_ShouldHandleDecimalPercentages")
    void getAmountForResponsible_ShouldHandleDecimalPercentages() {
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.addResponsible(responsible1, BigDecimal.valueOf(33.33));
        BigDecimal amount = transaction.getAmountForResponsible(responsible1);
        assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(33.33));
    }

    @Test
    @DisplayName("addResponsible_ShouldAddResponsibility_WhenCalledWithTwoParameters")
    void addResponsible_ShouldAddResponsibility_WhenCalledWithTwoParameters() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(100.00));
        assertThat(transaction.getResponsibilities()).hasSize(1);
        assertThat(transaction.getTotalPercentage()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("addResponsible_ShouldCalculateAmount_WhenCalledWithTwoParameters")
    void addResponsible_ShouldCalculateAmount_WhenCalledWithTwoParameters() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(50.00));
        TransactionResponsibility responsibility = transaction.getResponsibilities().get(0);
        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }

    @Test
    @DisplayName("addResponsible_ShouldAddResponsibilityWithNotes_WhenCalledWithThreeParameters")
    void addResponsible_ShouldAddResponsibilityWithNotes_WhenCalledWithThreeParameters() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(100.00), "Test notes");
        assertThat(transaction.getResponsibilities()).hasSize(1);
        TransactionResponsibility responsibility = transaction.getResponsibilities().get(0);
        assertThat(responsibility.getNotes()).isEqualTo("Test notes");
    }

    @Test
    @DisplayName("addResponsible_ShouldCalculateAmount_WhenCalledWithThreeParameters")
    void addResponsible_ShouldCalculateAmount_WhenCalledWithThreeParameters() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(75.00), "Test notes");
        TransactionResponsibility responsibility = transaction.getResponsibilities().get(0);
        assertThat(responsibility.getCalculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(750.00));
    }

    @Test
    @DisplayName("addResponsible_ShouldAllowMultipleResponsibilities")
    void addResponsible_ShouldAllowMultipleResponsibilities() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(60.00));
        transaction.addResponsible(responsible2, BigDecimal.valueOf(40.00));
        assertThat(transaction.getResponsibilities()).hasSize(2);
        assertThat(transaction.getTotalPercentage()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("getAmountForResponsible_ShouldSumAmounts_WhenSameResponsibleAddedMultipleTimes")
    void getAmountForResponsible_ShouldSumAmounts_WhenSameResponsibleAddedMultipleTimes() {
        transaction.addResponsible(responsible1, BigDecimal.valueOf(30.00));
        transaction.addResponsible(responsible1, BigDecimal.valueOf(20.00));
        BigDecimal amount = transaction.getAmountForResponsible(responsible1);
        assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }
}
