package com.finance_control.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Actionable financial recommendation provided by the AI model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRecommendationDTO {

    private String title;

    private String description;
}
