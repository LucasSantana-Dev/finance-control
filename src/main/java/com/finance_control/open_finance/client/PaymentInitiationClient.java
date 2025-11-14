package com.finance_control.open_finance.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
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

    @Qualifier("openFinanceWebClient")
    private final WebClient webClient;

    private static final String PAYMENTS_ENDPOINT = "/open-banking/payments/v1/pix/payments";

    /**
     * Initiates a payment.
     *
     * @param accessToken the OAuth access token
     * @param paymentRequest payment request details
     * @return payment response with payment ID and status
     */
    public Mono<PaymentResponse> initiatePayment(String accessToken, PaymentRequest paymentRequest) {
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

        return webClient.post()
                .uri(PAYMENTS_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parsePaymentResponse)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnSuccess(response -> log.info("Successfully initiated payment: {}", response.getPaymentId()))
                .doOnError(error -> log.error("Failed to initiate payment: {}", error.getMessage()));
    }

    /**
     * Gets the status of a payment.
     *
     * @param accessToken the OAuth access token
     * @param paymentId the payment ID
     * @return payment status
     */
    public Mono<PaymentStatus> getPaymentStatus(String accessToken, String paymentId) {
        log.debug("Checking payment status: {}", paymentId);

        return webClient.get()
                .uri(PAYMENTS_ENDPOINT + "/{paymentId}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parsePaymentStatusResponse)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnSuccess(status -> log.info("Payment status for {}: {}", paymentId, status.getStatus()))
                .doOnError(error -> log.error("Failed to get payment status for {}: {}", paymentId, error.getMessage()));
    }

    /**
     * Cancels a payment.
     *
     * @param accessToken the OAuth access token
     * @param paymentId the payment ID
     * @return Mono completing when cancellation is successful
     */
    public Mono<Void> cancelPayment(String accessToken, String paymentId) {
        log.debug("Cancelling payment: {}", paymentId);

        return webClient.delete()
                .uri(PAYMENTS_ENDPOINT + "/{paymentId}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnSuccess(response -> log.info("Successfully cancelled payment: {}", paymentId))
                .doOnError(error -> log.error("Failed to cancel payment {}: {}", paymentId, error.getMessage()));
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
