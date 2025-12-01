package com.finance_control.open_finance.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for Open Finance Payment Initiation API.
 * Handles payment initiation, status checking, and cancellation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class PaymentInitiationClient {

    @Qualifier("openFinanceRestClient")
    private final RestClient restClient;

    private static final String PAYMENTS_ENDPOINT = "/open-banking/payments/v1/pix/payments";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    /**
     * Initiates a payment.
     *
     * @param accessToken the OAuth access token
     * @param paymentRequest payment request details
     * @return payment response with payment ID and status
     */
    public PaymentResponse initiatePayment(String accessToken, PaymentRequest paymentRequest) {
        log.debug("Initiating payment: {}", paymentRequest.getEndToEndId());

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> payment = new HashMap<>();

        payment.put("endToEndId", paymentRequest.getEndToEndId());
        payment.put("amount", paymentRequest.getAmount().toString());
        payment.put("currency", paymentRequest.getCurrency());

        Map<String, Object> debtor = new HashMap<>();
        debtor.put("account", paymentRequest.getDebtorAccount());
        payment.put("debtor", debtor);

        Map<String, Object> creditor = new HashMap<>();
        creditor.put("account", paymentRequest.getCreditorAccount());
        payment.put("creditor", creditor);

        if (paymentRequest.getPaymentType() != null) {
            payment.put("paymentType", paymentRequest.getPaymentType());
        }

        data.put("payment", payment);
        requestBody.put("data", data);

        return executeWithRetry(() -> {
            JsonNode response = restClient.post()
                    .uri(PAYMENTS_ENDPOINT)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            PaymentResponse result = parsePaymentResponse(response);
            log.info("Successfully initiated payment: {}", result.getPaymentId());
            return result;
        }, "Failed to initiate payment");
    }

    /**
     * Gets the status of a payment.
     *
     * @param accessToken the OAuth access token
     * @param paymentId the payment ID
     * @return payment status
     */
    public PaymentStatus getPaymentStatus(String accessToken, String paymentId) {
        log.debug("Checking payment status: {}", paymentId);

        return executeWithRetry(() -> {
            JsonNode response = restClient.get()
                    .uri(PAYMENTS_ENDPOINT + "/{paymentId}", paymentId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);

            PaymentStatus status = parsePaymentStatusResponse(response);
            log.info("Payment status for {}: {}", paymentId, status.getStatus());
            return status;
        }, "Failed to get payment status for " + paymentId);
    }

    /**
     * Cancels a payment.
     *
     * @param accessToken the OAuth access token
     * @param paymentId the payment ID
     */
    public void cancelPayment(String accessToken, String paymentId) {
        log.debug("Cancelling payment: {}", paymentId);

        executeWithRetry(() -> {
            restClient.delete()
                    .uri(PAYMENTS_ENDPOINT + "/{paymentId}", paymentId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully cancelled payment: {}", paymentId);
            return null;
        }, "Failed to cancel payment " + paymentId);
    }

    /**
     * Executes a request with retry logic for 5xx server errors.
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> operation, String errorMessage) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (RestClientException e) {
                lastException = e;
                HttpStatusCode statusCode = null;

                if (e instanceof HttpServerErrorException serverEx) {
                    statusCode = serverEx.getStatusCode();
                } else if (e instanceof HttpClientErrorException clientEx) {
                    statusCode = clientEx.getStatusCode();
                }

                if (statusCode != null && statusCode.is5xxServerError() && attempts < MAX_RETRIES - 1) {
                    attempts++;
                    long delay = RETRY_DELAY_MS * attempts;
                    log.warn("Server error ({}), retrying in {}ms (attempt {}/{})", statusCode, delay, attempts, MAX_RETRIES);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    log.error("{}: {}", errorMessage, e.getMessage());
                    throw new RuntimeException(errorMessage, e);
                }
            }
        }

        log.error("{} after {} attempts", errorMessage, MAX_RETRIES);
        throw new RuntimeException(errorMessage, lastException);
    }

    private PaymentResponse parsePaymentResponse(JsonNode jsonNode) {
        JsonNode data = jsonNode.path("data");
        JsonNode payment = data.path("payment");

        String paymentId = payment.has("paymentId") ? payment.path("paymentId").asText() : null;
        String status = payment.has("status") ? payment.path("status").asText() : "PENDING";
        String endToEndId = payment.has("endToEndId") ? payment.path("endToEndId").asText() : null;

        return PaymentResponse.builder()
                .paymentId(paymentId)
                .endToEndId(endToEndId)
                .status(status)
                .build();
    }

    private PaymentStatus parsePaymentStatusResponse(JsonNode jsonNode) {
        JsonNode data = jsonNode.path("data");
        JsonNode payment = data.path("payment");

        String paymentId = payment.has("paymentId") ? payment.path("paymentId").asText() : null;
        String status = payment.has("status") ? payment.path("status").asText() : "UNKNOWN";
        String endToEndId = payment.has("endToEndId") ? payment.path("endToEndId").asText() : null;

        return PaymentStatus.builder()
                .paymentId(paymentId)
                .endToEndId(endToEndId)
                .status(status)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class PaymentRequest {
        private String endToEndId;
        private BigDecimal amount;
        private String currency;
        private Map<String, String> debtorAccount;
        private Map<String, String> creditorAccount;
        private String paymentType; // PIX, TED, DOC, etc.
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
