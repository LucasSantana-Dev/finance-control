package com.finance_control.open_finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for payment initiation requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotBlank
    private String endToEndId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency = "BRL";

    @NotNull
    private Map<String, String> debtorAccount;

    @NotNull
    private Map<String, String> creditorAccount;

    private String paymentType; // PIX, TED, DOC, etc.
}
