package com.finance_control.shared.controller;

import com.finance_control.shared.feature.Feature;
import com.finance_control.shared.feature.FeatureFlagService;
import com.finance_control.shared.service.DataExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST controller for data export operations.
 * Provides endpoints for exporting user data in various formats.
 */
@RestController
@RequestMapping("/export")
@Tag(name = "Data Export", description = "Endpoints for exporting user data in various formats")
@RequiredArgsConstructor
@Slf4j
public class DataExportController {

    private final DataExportService dataExportService;
    private final FeatureFlagService featureFlagService;

    @GetMapping("/all/csv")
    @Operation(
            summary = "Export all user data as CSV",
            description = "Exports all user data including profile, transactions, and financial goals in CSV format"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data exported successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<byte[]> exportAllDataAsCsv() {
        log.info("Exporting all user data as CSV");

        featureFlagService.requireEnabled(Feature.DATA_EXPORT);

        try {
            byte[] csvData = dataExportService.exportUserDataAsCsv();

            String filename = "finance-control-export-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting all user data as CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all/json")
    @Operation(
            summary = "Export all user data as JSON",
            description = "Exports all user data including profile, transactions, and financial goals in JSON format"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data exported successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> exportAllDataAsJson() {
        log.info("Exporting all user data as JSON");

        featureFlagService.requireEnabled(Feature.DATA_EXPORT);

        try {
            String jsonData = dataExportService.exportUserDataAsJson();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return new ResponseEntity<>(jsonData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting all user data as JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/transactions/csv")
    @Operation(summary = "Export transactions as CSV", description = "Exports user transactions in CSV format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions exported successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<byte[]> exportTransactionsAsCsv() {
        log.info("Exporting transactions as CSV");

        featureFlagService.requireEnabled(Feature.DATA_EXPORT);

        try {
            byte[] csvData = dataExportService.exportTransactionsAsCsv();

            String filename = "transactions-export-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting transactions as CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/goals/csv")
    @Operation(summary = "Export financial goals as CSV", description = "Exports user financial goals in CSV format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Financial goals exported successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<byte[]> exportFinancialGoalsAsCsv() {
        log.info("Exporting financial goals as CSV");

        featureFlagService.requireEnabled(Feature.DATA_EXPORT);

        try {
            byte[] csvData = dataExportService.exportFinancialGoalsAsCsv();

            String filename = "financial-goals-export-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting financial goals as CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/data-export/transactions/csv")
    @Operation(
            summary = "Export filtered transactions as CSV",
            description = "Exports user transactions in CSV format with optional filters for date range, type, and category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions exported successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<byte[]> exportFilteredTransactionsAsCsv(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "Transaction type (income or expense)")
            @RequestParam(required = false) String type,
            @Parameter(description = "Category name")
            @RequestParam(required = false) String category) {
        log.info("Exporting filtered transactions as CSV (dateFrom: {}, dateTo: {}, type: {}, category: {})",
                dateFrom, dateTo, type, category);

        featureFlagService.requireEnabled(Feature.DATA_EXPORT);

        try {
            byte[] csvData = dataExportService.exportTransactionsAsCsv(dateFrom, dateTo, type, category);

            String filename = "transactions-export-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting filtered transactions as CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/data-export/goals/csv")
    @Operation(
            summary = "Export filtered financial goals as CSV",
            description = "Exports user financial goals in CSV format with optional status filter"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Financial goals exported successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<byte[]> exportFilteredFinancialGoalsAsCsv(
            @Parameter(description = "Goal status (active, completed, paused, cancelled)")
            @RequestParam(required = false) String status) {
        log.info("Exporting filtered financial goals as CSV (status: {})", status);

        featureFlagService.requireEnabled(Feature.DATA_EXPORT);

        try {
            byte[] csvData = dataExportService.exportFinancialGoalsAsCsv(status);

            String filename = "financial-goals-export-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting filtered financial goals as CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
