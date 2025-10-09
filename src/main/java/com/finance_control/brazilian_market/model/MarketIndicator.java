package com.finance_control.brazilian_market.model;

import com.finance_control.shared.model.BaseModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing Brazilian market indicators and economic data.
 * Includes Selic rate, CDI, IPCA, and other economic indicators from BCB.
 */
@Entity
@Table(name = "market_indicators")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MarketIndicator extends BaseModel<Long> {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String code;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private IndicatorType indicatorType;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Frequency frequency;

    @Column(name = "current_value", precision = 19, scale = 6)
    private BigDecimal currentValue;

    @Column(name = "previous_value", precision = 19, scale = 6)
    private BigDecimal previousValue;

    @Column(name = "change_value", precision = 19, scale = 6)
    private BigDecimal changeValue;

    @Column(name = "change_percent", precision = 8, scale = 4)
    private BigDecimal changePercent;

    @Column(name = "reference_date")
    private LocalDate referenceDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Enum representing different types of market indicators.
     */
    public enum IndicatorType {
        /** Interest rates */
        INTEREST_RATE,
        /** Inflation indices */
        INFLATION,
        /** Exchange rates */
        EXCHANGE_RATE,
        /** Stock market indices */
        STOCK_INDEX,
        /** Economic indicators */
        ECONOMIC,
        /** Other indicators */
        OTHER
    }

    /**
     * Enum representing data frequency.
     */
    public enum Frequency {
        /** Daily */
        DAILY,
        /** Weekly */
        WEEKLY,
        /** Monthly */
        MONTHLY,
        /** Quarterly */
        QUARTERLY,
        /** Annually */
        ANNUALLY,
        /** Irregular */
        IRREGULAR
    }

    /**
     * Calculates the change value and percentage.
     */
    public void calculateChanges() {
        if (currentValue != null && previousValue != null) {
            changeValue = currentValue.subtract(previousValue);

            if (previousValue.compareTo(BigDecimal.ZERO) != 0) {
                changePercent = changeValue.divide(previousValue, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            } else {
                changePercent = BigDecimal.ZERO;
            }
        }
    }

    /**
     * Updates the indicator value and calculates changes.
     */
    public void updateValue(BigDecimal newValue, LocalDate referenceDate) {
        if (currentValue != null) {
            previousValue = currentValue;
        }
        currentValue = newValue;
        this.referenceDate = referenceDate;
        calculateChanges();
        lastUpdated = LocalDateTime.now();
    }

    /**
     * Checks if the indicator is a key economic indicator.
     */
    public boolean isKeyIndicator() {
        return indicatorType == IndicatorType.INTEREST_RATE ||
               indicatorType == IndicatorType.INFLATION ||
               code.equals("SELIC") ||
               code.equals("CDI") ||
               code.equals("IPCA");
    }
}
