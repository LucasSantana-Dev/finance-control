package com.finance_control.open_finance.dto;

import com.finance_control.shared.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Open Finance consent operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConsentDTO extends BaseDTO<Long> {

    @NotNull
    private Long userId;

    @NotNull
    private Long institutionId;

    private String institutionName;
    private String institutionCode;

    @NotBlank
    private String status;

    @NotBlank
    private String scopes; // Comma-separated list

    private List<String> scopeList; // Parsed scopes

    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;

    private boolean active;
    private boolean expired;
}
