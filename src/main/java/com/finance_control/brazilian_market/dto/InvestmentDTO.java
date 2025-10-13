package com.finance_control.brazilian_market.dto;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.shared.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Investment data.
 * Represents all types of investments (stocks, FIIs, bonds, ETFs, etc.) in a unified format.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InvestmentDTO extends BaseDTO<Long> {

    @NotBlank(message = "Ticker is required")
    @Size(max = 20, message = "Ticker must not exceed 20 characters")
    private String ticker;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Investment type is required")
    private Investment.InvestmentType investmentType;

    private Investment.InvestmentSubtype investmentSubtype;

    @Size(max = 50, message = "Market segment must not exceed 50 characters")
    private String marketSegment;

    @Size(max = 100, message = "Sector must not exceed 100 characters")
    private String sector;

    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    @Positive(message = "Current price must be positive")
    private BigDecimal currentPrice;

    private BigDecimal previousClose;

    private BigDecimal dayChange;

    private BigDecimal dayChangePercent;

    private Long volume;

    private BigDecimal marketCap;

    private BigDecimal dividendYield;

    private BigDecimal lastDividend;

    private LocalDate lastDividendDate;

    private BigDecimal netWorth;

    private BigDecimal pVpRatio;

    private BigDecimal interestRate;

    private BigDecimal yieldToMaturity;

    private LocalDate maturityDate;

    @Size(max = 10, message = "Credit rating must not exceed 10 characters")
    private String creditRating;

    @Size(max = 20, message = "Exchange must not exceed 20 characters")
    private String exchange = "B3";

    @Size(max = 3, message = "Currency must not exceed 3 characters")
    private String currency = "BRL";

    private Boolean isActive = true;

    private LocalDateTime lastUpdated;

    private Long userId;

    @Override
    public void validateCreate() {
        super.validateCreate();
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (investmentType == null) {
            throw new IllegalArgumentException("Investment type is required");
        }
    }

    @Override
    public void validateUpdate() {
        super.validateUpdate();
        // For updates, only validate non-null fields
        if (ticker != null && ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be empty");
        }
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }

    /**
     * Helper method to check if this is a stock investment
     */
    public boolean isStock() {
        return Investment.InvestmentType.STOCK.equals(investmentType);
    }

    /**
     * Helper method to check if this is a FII investment
     */
    public boolean isFii() {
        return Investment.InvestmentType.FII.equals(investmentType);
    }

    /**
     * Helper method to check if this is a bond investment
     */
    public boolean isBond() {
        return Investment.InvestmentType.BOND.equals(investmentType);
    }

    /**
     * Helper method to get display name with ticker
     */
    public String getDisplayName() {
        return String.format("%s (%s)", name, ticker);
    }
}
