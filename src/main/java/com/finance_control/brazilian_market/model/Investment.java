package com.finance_control.brazilian_market.model;

import com.finance_control.users.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Unified entity for all types of investments (stocks, FIIs, bonds, ETFs, etc.)
 * This replaces the separate brazilian_stocks, fii_funds, and brazilian_bonds tables
 * with a single generic table that can handle any investment type.
 */
@Entity
@Table(name = "investments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ticker", "user_id"}),
       indexes = {
           @Index(name = "idx_investments_user_id", columnList = "user_id"),
           @Index(name = "idx_investments_ticker", columnList = "ticker"),
           @Index(name = "idx_investments_investment_type", columnList = "investment_type"),
           @Index(name = "idx_investments_active_user_type", columnList = "user_id, investment_type, is_active"),
           @Index(name = "idx_investments_active_user_ticker", columnList = "user_id, ticker, is_active")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ticker is required")
    @Size(max = 20, message = "Ticker must not exceed 20 characters")
    @Column(nullable = false, length = 20)
    private String ticker;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Investment classification
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Investment type is required")
    @Column(name = "investment_type", nullable = false, length = 50)
    private InvestmentType investmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_subtype", length = 50)
    private InvestmentSubtype investmentSubtype;

    @Size(max = 50, message = "Market segment must not exceed 50 characters")
    @Column(name = "market_segment", length = 50)
    private String marketSegment;

    @Size(max = 100, message = "Sector must not exceed 100 characters")
    @Column(length = 100)
    private String sector;

    @Size(max = 100, message = "Industry must not exceed 100 characters")
    @Column(length = 100)
    private String industry;

    // Market data (from external API)
    @DecimalMin(value = "0.0", inclusive = false, message = "Current price must be positive")
    @Digits(integer = 17, fraction = 2, message = "Current price must have at most 17 integer digits and 2 decimal places")
    @Column(name = "current_price", precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Previous close must be positive")
    @Digits(integer = 17, fraction = 2, message = "Previous close must have at most 17 integer digits and 2 decimal places")
    @Column(name = "previous_close", precision = 19, scale = 2)
    private BigDecimal previousClose;

    @Digits(integer = 17, fraction = 2, message = "Day change must have at most 17 integer digits and 2 decimal places")
    @Column(name = "day_change", precision = 19, scale = 2)
    private BigDecimal dayChange;

    @DecimalMin(value = "-100.0", message = "Day change percent must be at least -100%")
    @DecimalMax(value = "1000.0", message = "Day change percent must be at most 1000%")
    @Digits(integer = 4, fraction = 4, message = "Day change percent must have at most 4 integer digits and 4 decimal places")
    @Column(name = "day_change_percent", precision = 8, scale = 4)
    private BigDecimal dayChangePercent;

    @Min(value = 0, message = "Volume must be non-negative")
    @Column
    private Long volume;

    @DecimalMin(value = "0.0", inclusive = false, message = "Market cap must be positive")
    @Digits(integer = 17, fraction = 2, message = "Market cap must have at most 17 integer digits and 2 decimal places")
    @Column(name = "market_cap", precision = 19, scale = 2)
    private BigDecimal marketCap;

    // Additional metrics (varies by investment type)
    @DecimalMin(value = "0.0", message = "Dividend yield must be non-negative")
    @DecimalMax(value = "100.0", message = "Dividend yield must be at most 100%")
    @Digits(integer = 4, fraction = 4, message = "Dividend yield must have at most 4 integer digits and 4 decimal places")
    @Column(name = "dividend_yield", precision = 8, scale = 4)
    private BigDecimal dividendYield;

    @DecimalMin(value = "0.0", inclusive = false, message = "Last dividend must be positive")
    @Digits(integer = 17, fraction = 2, message = "Last dividend must have at most 17 integer digits and 2 decimal places")
    @Column(name = "last_dividend", precision = 19, scale = 2)
    private BigDecimal lastDividend;

    @Column(name = "last_dividend_date")
    private LocalDate lastDividendDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Net worth must be positive")
    @Digits(integer = 17, fraction = 2, message = "Net worth must have at most 17 integer digits and 2 decimal places")
    @Column(name = "net_worth", precision = 19, scale = 2)
    private BigDecimal netWorth;

    @DecimalMin(value = "0.0", inclusive = false, message = "P/VP ratio must be positive")
    @Digits(integer = 4, fraction = 4, message = "P/VP ratio must have at most 4 integer digits and 4 decimal places")
    @Column(name = "p_vp_ratio", precision = 8, scale = 4)
    private BigDecimal pVpRatio;

    @DecimalMin(value = "0.0", message = "Interest rate must be non-negative")
    @DecimalMax(value = "100.0", message = "Interest rate must be at most 100%")
    @Digits(integer = 4, fraction = 4, message = "Interest rate must have at most 4 integer digits and 4 decimal places")
    @Column(name = "interest_rate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @DecimalMin(value = "0.0", message = "Yield to maturity must be non-negative")
    @DecimalMax(value = "100.0", message = "Yield to maturity must be at most 100%")
    @Digits(integer = 4, fraction = 4, message = "Yield to maturity must have at most 4 integer digits and 4 decimal places")
    @Column(name = "yield_to_maturity", precision = 8, scale = 4)
    private BigDecimal yieldToMaturity;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Size(max = 10, message = "Credit rating must not exceed 10 characters")
    @Column(name = "credit_rating", length = 10)
    private String creditRating;

    // Metadata
    @Size(max = 20, message = "Exchange must not exceed 20 characters")
    @Column(length = 20)
    @Builder.Default
    private String exchange = "B3";

    @Size(max = 3, message = "Currency must not exceed 3 characters")
    @Column(length = 3)
    @Builder.Default
    private String currency = "BRL";

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Investment types enum
     */
    public enum InvestmentType {
        STOCK("Stock"),
        FII("Real Estate Investment Fund"),
        BOND("Bond"),
        ETF("Exchange Traded Fund"),
        CRYPTO("Cryptocurrency"),
        COMMODITY("Commodity"),
        CURRENCY("Currency"),
        OTHER("Other");

        private final String description;

        InvestmentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Investment subtypes enum
     */
    public enum InvestmentSubtype {
        // Stock subtypes
        ORDINARY("Ordinary Share"),
        PREFERRED("Preferred Share"),
        UNIT("Unit"),

        // FII subtypes
        TIJOLO("Brick (Real Estate)"),
        PAPEL("Paper (Securities)"),
        HIBRIDO("Hybrid"),
        FUNDO_DE_FUNDOS("Fund of Funds"),

        // Bond subtypes
        CDB("Certificate of Bank Deposit"),
        RDB("Bank Deposit Receipt"),
        LCI("Real Estate Credit Bill"),
        LCA("Agribusiness Credit Bill"),
        LF("Financial Letter"),
        DEBENTURE("Debenture"),
        TESOURO_DIRETO("Treasury Direct"),

        // Generic
        OTHER("Other");

        private final String description;

        InvestmentSubtype(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Helper method to check if this is a stock investment
     */
    public boolean isStock() {
        return InvestmentType.STOCK.equals(investmentType);
    }

    /**
     * Helper method to check if this is a FII investment
     */
    public boolean isFii() {
        return InvestmentType.FII.equals(investmentType);
    }

    /**
     * Helper method to check if this is a bond investment
     */
    public boolean isBond() {
        return InvestmentType.BOND.equals(investmentType);
    }

    /**
     * Helper method to get display name with ticker
     */
    public String getDisplayName() {
        return String.format("%s (%s)", name, ticker);
    }

    /**
     * Helper method to calculate price change percentage
     */
    public BigDecimal calculatePriceChangePercent() {
        if (currentPrice == null || previousClose == null || previousClose.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return currentPrice.subtract(previousClose)
                .divide(previousClose, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
