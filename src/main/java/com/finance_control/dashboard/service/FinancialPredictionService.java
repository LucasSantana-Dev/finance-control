package com.finance_control.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.dashboard.dto.CategorySpendingDTO;
import com.finance_control.dashboard.dto.FinancialMetricsDTO;
import com.finance_control.dashboard.dto.FinancialPredictionRequest;
import com.finance_control.dashboard.dto.FinancialPredictionResponse;
import com.finance_control.dashboard.dto.ForecastedMonthDTO;
import com.finance_control.dashboard.dto.MonthlyTrendDTO;
import com.finance_control.dashboard.dto.PredictionRecommendationDTO;
import com.finance_control.shared.context.UserContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service responsible for orchestrating financial prediction flows with AI support.
 */
@Service
@ConditionalOnBean(PredictionModelClient.class)
@Slf4j
public class FinancialPredictionService {

    private static final int DEFAULT_CATEGORY_SAMPLE = 5;

    private final PredictionModelClient predictionModelClient;
    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;

    public FinancialPredictionService(PredictionModelClient predictionModelClient,
                                      DashboardService dashboardService,
                                      ObjectMapper objectMapper) {
        this.predictionModelClient = predictionModelClient;
        this.dashboardService = dashboardService;
        ObjectMapper configuredMapper = objectMapper.copy();
        configuredMapper.findAndRegisterModules();
        this.objectMapper = configuredMapper;
    }

    /**
     * Generates financial predictions based on historical data and an AI provider.
     *
     * @param request configuration describing the expected forecast horizon and additional context
     * @return structured response produced by the AI model
     */
    public FinancialPredictionResponse generatePrediction(FinancialPredictionRequest request) {
        Long userId = UserContext.getCurrentUserId();
        int historyMonths = request.resolvedHistoryMonths();
        int forecastMonths = request.resolvedForecastMonths();

        List<MonthlyTrendDTO> monthlyTrends = dashboardService.getMonthlyTrends(userId, historyMonths);
        List<CategorySpendingDTO> topCategories = dashboardService.getTopSpendingCategories(userId, DEFAULT_CATEGORY_SAMPLE);

        LocalDate startDate = YearMonth.now().minusMonths(historyMonths - 1L).atDay(1);
        LocalDate endDate = LocalDate.now();
        FinancialMetricsDTO recentMetrics = dashboardService.getFinancialMetrics(startDate, endDate);

        String prompt = buildPrompt(request, monthlyTrends, topCategories, recentMetrics, forecastMonths);
        String rawResponse = predictionModelClient.generateForecast(prompt);

        return convertToResponse(rawResponse, request);
    }

    private String buildPrompt(FinancialPredictionRequest request,
                               List<MonthlyTrendDTO> monthlyTrends,
                               List<CategorySpendingDTO> topCategories,
                               FinancialMetricsDTO metrics,
                               int forecastMonths) {
        try {
            String historyJson = objectMapper.writeValueAsString(monthlyTrends);
            String categoriesJson = objectMapper.writeValueAsString(topCategories);
            String metricsJson = objectMapper.writeValueAsString(metrics);

            StringBuilder builder = new StringBuilder();
            builder.append("You are a seasoned financial planning assistant. ")
                    .append("Analyze the historical income, expenses, balances, and transaction volumes provided. ")
                    .append("Project financial performance for the next ")
                    .append(forecastMonths)
                    .append(" months for the authenticated user.\n\n")
                    .append("You must respond STRICTLY in JSON with the following structure:\n")
                    .append("{\n")
                    .append("  \"summary\": \"string\",\n")
                    .append("  \"forecast\": [\n")
                    .append("    {\n")
                    .append("      \"month\": \"YYYY-MM\",\n")
                    .append("      \"projectedIncome\": number,\n")
                    .append("      \"projectedExpenses\": number,\n")
                    .append("      \"projectedNet\": number,\n")
                    .append("      \"notes\": \"string\"\n")
                    .append("    }\n")
                    .append("  ],\n")
                    .append("  \"recommendations\": [\n")
                    .append("    {\n")
                    .append("      \"title\": \"string\",\n")
                    .append("      \"description\": \"string\"\n")
                    .append("    }\n")
                    .append("  ]\n")
                    .append("}\n\n")
                    .append("Historical monthly summary (oldest to newest):\n")
                    .append(historyJson)
                    .append("\n\n")
                    .append("Recent metrics summary for the selected period:\n")
                    .append(metricsJson)
                    .append("\n\n")
                    .append("Top spending categories for the latest month:\n")
                    .append(categoriesJson)
                    .append("\n\n")
                    .append("Forecast horizon (months): ")
                    .append(forecastMonths)
                    .append("\n");

            if (StringUtils.hasText(request.getFinancialGoal())) {
                builder.append("Primary financial goal: ").append(request.getFinancialGoal()).append("\n");
            }
            if (StringUtils.hasText(request.getAdditionalContext())) {
                builder.append("Additional context: ").append(request.getAdditionalContext()).append("\n");
            }

            builder.append("Ensure monetary values use decimals with two fractional digits. ")
                    .append("Prioritize actionable insights that align with the provided goal.\n");

            return builder.toString();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize dashboard data for AI prompt", ex);
        }
    }

    private FinancialPredictionResponse convertToResponse(String rawResponse, FinancialPredictionRequest request) {
        if (!StringUtils.hasText(rawResponse)) {
            return FinancialPredictionResponse.builder()
                    .summary("Financial predictions could not be generated. Please verify AI configuration.")
                    .rawModelResponse(rawResponse)
                    .build();
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String summary = root.path("summary").asText(null);

            List<ForecastedMonthDTO> forecast = new ArrayList<>();
            JsonNode forecastNode = root.path("forecast");
            if (forecastNode.isArray()) {
                forecastNode.forEach(node -> forecast.add(convertForecastNode(node)));
            }

            List<PredictionRecommendationDTO> recommendations = new ArrayList<>();
            JsonNode recommendationsNode = root.path("recommendations");
            if (recommendationsNode.isArray()) {
                recommendationsNode.forEach(node -> recommendations.add(convertRecommendationNode(node)));
            }

            return FinancialPredictionResponse.builder()
                    .summary(StringUtils.hasText(summary) ? summary : "Financial prediction generated")
                    .forecast(forecast)
                    .recommendations(recommendations)
                    .rawModelResponse(rawResponse)
                    .build();
        } catch (Exception ex) {
            log.warn("Unable to parse AI prediction payload: {}", ex.getMessage());
            return FinancialPredictionResponse.builder()
                    .summary("Financial prediction generated, but the structured output could not be parsed.")
                    .rawModelResponse(rawResponse)
                    .build();
        }
    }

    private ForecastedMonthDTO convertForecastNode(JsonNode node) {
        YearMonth month = parseYearMonth(node.path("month").asText(null));
        BigDecimal income = parseDecimal(node.path("projectedIncome"));
        BigDecimal expenses = parseDecimal(node.path("projectedExpenses"));
        BigDecimal net = parseDecimal(node.path("projectedNet"));
        if (net == null && income != null && expenses != null) {
            net = income.subtract(expenses);
        }

        return ForecastedMonthDTO.builder()
                .month(month)
                .projectedIncome(income)
                .projectedExpenses(expenses)
                .projectedNet(net)
                .notes(node.path("notes").asText(null))
                .build();
    }

    private PredictionRecommendationDTO convertRecommendationNode(JsonNode node) {
        return PredictionRecommendationDTO.builder()
                .title(node.path("title").asText(null))
                .description(node.path("description").asText(null))
                .build();
    }

    private YearMonth parseYearMonth(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return YearMonth.parse(value.trim());
        } catch (Exception ex) {
            log.debug("Unable to parse YearMonth '{}': {}", value, ex.getMessage());
            return null;
        }
    }

    private BigDecimal parseDecimal(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        String text = node.asText(null);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException ex) {
            log.debug("Unable to parse decimal '{}': {}", text, ex.getMessage());
            return null;
        }
    }
}
