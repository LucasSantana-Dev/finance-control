package com.finance_control.open_finance.controller;

import com.finance_control.open_finance.dto.ConsentDTO;
import com.finance_control.open_finance.service.OpenFinanceConsentService;
import com.finance_control.shared.feature.Feature;
import com.finance_control.shared.feature.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Open Finance consent management.
 */
@Slf4j
@RestController
@RequestMapping("/api/open-finance/consents")
@RequiredArgsConstructor
@Tag(name = "Open Finance Consents", description = "Endpoints for managing Open Finance consents and OAuth flow")
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinanceConsentController {

    private final OpenFinanceConsentService consentService;
    private final FeatureFlagService featureFlagService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate consent flow",
               description = "Initiates the OAuth consent flow for connecting a bank account. Returns an authorization URL.")
    public ResponseEntity<ConsentInitiationResponse> initiateConsent(
            @Valid @RequestBody ConsentInitiationRequest request) {
        log.debug("Initiating consent flow for institution: {}", request.getInstitutionId());
        featureFlagService.requireEnabled(Feature.OPEN_FINANCE);
        var serviceResponse = consentService.initiateConsent(
                request.getInstitutionId(),
                request.getScopes());
        ConsentInitiationResponse response = ConsentInitiationResponse.builder()
                .consentId(serviceResponse.getConsentId())
                .authorizationUrl(serviceResponse.getAuthorizationUrl())
                .state(serviceResponse.getState())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    @Operation(summary = "OAuth callback handler",
               description = "Handles the OAuth callback after user authorization. Exchanges authorization code for tokens.")
    public ResponseEntity<ConsentDTO> handleCallback(
            @RequestParam @NotNull Long consentId,
            @RequestParam @NotNull String code,
            @RequestParam(required = false) String state) {
        log.debug("Handling OAuth callback for consent: {}", consentId);
        featureFlagService.requireEnabled(Feature.OPEN_FINANCE);
        ConsentDTO consent = consentService.handleCallback(consentId, code, state);
        return ResponseEntity.ok(consent);
    }

    @GetMapping
    @Operation(summary = "List user consents",
               description = "Retrieves all consents for the current user.")
    public ResponseEntity<List<ConsentDTO>> getUserConsents() {
        log.debug("Retrieving consents for current user");
        featureFlagService.requireEnabled(Feature.OPEN_FINANCE);
        List<ConsentDTO> consents = consentService.getUserConsents();
        return ResponseEntity.ok(consents);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get consent by ID",
               description = "Retrieves a specific consent by its ID.")
    public ResponseEntity<ConsentDTO> getConsent(@PathVariable Long id) {
        log.debug("Retrieving consent: {}", id);
        featureFlagService.requireEnabled(Feature.OPEN_FINANCE);
        ConsentDTO consent = consentService.getConsent(id);
        return ResponseEntity.ok(consent);
    }

    @PostMapping("/{id}/refresh")
    @Operation(summary = "Refresh consent token",
               description = "Refreshes the access token for a consent before it expires.")
    public ResponseEntity<ConsentDTO> refreshToken(@PathVariable Long id) {
        log.debug("Refreshing token for consent: {}", id);
        featureFlagService.requireEnabled(Feature.OPEN_FINANCE);
        ConsentDTO consent = consentService.refreshToken(id);
        return ResponseEntity.ok(consent);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revoke consent",
               description = "Revokes a consent and disconnects the associated bank account.")
    public ResponseEntity<Void> revokeConsent(@PathVariable Long id) {
        log.debug("Revoking consent: {}", id);
        featureFlagService.requireEnabled(Feature.OPEN_FINANCE);
        consentService.revokeConsent(id);
        return ResponseEntity.noContent().build();
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConsentInitiationRequest {
        @NotNull
        private Long institutionId;
        private List<String> scopes;
    }

    @lombok.Data
    @lombok.Builder
    public static class ConsentInitiationResponse {
        private Long consentId;
        private String authorizationUrl;
        private String state;
    }
}
