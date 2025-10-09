package com.finance_control.brazilian_market.dto;

import com.finance_control.brazilian_market.model.FII;
import com.finance_control.shared.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for FII (Real Estate Investment Fund) data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FIIDTO extends BaseDTO {

    @NotBlank(message = "Ticker is required")
    private String ticker;

    @NotBlank(message = "Fund name is required")
    private String fundName;

    private String description;

    @NotNull(message = "FII type is required")
    private FII.FIIType fiiType;

    @NotNull(message = "FII segment is required")
    private FII.FIISegment segment;

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

    private BigDecimal pvpRatio;

    private LocalDateTime lastUpdated;

    private Boolean isActive = true;

    private Long userId;

    @Override
    public void validateCreate() {
        super.validateCreate();
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker is required");
        }
        if (fundName == null || fundName.trim().isEmpty()) {
            throw new IllegalArgumentException("Fund name is required");
        }
        if (fiiType == null) {
            throw new IllegalArgumentException("FII type is required");
        }
        if (segment == null) {
            throw new IllegalArgumentException("FII segment is required");
        }
    }

    @Override
    public void validateUpdate() {
        super.validateUpdate();
        // For updates, only validate non-null fields
        if (ticker != null && ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be empty");
        }
        if (fundName != null && fundName.trim().isEmpty()) {
            throw new IllegalArgumentException("Fund name cannot be empty");
        }
    }
}
