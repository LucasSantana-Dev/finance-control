package com.finance_control.open_finance.dto;

import com.finance_control.shared.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for Open Finance institution information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OpenFinanceInstitutionDTO extends BaseDTO<Long> {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    @NotBlank
    private String apiBaseUrl;

    @NotBlank
    private String authorizationUrl;

    @NotBlank
    private String tokenUrl;

    @NotNull
    private Boolean certificateRequired;

    @NotNull
    private Boolean isActive;
}
