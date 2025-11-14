package com.finance_control.open_finance.dto;

import com.finance_control.shared.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for connected account operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConnectedAccountDTO extends BaseDTO<Long> {

    @NotNull
    private Long userId;

    @NotNull
    private Long consentId;

    @NotNull
    private Long institutionId;

    private String institutionName;
    private String institutionCode;

    @NotBlank
    private String externalAccountId;

    @NotBlank
    private String accountType;

    private String accountNumber;
    private String branch;
    private String accountHolderName;

    private BigDecimal balance;
    private String currency = "BRL";

    private LocalDateTime lastSyncedAt;

    @NotBlank
    private String syncStatus;

    private boolean syncable;
}
