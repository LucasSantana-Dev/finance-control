package com.finance_control.brazilian_market.model;

import com.finance_control.shared.model.BaseModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a Brazilian stock traded on B3.
 * Includes common stocks, preferred stocks, and other equity instruments.
 */
@Entity
@Table(name = "brazilian_stocks")
@Getter
@Setter
@ToString(exclude = {"user"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BrazilianStock extends BaseModel<Long> {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String ticker;

    @NotBlank
    @Column(nullable = false)
    private String companyName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private StockType stockType;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private MarketSegment segment;

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

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.finance_control.users.model.User user;

    /**
     * Enum representing different types of Brazilian stocks.
     */
    public enum StockType {
        /** Common stock */
        ORDINARY,
        /** Preferred stock */
        PREFERRED,
        /** Unit (combination of common and preferred) */
        UNIT,
        /** Real Estate Investment Fund */
        FII,
        /** Exchange Traded Fund */
        ETF,
        /** Other equity instruments */
        OTHER
    }

    /**
     * Enum representing market segments on B3.
     */
    public enum MarketSegment {
        /** Novo Mercado */
        NOVO_MERCADO,
        /** Level 2 */
        LEVEL_2,
        /** Level 1 */
        LEVEL_1,
        /** Traditional */
        TRADITIONAL,
        /** Bovespa Mais */
        BOVESPA_MAIS,
        /** Bovespa Mais Level 2 */
        BOVESPA_MAIS_LEVEL_2,
        /** Other segments */
        OTHER
    }

    /**
     * Calculates the day change percentage.
     */
    public BigDecimal calculateDayChangePercent() {
        if (currentPrice == null || previousClose == null || previousClose.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        dayChange = currentPrice.subtract(previousClose);
        dayChangePercent = dayChange.divide(previousClose, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return dayChangePercent;
    }

    /**
     * Updates the stock price and calculates changes.
     */
    public void updatePrice(BigDecimal newPrice) {
        if (currentPrice != null) {
            previousClose = currentPrice;
        }
        currentPrice = newPrice;
        calculateDayChangePercent();
        lastUpdated = LocalDateTime.now();
    }
}
