package com.finance_control.reports.controller;

import com.finance_control.reports.dto.GoalReportDTO;
import com.finance_control.reports.dto.SummaryReportDTO;
import com.finance_control.reports.dto.TransactionReportDTO;
import com.finance_control.reports.service.ReportService;
import com.finance_control.shared.feature.Feature;
import com.finance_control.shared.feature.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for financial reports.
 * Provides endpoints for generating transaction, goal, and summary reports.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Financial reports endpoints for transactions, goals, and summaries")
public class ReportsController {

    private final ReportService reportService;
    private final FeatureFlagService featureFlagService;

    @GetMapping("/transactions")
    @Operation(
            summary = "Get transaction report",
            description = "Generate a transaction report with optional filters for date range, type, and category"
    )
    public ResponseEntity<TransactionReportDTO> getTransactionReport(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "Transaction type (income or expense)")
            @RequestParam(required = false) String type,
            @Parameter(description = "Category name")
            @RequestParam(required = false) String category) {

        log.debug("GET request to retrieve transaction report (dateFrom: {}, dateTo: {}, type: {}, category: {})",
                dateFrom, dateTo, type, category);

        featureFlagService.requireEnabled(Feature.REPORTS);

        TransactionReportDTO report = reportService.generateTransactionReport(dateFrom, dateTo, type, category);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/goals")
    @Operation(
            summary = "Get goal report",
            description = "Generate a goal report with optional status filter"
    )
    public ResponseEntity<GoalReportDTO> getGoalReport(
            @Parameter(description = "Goal status (active, completed, paused, cancelled)")
            @RequestParam(required = false) String status) {

        log.debug("GET request to retrieve goal report (status: {})", status);

        featureFlagService.requireEnabled(Feature.REPORTS);

        GoalReportDTO report = reportService.generateGoalReport(status);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Get summary report",
            description = "Generate a comprehensive summary report combining transactions and goals with optional date range"
    )
    public ResponseEntity<SummaryReportDTO> getSummaryReport(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        log.debug("GET request to retrieve summary report (dateFrom: {}, dateTo: {})", dateFrom, dateTo);

        featureFlagService.requireEnabled(Feature.REPORTS);

        SummaryReportDTO report = reportService.generateSummaryReport(dateFrom, dateTo);
        return ResponseEntity.ok(report);
    }
}
