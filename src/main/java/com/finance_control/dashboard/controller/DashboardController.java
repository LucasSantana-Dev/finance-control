package com.finance_control.dashboard.controller;

import com.finance_control.dashboard.dto.*;
import com.finance_control.dashboard.service.DashboardService;
import com.finance_control.dashboard.service.FinancialPredictionService;
import com.finance_control.shared.feature.Feature;
import com.finance_control.shared.feature.FeatureFlagService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for dashboard operations.
 * Provides endpoints for financial dashboard data and metrics.
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Financial dashboard endpoints for metrics and analytics")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ObjectProvider<FinancialPredictionService> financialPredictionServiceProvider;
    private final FeatureFlagService featureFlagService;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary",
               description = "Retrieve comprehensive dashboard summary with key financial metrics")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        log.debug("GET request to retrieve dashboard summary");
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get financial metrics",
               description = "Retrieve detailed financial metrics for a specific period")
    public ResponseEntity<FinancialMetricsDTO> getFinancialMetrics(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("GET request to retrieve financial metrics (dates present: {}, {})", startDate != null, endDate != null);
        FinancialMetricsDTO metrics = dashboardService.getFinancialMetrics(startDate, endDate);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/spending-categories")
    @Operation(summary = "Get top spending categories",
               description = "Retrieve top spending categories for chart visualization")
    public ResponseEntity<List<CategorySpendingDTO>> getTopSpendingCategories(
            @Parameter(description = "Number of top categories to return")
            @RequestParam(defaultValue = "5") int limit) {
        log.debug("GET request to retrieve top {} spending categories", limit);
        List<CategorySpendingDTO> categories = dashboardService.getTopSpendingCategories(
                com.finance_control.shared.context.UserContext.getCurrentUserId(), limit);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/monthly-trends")
    @Operation(summary = "Get monthly trends",
               description = "Retrieve monthly income/expense trends for chart visualization")
    public ResponseEntity<List<MonthlyTrendDTO>> getMonthlyTrends(
            @Parameter(description = "Number of months to include")
            @RequestParam(defaultValue = "12") int months) {
        log.debug("GET request to retrieve monthly trends for last {} months", months);
        List<MonthlyTrendDTO> trends = dashboardService.getMonthlyTrends(
                com.finance_control.shared.context.UserContext.getCurrentUserId(), months);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/current-month-metrics")
    @Operation(summary = "Get current month metrics",
               description = "Retrieve financial metrics for the current month")
    public ResponseEntity<FinancialMetricsDTO> getCurrentMonthMetrics() {
        log.debug("GET request to retrieve current month metrics");
        LocalDate startOfMonth = java.time.YearMonth.now().atDay(1);
        LocalDate endOfMonth = java.time.YearMonth.now().atEndOfMonth();
        FinancialMetricsDTO metrics = dashboardService.getFinancialMetrics(startOfMonth, endOfMonth);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/year-to-date-metrics")
    @Operation(summary = "Get year-to-date metrics",
               description = "Retrieve financial metrics for the current year")
    public ResponseEntity<FinancialMetricsDTO> getYearToDateMetrics() {
        log.debug("GET request to retrieve year-to-date metrics");
        LocalDate startOfYear = java.time.YearMonth.now().atDay(1).withDayOfYear(1);
        LocalDate endOfYear = java.time.YearMonth.now().atEndOfMonth().withDayOfYear(365);
        FinancialMetricsDTO metrics = dashboardService.getFinancialMetrics(startOfYear, endOfYear);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping
    @Operation(summary = "Get dashboard data with filtering",
               description = "Retrieve dashboard data with flexible filtering and date range options")
    public ResponseEntity<Object> getDashboardData(
            @Parameter(description = "Type of dashboard data to retrieve " +
                    "(summary, metrics, spending-categories, monthly-trends, " +
                    "current-month-metrics, year-to-date-metrics)")
            @RequestParam String data,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Number of items to return (for lists)")
            @RequestParam(required = false, defaultValue = "10") int limit,
            @Parameter(description = "Number of months for trends")
            @RequestParam(required = false, defaultValue = "12") int months) {

        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("Data parameter is required");
        }

        log.debug("GET request to retrieve dashboard data (data length: {})", data.length());

        return switch (data) {
            case "summary" -> ResponseEntity.ok(dashboardService.getDashboardSummary());
            case "metrics" -> {
                if (startDate == null || endDate == null) {
                    throw new IllegalArgumentException("Start date and end date are required for metrics data");
                }
                yield ResponseEntity.ok(dashboardService.getFinancialMetrics(startDate, endDate));
            }
            case "spending-categories" -> ResponseEntity.ok(dashboardService.getTopSpendingCategories(
                    com.finance_control.shared.context.UserContext.getCurrentUserId(), limit));
            case "monthly-trends" -> ResponseEntity.ok(dashboardService.getMonthlyTrends(
                    com.finance_control.shared.context.UserContext.getCurrentUserId(), months));
            case "current-month-metrics" -> {
                LocalDate startOfMonth = java.time.YearMonth.now().atDay(1);
                LocalDate endOfMonth = java.time.YearMonth.now().atEndOfMonth();
                yield ResponseEntity.ok(dashboardService.getFinancialMetrics(startOfMonth, endOfMonth));
            }
            case "year-to-date-metrics" -> {
                LocalDate startOfYear = java.time.YearMonth.now().atDay(1).withDayOfYear(1);
                LocalDate endOfYear = java.time.YearMonth.now().atEndOfMonth().withDayOfYear(365);
                yield ResponseEntity.ok(dashboardService.getFinancialMetrics(startOfYear, endOfYear));
            }
            default -> throw new IllegalArgumentException("Invalid data type: " + data);
        };
    }

    @PostMapping("/predictions")
    @Operation(summary = "Generate financial predictions",
               description = "Produce AI-assisted financial forecasts and actionable recommendations")
    public ResponseEntity<FinancialPredictionResponse> generateFinancialPredictions(
            @Valid @RequestBody FinancialPredictionRequest request) {
        log.debug("POST request to generate financial predictions");

        featureFlagService.requireEnabled(Feature.FINANCIAL_PREDICTIONS);

        FinancialPredictionService predictionService = financialPredictionServiceProvider.getIfAvailable();
        if (predictionService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Financial predictions are disabled. Configure OpenAI credentials to enable this endpoint.");
        }

        FinancialPredictionResponse response = predictionService.generatePrediction(request);
        return ResponseEntity.ok(response);
    }
}
