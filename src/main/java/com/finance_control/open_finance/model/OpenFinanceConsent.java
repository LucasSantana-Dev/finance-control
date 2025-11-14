package com.finance_control.open_finance.model;

import com.finance_control.shared.model.BaseModel;
import com.finance_control.users.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an OAuth 2.0 consent for Open Finance API access.
 * Stores consent status, tokens, and expiration information.
 */
@Entity
@Table(name = "open_finance_consents",
       indexes = {
           @Index(name = "idx_open_finance_consents_user_id", columnList = "user_id"),
           @Index(name = "idx_open_finance_consents_institution_id", columnList = "institution_id"),
           @Index(name = "idx_open_finance_consents_status", columnList = "status"),
           @Index(name = "idx_open_finance_consents_expires_at", columnList = "expires_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OpenFinanceConsent extends BaseModel<Long> {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private OpenFinanceInstitution institution;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String status; // PENDING, AUTHORIZED, REVOKED, EXPIRED, FAILED

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String scopes; // Comma-separated list of OAuth scopes

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken; // Encrypted

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken; // Encrypted

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public boolean isActive() {
        return "AUTHORIZED".equals(status) &&
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now())) &&
               revokedAt == null;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
