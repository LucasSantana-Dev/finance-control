package com.finance_control.shared.controller;

import com.finance_control.shared.dto.FrontendErrorReportRequest;
import com.finance_control.shared.dto.FrontendErrorReportResponse;
import com.finance_control.shared.error.FrontendErrorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitoring/frontend-errors")
@Tag(name = "Frontend Error Logging", description = "Ingests client-side errors for monitoring and alerting")
@RequiredArgsConstructor
@Validated
@Slf4j
@ConditionalOnProperty(value = "app.monitoring.frontendErrors.enabled", havingValue = "true", matchIfMissing = true)
public class FrontendErrorController {

    private final FrontendErrorService frontendErrorService;

    @PostMapping
    @Operation(
        summary = "Submit a frontend error report",
        description = "Stores a frontend error, forwards it to Sentry, and triggers alerts when thresholds are reached."
    )
    public ResponseEntity<FrontendErrorReportResponse> submitError(
        @Valid @RequestBody FrontendErrorReportRequest request
    ) {
        log.debug("Received frontend error report: {}", request.getMessage());
        FrontendErrorReportResponse response = frontendErrorService.logFrontendError(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
