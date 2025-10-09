package com.finance_control.brazilian_market.dto;

import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.shared.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for market indicators and economic data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MarketIndicatorDTO extends BaseDTO {

    @NotBlank(message = "Indicator code is required")
    private String code;

    @NotBlank(message = "Indicator name is required")
    private String name;

    private String description;

    @NotNull(message = "Indicator type is required")
    private MarketIndicator.IndicatorType indicatorType;

    @NotNull(message = "Frequency is required")
    private MarketIndicator.Frequency frequency;

    private BigDecimal currentValue;

    private BigDecimal previousValue;

    private BigDecimal changeValue;

    private BigDecimal changePercent;

    private LocalDate referenceDate;

    private LocalDateTime lastUpdated;

    private Boolean isActive = true;

    @Override
    public void validateCreate() {
        super.validateCreate();
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Indicator code is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Indicator name is required");
        }
        if (indicatorType == null) {
            throw new IllegalArgumentException("Indicator type is required");
        }
        if (frequency == null) {
            throw new IllegalArgumentException("Frequency is required");
        }
    }

    @Override
    public void validateUpdate() {
        super.validateUpdate();
        // For updates, only validate non-null fields
        if (code != null && code.trim().isEmpty()) {
            throw new IllegalArgumentException("Indicator code cannot be empty");
        }
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Indicator name cannot be empty");
        }
    }
}
