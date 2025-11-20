package com.finance_control.transactions.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionInstallmentRequest {
    @NotBlank
    private String description;

    @NotNull
    private BigDecimal totalAmount;

    @Min(2)
    private int installmentCount;

    @NotNull
    private LocalDate firstInstallmentDate;

    @NotNull
    private Long categoryId;

    private Long subcategoryId;

    @NotNull
    private String source;

    @NotNull
    private String type;

    @NotNull
    private String subtype;

    private Long userId;
}
