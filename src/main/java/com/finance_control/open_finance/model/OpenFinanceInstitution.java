package com.finance_control.open_finance.model;

import com.finance_control.shared.model.BaseModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing an Open Finance Brasil participating institution.
 * Stores metadata about banks and financial institutions that participate in Open Finance.
 */
@Entity
@Table(name = "open_finance_institutions",
       uniqueConstraints = @UniqueConstraint(columnNames = "code"),
       indexes = {
           @Index(name = "idx_open_finance_institutions_code", columnList = "code"),
           @Index(name = "idx_open_finance_institutions_is_active", columnList = "is_active")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OpenFinanceInstitution extends BaseModel<Long> {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank
    @Column(name = "api_base_url", nullable = false, length = 512)
    private String apiBaseUrl;

    @NotBlank
    @Column(name = "authorization_url", nullable = false, length = 512)
    private String authorizationUrl;

    @NotBlank
    @Column(name = "token_url", nullable = false, length = 512)
    private String tokenUrl;

    @NotNull
    @Column(name = "certificate_required", nullable = false)
    private Boolean certificateRequired = true;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
