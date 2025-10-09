package com.finance_control.brazilian_market.dto;

import com.finance_control.brazilian_market.model.BrazilianStock;
import com.finance_control.shared.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Brazilian stock data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BrazilianStockDTO extends BaseDTO {

    @NotBlank(message = "Ticker is required")
    private String ticker;

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String description;

    @NotNull(message = "Stock type is required")
    private BrazilianStock.StockType stockType;

    @NotNull(message = "Market segment is required")
    private BrazilianStock.MarketSegment segment;

    @Positive(message = "Current price must be positive")
    private BigDecimal currentPrice;

    private BigDecimal previousClose;

    private BigDecimal dayChange;

    private BigDecimal dayChangePercent;

    private Long volume;

    private BigDecimal marketCap;

    private LocalDateTime lastUpdated;

    private Boolean isActive = true;

    private Long userId;

    @Override
    public void validateCreate() {
        super.validateCreate();
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker is required");
        }
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (stockType == null) {
            throw new IllegalArgumentException("Stock type is required");
        }
        if (segment == null) {
            throw new IllegalArgumentException("Market segment is required");
        }
    }

    @Override
    public void validateUpdate() {
        super.validateUpdate();
        // For updates, only validate non-null fields
        if (ticker != null && ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be empty");
        }
        if (companyName != null && companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
    }
}
