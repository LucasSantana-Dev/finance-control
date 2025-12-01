package com.finance_control.open_finance.controller;

import com.finance_control.open_finance.client.PaymentInitiationClient;
import com.finance_control.open_finance.dto.PaymentRequestDTO;
import com.finance_control.open_finance.service.OpenFinanceConsentService;
import com.finance_control.shared.feature.Feature;
import com.finance_control.shared.feature.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Open Finance payment initiation.
 */
@Slf4j
@RestController
@RequestMapping("/api/open-finance/payments")
@RequiredArgsConstructor
@Tag(name = "Open Finance Payments", description = "Endpoints for initiating and managing payments via Open Finance")
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinancePaymentController {

    private final PaymentInitiationClient paymentClient;
    private final OpenFinanceConsentService consentService;
    private final FeatureFlagService featureFlagService;

    @PostMapping("/initiate/{consentId}")
    @Operation(summary = "Initiate payment",
               description = "Initiates a payment through Open Finance using the specified consent.")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @PathVariable @NotNull Long consentId,
            @Valid @RequestBody PaymentRequestDTO request) {
        log.debug("Initiating payment for consent: {}", consentId);
        featureFlagService.requireEnabled(Feature.OPEN_FINANCE);

        String accessToken = consentService.getAccessToken(consentId);

        PaymentInitiationClient.PaymentRequest paymentRequest = PaymentInitiationClient.PaymentRequest.builder()
                .endToEndId(request.getEndToEndId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .debtorAccount(request.getDebtorAccount())
                .creditorAccount(request.getCreditorAccount())
                .paymentType(request.getPaymentType())
                .build();

        PaymentInitiationClient.PaymentResponse response = paymentClient.initiatePayment(accessToken, paymentRequest);

        return ResponseEntity.ok(PaymentResponse.builder()
                .paymentId(response.getPaymentId())
                .endToEndId(response.getEndToEndId())
                .status(response.getStatus())
                .build());
    }

    @GetMapping("/{consentId}/status/{paymentId}")
    @Operation(summary = "Get payment status",
               description = "Retrieves the status of a payment.")
    public ResponseEntity<PaymentStatus> getPaymentStatus(
            @PathVariable @NotNull Long consentId,
            @PathVariable @NotNull String paymentId) {
        log.debug("Checking payment status: {}", paymentId);
        featureFlagService.requireEnabled(Feature.OPEN_FINANCE);

        String accessToken = consentService.getAccessToken(consentId);
        PaymentInitiationClient.PaymentStatus status = paymentClient.getPaymentStatus(accessToken, paymentId);

        return ResponseEntity.ok(PaymentStatus.builder()
                .paymentId(status.getPaymentId())
                .endToEndId(status.getEndToEndId())
                .status(status.getStatus())
                .build());
    }

    @DeleteMapping("/{consentId}/{paymentId}")
    @Operation(summary = "Cancel payment",
               description = "Cancels a pending payment.")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable @NotNull Long consentId,
            @PathVariable @NotNull String paymentId) {
        log.debug("Cancelling payment: {}", paymentId);
        featureFlagService.requireEnabled(Feature.OPEN_FINANCE);

        String accessToken = consentService.getAccessToken(consentId);
        paymentClient.cancelPayment(accessToken, paymentId);

        return ResponseEntity.noContent().build();
    }

    @lombok.Data
    @lombok.Builder
    public static class PaymentResponse {
        private String paymentId;
        private String endToEndId;
        private String status;
    }

    @lombok.Data
    @lombok.Builder
    public static class PaymentStatus {
        private String paymentId;
        private String endToEndId;
        private String status;
    }
}
