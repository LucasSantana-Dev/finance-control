package com.finance_control.unit.brazilian_market.model;

import com.finance_control.brazilian_market.model.MarketIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MarketIndicatorTest {

    private MarketIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new MarketIndicator();
        indicator.setCode("TEST");
        indicator.setName("Test Indicator");
        indicator.setIndicatorType(MarketIndicator.IndicatorType.ECONOMIC);
        indicator.setFrequency(MarketIndicator.Frequency.MONTHLY);
    }

    @Test
    void calculateChanges_WithBothValuesNotNull_ShouldCalculateChangeValue() {
        indicator.setCurrentValue(BigDecimal.valueOf(10.50));
        indicator.setPreviousValue(BigDecimal.valueOf(10.00));

        indicator.calculateChanges();

        assertThat(indicator.getChangeValue()).isEqualTo(BigDecimal.valueOf(0.50));
    }

    @Test
    void calculateChanges_WithBothValuesNotNull_ShouldCalculateChangePercent() {
        indicator.setCurrentValue(BigDecimal.valueOf(11.00));
        indicator.setPreviousValue(BigDecimal.valueOf(10.00));

        indicator.calculateChanges();

        assertThat(indicator.getChangePercent()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
    }

    @Test
    void calculateChanges_WithPreviousValueZero_ShouldSetChangePercentToZero() {
        indicator.setCurrentValue(BigDecimal.valueOf(10.00));
        indicator.setPreviousValue(BigDecimal.ZERO);

        indicator.calculateChanges();

        assertThat(indicator.getChangeValue()).isEqualTo(BigDecimal.valueOf(10.00));
        assertThat(indicator.getChangePercent()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void calculateChanges_WithNullCurrentValue_ShouldNotCalculate() {
        indicator.setCurrentValue(null);
        indicator.setPreviousValue(BigDecimal.valueOf(10.00));

        indicator.calculateChanges();

        assertThat(indicator.getChangeValue()).isNull();
        assertThat(indicator.getChangePercent()).isNull();
    }

    @Test
    void calculateChanges_WithNullPreviousValue_ShouldNotCalculate() {
        indicator.setCurrentValue(BigDecimal.valueOf(10.00));
        indicator.setPreviousValue(null);

        indicator.calculateChanges();

        assertThat(indicator.getChangeValue()).isNull();
        assertThat(indicator.getChangePercent()).isNull();
    }

    @Test
    void calculateChanges_WithBothValuesNull_ShouldNotCalculate() {
        indicator.setCurrentValue(null);
        indicator.setPreviousValue(null);

        indicator.calculateChanges();

        assertThat(indicator.getChangeValue()).isNull();
        assertThat(indicator.getChangePercent()).isNull();
    }

    @Test
    void updateValue_WithNullCurrentValue_ShouldSetNewValue() {
        indicator.setCurrentValue(null);
        BigDecimal newValue = BigDecimal.valueOf(10.00);
        LocalDate referenceDate = LocalDate.now();

        indicator.updateValue(newValue, referenceDate);

        assertThat(indicator.getCurrentValue()).isEqualTo(newValue);
        assertThat(indicator.getPreviousValue()).isNull();
        assertThat(indicator.getReferenceDate()).isEqualTo(referenceDate);
        assertThat(indicator.getLastUpdated()).isNotNull();
    }

    @Test
    void updateValue_WithNonNullCurrentValue_ShouldMoveCurrentToPrevious() {
        BigDecimal oldValue = BigDecimal.valueOf(10.00);
        BigDecimal newValue = BigDecimal.valueOf(11.00);
        indicator.setCurrentValue(oldValue);
        LocalDate referenceDate = LocalDate.now();

        indicator.updateValue(newValue, referenceDate);

        assertThat(indicator.getCurrentValue()).isEqualTo(newValue);
        assertThat(indicator.getPreviousValue()).isEqualTo(oldValue);
        assertThat(indicator.getReferenceDate()).isEqualTo(referenceDate);
        assertThat(indicator.getLastUpdated()).isNotNull();
    }

    @Test
    void isKeyIndicator_WithInterestRateType_ShouldReturnTrue() {
        indicator.setIndicatorType(MarketIndicator.IndicatorType.INTEREST_RATE);

        boolean result = indicator.isKeyIndicator();

        assertThat(result).isTrue();
    }

    @Test
    void isKeyIndicator_WithInflationType_ShouldReturnTrue() {
        indicator.setIndicatorType(MarketIndicator.IndicatorType.INFLATION);

        boolean result = indicator.isKeyIndicator();

        assertThat(result).isTrue();
    }

    @Test
    void isKeyIndicator_WithSELICCode_ShouldReturnTrue() {
        indicator.setCode("SELIC");
        indicator.setIndicatorType(MarketIndicator.IndicatorType.ECONOMIC);

        boolean result = indicator.isKeyIndicator();

        assertThat(result).isTrue();
    }

    @Test
    void isKeyIndicator_WithCDICode_ShouldReturnTrue() {
        indicator.setCode("CDI");
        indicator.setIndicatorType(MarketIndicator.IndicatorType.ECONOMIC);

        boolean result = indicator.isKeyIndicator();

        assertThat(result).isTrue();
    }

    @Test
    void isKeyIndicator_WithIPCACode_ShouldReturnTrue() {
        indicator.setCode("IPCA");
        indicator.setIndicatorType(MarketIndicator.IndicatorType.ECONOMIC);

        boolean result = indicator.isKeyIndicator();

        assertThat(result).isTrue();
    }

    @Test
    void isKeyIndicator_WithOtherTypeAndCode_ShouldReturnFalse() {
        indicator.setIndicatorType(MarketIndicator.IndicatorType.ECONOMIC);
        indicator.setCode("OTHER");

        boolean result = indicator.isKeyIndicator();

        assertThat(result).isFalse();
    }

    @Test
    void isKeyIndicator_WithExchangeRateType_ShouldReturnFalse() {
        indicator.setIndicatorType(MarketIndicator.IndicatorType.EXCHANGE_RATE);
        indicator.setCode("USD");

        boolean result = indicator.isKeyIndicator();

        assertThat(result).isFalse();
    }

    @Test
    void calculateChanges_WithNegativeChange_ShouldCalculateCorrectly() {
        indicator.setCurrentValue(BigDecimal.valueOf(9.00));
        indicator.setPreviousValue(BigDecimal.valueOf(10.00));

        indicator.calculateChanges();

        assertThat(indicator.getChangeValue()).isEqualByComparingTo(BigDecimal.valueOf(-1.00));
        assertThat(indicator.getChangePercent()).isEqualByComparingTo(BigDecimal.valueOf(-10.0));
    }

    @Test
    void updateValue_ShouldCallCalculateChanges() {
        indicator.setCurrentValue(BigDecimal.valueOf(10.00));
        indicator.setPreviousValue(BigDecimal.valueOf(9.00));
        BigDecimal newValue = BigDecimal.valueOf(11.00);
        LocalDate referenceDate = LocalDate.now();

        indicator.updateValue(newValue, referenceDate);

        assertThat(indicator.getChangeValue()).isNotNull();
        assertThat(indicator.getChangePercent()).isNotNull();
    }
}
