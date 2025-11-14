package com.finance_control.open_finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for account balance information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceDTO {

    @NotNull
    private Long accountId;

    @NotNull
    private String externalAccountId;

    private BigDecimal balance;
    private String currency = "BRL";

    private LocalDateTime lastUpdated;
}
