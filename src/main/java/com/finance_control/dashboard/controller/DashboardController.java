package com.finance_control.dashboard.controller;

import com.finance_control.dashboard.dto.*;
import com.finance_control.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        log.debug("GET request to retrieve financial metrics from {} to {}", startDate, endDate);
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
}
