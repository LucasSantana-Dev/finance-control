package com.finance_control.shared.exception;

import com.finance_control.open_finance.exception.AccountSyncException;
import com.finance_control.open_finance.exception.ConsentExpiredException;
import com.finance_control.open_finance.exception.OpenFinanceApiException;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.monitoring.SentryService;
import io.sentry.SentryLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SentryService sentryService;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        log.warn("Entity not found: {}", ex.getMessage());

        // Don't send 404s to Sentry as they're expected errors
        // But add breadcrumb for tracking
        sentryService.addBreadcrumb("Entity not found: " + ex.getMessage(), "error", SentryLevel.INFO);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request != null ? request.getDescription(false) : "unknown")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Invalid argument: {}", ex.getMessage());

        // Capture to Sentry with context
        Map<String, Object> context = buildRequestContext(request);
        sentryService.captureException(ex, context);
        sentryService.setTags(Map.of(
            "exception_type", "IllegalArgumentException",
            "http_status", "400"
        ));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request != null ? request.getDescription(false) : "unknown")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName;
            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            } else {
                fieldName = error.getObjectName();
            }
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName != null ? fieldName : "unknown", errorMessage != null ? errorMessage : "");
        });

        // Add breadcrumb for validation errors (don't send full exception as they're expected)
        sentryService.addBreadcrumb(
            "Validation failed: " + errors.size() + " field(s) with errors",
            "validation",
            SentryLevel.WARNING
        );
        sentryService.setContext("validation_errors", errors);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Validation failed for the provided data")
                .path(request != null ? request.getDescription(false) : "unknown")
                .details(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        log.warn("Response status exception: {} - {}", status, ex.getReason());

        // Only capture 5xx errors to Sentry
        if (status.is5xxServerError()) {
            Map<String, Object> context = buildRequestContext(request);
            sentryService.captureException(ex, context);
            sentryService.setTags(Map.of(
                "exception_type", "ResponseStatusException",
                "http_status", String.valueOf(status.value())
            ));
        } else {
            sentryService.addBreadcrumb(
                "Response status exception: " + status + " - " + ex.getReason(),
                "http",
                SentryLevel.INFO
            );
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getReason() != null ? ex.getReason() : status.getReasonPhrase())
                .path(request != null ? request.getDescription(false) : "unknown")
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(ConsentExpiredException.class)
    public ResponseEntity<ErrorResponse> handleConsentExpired(ConsentExpiredException ex, WebRequest request) {
        log.warn("Consent expired: {}", ex.getMessage());

        Map<String, Object> context = buildRequestContext(request);
        sentryService.captureException(ex, context);
        sentryService.setTags(Map.of(
            "exception_type", "ConsentExpiredException",
            "http_status", "410"
        ));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.GONE.value())
                .error("Gone")
                .message(ex.getMessage())
                .path(request != null ? request.getDescription(false) : "unknown")
                .build();

        return ResponseEntity.status(HttpStatus.GONE).body(errorResponse);
    }

    @ExceptionHandler(AccountSyncException.class)
    public ResponseEntity<ErrorResponse> handleAccountSync(AccountSyncException ex, WebRequest request) {
        log.error("Account sync failed: {}", ex.getMessage(), ex);

        Map<String, Object> context = buildRequestContext(request);
        sentryService.captureException(ex, context);
        sentryService.setTags(Map.of(
            "exception_type", "AccountSyncException",
            "http_status", "500"
        ));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(ex.getMessage())
                .path(request != null ? request.getDescription(false) : "unknown")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(OpenFinanceApiException.class)
    public ResponseEntity<ErrorResponse> handleOpenFinanceApi(OpenFinanceApiException ex, WebRequest request) {
        log.error("Open Finance API error: {}", ex.getMessage(), ex);

        Map<String, Object> context = buildRequestContext(request);
        context.put("status_code", ex.getStatusCode());
        sentryService.captureException(ex, context);
        sentryService.setTags(Map.of(
            "exception_type", "OpenFinanceApiException",
            "http_status", String.valueOf(ex.getStatusCode() > 0 ? ex.getStatusCode() : 502)
        ));

        HttpStatus status = ex.getStatusCode() > 0 && ex.getStatusCode() < 600 ?
                HttpStatus.valueOf(ex.getStatusCode()) : HttpStatus.BAD_GATEWAY;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request != null ? request.getDescription(false) : "unknown")
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        // Capture all unexpected exceptions to Sentry with full context
        Map<String, Object> context = buildRequestContext(request);
        setUserContext();
        sentryService.captureException(ex, context);
        sentryService.setTags(Map.of(
            "exception_type", ex.getClass().getSimpleName(),
            "http_status", "500",
            "unhandled", "true"
        ));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request != null ? request.getDescription(false) : "unknown")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Builds request context for Sentry from WebRequest.
     */
    private Map<String, Object> buildRequestContext(WebRequest request) {
        Map<String, Object> context = new HashMap<>();
        if (request != null) {
            context.put("path", request.getDescription(false));
            context.put("request_uri", request.getDescription(false));
        }
        return context;
    }

    /**
     * Sets user context in Sentry from SecurityContext or UserContext.
     */
    private void setUserContext() {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId != null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String email = null;
                String username = null;

                if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                    org.springframework.security.core.userdetails.UserDetails userDetails =
                        (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                    username = userDetails.getUsername();
                }

                sentryService.setUserContext(userId, email, username);
            }
        } catch (Exception e) {
            log.debug("Failed to set user context in Sentry: {}", e.getMessage());
        }
    }
}
