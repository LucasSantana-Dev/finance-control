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
 * Entity representing a Brazilian Real Estate Investment Fund (FII).
 * FIIs are investment funds that invest in real estate assets and distribute dividends.
 */
@Entity
@Table(name = "fii_funds")
@Getter
@Setter
@ToString(exclude = {"user"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FII extends BaseModel<Long> {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String ticker;

    @NotBlank
    @Column(nullable = false)
    private String fundName;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private FIIType fiiType;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private FIISegment segment;

    @Column(name = "current_price", precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "previous_close", precision = 19, scale = 2)
    private BigDecimal previousClose;

    @Column(name = "day_change", precision = 19, scale = 2)
    private BigDecimal dayChange;

    @Column(name = "day_change_percent", precision = 8, scale = 4)
    private BigDecimal dayChangePercent;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "market_cap", precision = 19, scale = 2)
    private BigDecimal marketCap;

    @Column(name = "dividend_yield", precision = 8, scale = 4)
    private BigDecimal dividendYield;

    @Column(name = "last_dividend", precision = 19, scale = 2)
    private BigDecimal lastDividend;

    @Column(name = "last_dividend_date")
    private LocalDate lastDividendDate;

    @Column(name = "net_worth", precision = 19, scale = 2)
    private BigDecimal netWorth;

    @Column(name = "p_vp_ratio", precision = 8, scale = 4)
    private BigDecimal pvpRatio;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.finance_control.users.model.User user;

    public enum FIIType {
        TIJOLO,
        PAPEL,
        HIBRIDO,
        FUNDO_DE_FUNDOS,
        OTHER
    }

    public enum FIISegment {
        SHOPPING,
        OFFICES,
        LOGISTICS,
        RESIDENTIAL,
        HEALTHCARE,
        EDUCATIONAL,
        HOTELS,
        MIXED,
        OTHER
    }

    public BigDecimal calculateDayChangePercent() {
        if (currentPrice == null || previousClose == null || previousClose.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        dayChange = currentPrice.subtract(previousClose);
        dayChangePercent = dayChange.divide(previousClose, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return dayChangePercent;
    }

    public BigDecimal calculatePVPRatio() {
        if (currentPrice == null || netWorth == null || netWorth.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        pvpRatio = currentPrice.divide(netWorth, 4, java.math.RoundingMode.HALF_UP);
        return pvpRatio;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (currentPrice != null) {
            previousClose = currentPrice;
        }
        currentPrice = newPrice;
        calculateDayChangePercent();
        calculatePVPRatio();
        lastUpdated = LocalDateTime.now();
    }
}
