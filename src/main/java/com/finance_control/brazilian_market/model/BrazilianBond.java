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
 * Entity representing Brazilian fixed income securities.
 * Includes CDB, RDB, LCI, LCA, and other fixed income instruments.
 */
@Entity
@Table(name = "brazilian_bonds")
@Getter
@Setter
@ToString(exclude = {"user"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BrazilianBond extends BaseModel<Long> {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String ticker;

    @NotBlank
    @Column(nullable = false)
    private String issuerName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private BondType bondType;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private IndexType indexType;

    @Column(name = "face_value", precision = 19, scale = 2)
    private BigDecimal faceValue;

    @Column(name = "current_price", precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "interest_rate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "yield_to_maturity", precision = 8, scale = 4)
    private BigDecimal yieldToMaturity;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "last_coupon_date")
    private LocalDate lastCouponDate;

    @Column(name = "next_coupon_date")
    private LocalDate nextCouponDate;

    @Column(name = "coupon_frequency")
    private Integer couponFrequency; // in months

    @Column(name = "credit_rating")
    private String creditRating;

    @Column(name = "liquidity")
    private String liquidity;

    @Column(name = "minimum_investment", precision = 19, scale = 2)
    private BigDecimal minimumInvestment;

    @Column(name = "is_tax_free")
    private Boolean isTaxFree = false;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.finance_control.users.model.User user;

    public enum BondType {
        CDB,
        RDB,
        LCI,
        LCA,
        LF,
        DEBENTURE,
        TESOURO_DIRETO,
        OTHER
    }

    public enum IndexType {
        CDI,
        IPCA,
        SELIC,
        PREFIXADO,
        IGP_M,
        OTHER
    }

    public long getDaysToMaturity() {
        if (maturityDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), maturityDate);
    }

    public BigDecimal getYearsToMaturity() {
        long days = getDaysToMaturity();
        return BigDecimal.valueOf(days).divide(BigDecimal.valueOf(365), 4, java.math.RoundingMode.HALF_UP);
    }

    public boolean isNearMaturity() {
        return getDaysToMaturity() <= 30;
    }

    public void updatePrice(BigDecimal newPrice) {
        currentPrice = newPrice;
        lastUpdated = LocalDateTime.now();
    }
}
