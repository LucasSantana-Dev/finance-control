package com.finance_control.dashboard.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Response structure produced after generating financial predictions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialPredictionResponse {

    private String summary;

    @Singular("forecastMonth")
    private List<ForecastedMonthDTO> forecast;

    @Singular("recommendation")
    private List<PredictionRecommendationDTO> recommendations;

    private String rawModelResponse;
}
