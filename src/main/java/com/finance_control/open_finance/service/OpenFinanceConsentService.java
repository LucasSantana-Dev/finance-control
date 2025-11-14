package com.finance_control.open_finance.service;

import com.finance_control.open_finance.client.OpenFinanceOAuthClient;
import com.finance_control.open_finance.dto.ConsentDTO;
import com.finance_control.open_finance.mapper.OpenFinanceMapper;
import com.finance_control.open_finance.model.OpenFinanceConsent;
import com.finance_control.open_finance.model.OpenFinanceInstitution;
import com.finance_control.open_finance.repository.OpenFinanceConsentRepository;
import com.finance_control.open_finance.repository.OpenFinanceInstitutionRepository;
import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.service.SupabaseRealtimeService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing Open Finance consents.
 * Handles consent creation, token management, refresh, and revocation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinanceConsentService {

    private final OpenFinanceConsentRepository consentRepository;
    private final OpenFinanceInstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final OpenFinanceOAuthClient oauthClient;
    private final OpenFinanceMapper mapper;
    private final AppProperties appProperties;
    private final MetricsService metricsService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SupabaseRealtimeService realtimeService;

    /**
     * Initiates a consent flow by generating an authorization URL.
     *
     * @param institutionId the institution ID
     * @param scopes optional list of scopes (uses defaults if not provided)
     * @return authorization URL and consent ID
     */
    @Transactional
    public ConsentInitiationResponse initiateConsent(Long institutionId, List<String> scopes) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }

        OpenFinanceInstitution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution not found: " + institutionId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        // Check for existing consent
        var existingConsent = consentRepository.findByUserIdAndInstitutionId(userId, institutionId);
        if (existingConsent.isPresent() && existingConsent.get().isActive()) {
            throw new IllegalStateException("Active consent already exists for this institution");
        }

        // Create pending consent
        OpenFinanceConsent consent = new OpenFinanceConsent();
        consent.setUser(user);
        consent.setInstitution(institution);
        consent.setStatus("PENDING");
        consent.setScopes(scopes != null && !scopes.isEmpty() ?
                String.join(",", scopes) :
                String.join(",", appProperties.openFinance().oauth().defaultScopes()));

        consent = consentRepository.save(consent);

        // Generate authorization URL
        String state = UUID.randomUUID().toString();
        String authUrl = oauthClient.generateAuthorizationUrl(institution, userId, state, scopes);

        log.info("Initiated consent flow for user {} and institution {}", userId, institutionId);
        metricsService.incrementOpenFinanceConsentCreated();

        return ConsentInitiationResponse.builder()
                .consentId(consent.getId())
                .authorizationUrl(authUrl)
                .state(state)
                .build();
    }

    /**
     * Handles OAuth callback and exchanges authorization code for tokens.
     *
     * @param consentId the consent ID
     * @param authorizationCode the authorization code
     * @param state the state parameter (for CSRF protection)
     * @return updated consent DTO
     */
    @Transactional
    public ConsentDTO handleCallback(Long consentId, String authorizationCode, String state) {
        OpenFinanceConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new EntityNotFoundException("Consent not found: " + consentId));

        if (!"PENDING".equals(consent.getStatus())) {
            throw new IllegalStateException("Consent is not in PENDING status");
        }

        String redirectUri = appProperties.openFinance().oauth().redirectUri();
        final OpenFinanceConsent finalConsent = consent;

        return oauthClient.exchangeAuthorizationCode(finalConsent.getInstitution(), authorizationCode, redirectUri)
                .map(tokenResponse -> {
                    // Encrypt and store tokens
                    finalConsent.setAccessToken(encryptToken(tokenResponse.getAccessToken()));
                    finalConsent.setRefreshToken(encryptToken(tokenResponse.getRefreshToken()));
                    finalConsent.setExpiresAt(tokenResponse.getExpiresAt());
                    finalConsent.setStatus("AUTHORIZED");

                    OpenFinanceConsent savedConsent = consentRepository.save(finalConsent);

                    // Broadcast realtime update
                    broadcastConsentUpdate(savedConsent);

                    log.info("Successfully authorized consent {} for user {}", consentId, savedConsent.getUser().getId());
                    return mapper.toDTO(savedConsent);
                })
                .block();
    }

    /**
     * Refreshes an access token before expiration.
     *
     * @param consentId the consent ID
     * @return updated consent DTO
     */
    @Transactional
    public ConsentDTO refreshToken(Long consentId) {
        OpenFinanceConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new EntityNotFoundException("Consent not found: " + consentId));

        if (!consent.isActive()) {
            throw new IllegalStateException("Consent is not active");
        }

        String refreshToken = decryptToken(consent.getRefreshToken());
        if (!StringUtils.hasText(refreshToken)) {
            throw new IllegalStateException("Refresh token not available");
        }

        final OpenFinanceConsent finalConsent = consent;

        return oauthClient.refreshToken(finalConsent.getInstitution(), refreshToken)
                .map(tokenResponse -> {
                    finalConsent.setAccessToken(encryptToken(tokenResponse.getAccessToken()));
                    if (StringUtils.hasText(tokenResponse.getRefreshToken())) {
                        finalConsent.setRefreshToken(encryptToken(tokenResponse.getRefreshToken()));
                    }
                    finalConsent.setExpiresAt(tokenResponse.getExpiresAt());
                    finalConsent.setStatus("AUTHORIZED");

                    OpenFinanceConsent savedConsent = consentRepository.save(finalConsent);

                    log.info("Successfully refreshed token for consent {}", consentId);
                    return mapper.toDTO(savedConsent);
                })
                .block();
    }

    /**
     * Revokes a consent.
     *
     * @param consentId the consent ID
     */
    @Transactional
    public void revokeConsent(Long consentId) {
        OpenFinanceConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new EntityNotFoundException("Consent not found: " + consentId));

        Long userId = UserContext.getCurrentUserId();
        if (!consent.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: consent does not belong to current user");
        }

        // Revoke token with institution
        String accessToken = decryptToken(consent.getAccessToken());
        if (StringUtils.hasText(accessToken)) {
            oauthClient.revokeToken(consent.getInstitution(), accessToken, "access_token")
                    .doOnError(error -> log.warn("Failed to revoke token with institution: {}", error.getMessage()))
                    .block();
        }

        consent.setStatus("REVOKED");
        consent.setRevokedAt(LocalDateTime.now());
        consent = consentRepository.save(consent);

        // Broadcast realtime update
        broadcastConsentUpdate(consent);

        metricsService.incrementOpenFinanceConsentRevoked();
        log.info("Successfully revoked consent {}", consentId);
    }

    /**
     * Gets all consents for the current user.
     *
     * @return list of consent DTOs
     */
    public List<ConsentDTO> getUserConsents() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }

        return consentRepository.findByUserId(userId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets a consent by ID.
     *
     * @param consentId the consent ID
     * @return consent DTO
     */
    public ConsentDTO getConsent(Long consentId) {
        OpenFinanceConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new EntityNotFoundException("Consent not found: " + consentId));

        Long userId = UserContext.getCurrentUserId();
        if (!consent.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: consent does not belong to current user");
        }

        return mapper.toDTO(consent);
    }

    /**
     * Gets the decrypted access token for a consent.
     * Used internally by other services.
     *
     * @param consentId the consent ID
     * @return decrypted access token
     */
    public String getAccessToken(Long consentId) {
        OpenFinanceConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new EntityNotFoundException("Consent not found: " + consentId));

        if (!consent.isActive()) {
            throw new IllegalStateException("Consent is not active");
        }

        return decryptToken(consent.getAccessToken());
    }

    /**
     * Refreshes tokens for consents expiring soon.
     * Called by scheduler.
     */
    @Transactional
    public void refreshExpiringTokens() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime refreshThreshold = now.plusMinutes(
                appProperties.openFinance().sync().tokenRefreshBeforeExpirationMinutes());

        List<OpenFinanceConsent> expiringConsents = consentRepository.findConsentsExpiringBetween(now, refreshThreshold);

        for (OpenFinanceConsent consent : expiringConsents) {
            try {
                refreshToken(consent.getId());
            } catch (Exception e) {
                log.error("Failed to refresh token for consent {}: {}", consent.getId(), e.getMessage());
            }
        }
    }

    private void broadcastConsentUpdate(OpenFinanceConsent consent) {
        if (realtimeService != null) {
            try {
                ConsentDTO dto = mapper.toDTO(consent);
                realtimeService.broadcastToUser("open_finance_consents", consent.getUser().getId(), dto);
                log.debug("Broadcasted consent update for user {}", consent.getUser().getId());
            } catch (Exception e) {
                log.warn("Failed to broadcast consent update: {}", e.getMessage());
            }
        }
    }

    private String encryptToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        // Simple Base64 encoding for now - in production, use proper encryption
        // TODO: Implement proper encryption using AES or similar
        return Base64.getEncoder().encodeToString(token.getBytes());
    }

    private String decryptToken(String encryptedToken) {
        if (!StringUtils.hasText(encryptedToken)) {
            return null;
        }
        try {
            return new String(Base64.getDecoder().decode(encryptedToken));
        } catch (Exception e) {
            log.error("Failed to decrypt token", e);
            return null;
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class ConsentInitiationResponse {
        private Long consentId;
        private String authorizationUrl;
        private String state;
    }
}
