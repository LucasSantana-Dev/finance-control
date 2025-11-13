package com.finance_control.dashboard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for generating AI-backed financial predictions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialPredictionRequest {

    private static final int DEFAULT_HISTORY_MONTHS = 6;
    private static final int DEFAULT_FORECAST_MONTHS = 3;

    @Min(value = 3, message = "History window must be at least 3 months")
    @Max(value = 24, message = "History window cannot exceed 24 months")
    @Builder.Default
    private Integer historyMonths = DEFAULT_HISTORY_MONTHS;

    @Min(value = 1, message = "Forecast horizon must be at least 1 month")
    @Max(value = 12, message = "Forecast horizon cannot exceed 12 months")
    @Builder.Default
    private Integer forecastMonths = DEFAULT_FORECAST_MONTHS;

    @Size(max = 400, message = "Financial goal must contain at most 400 characters")
    private String financialGoal;

    @Size(max = 400, message = "Additional context must contain at most 400 characters")
    private String additionalContext;

    public int resolvedHistoryMonths() {
        return historyMonths != null ? historyMonths : DEFAULT_HISTORY_MONTHS;
    }

    public int resolvedForecastMonths() {
        return forecastMonths != null ? forecastMonths : DEFAULT_FORECAST_MONTHS;
    }
}
