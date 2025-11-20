package com.finance_control.shared.config.properties;

import java.util.List;

/**
 * Open Finance configuration properties.
 */
public record OpenFinanceProperties(
    boolean enabled,
    String sandboxBaseUrl,
    String productionBaseUrl,
    boolean useProduction,
    OAuthProperties oauth,
    CertificatesProperties certificates,
    SyncProperties sync,
    InstitutionRegistryProperties institutionRegistry
) {
    public OpenFinanceProperties() {
        this(true,
             "https://api.sandbox.openfinancebrasil.org.br",
             "https://api.openfinancebrasil.org.br",
             false,
             new OAuthProperties(),
             new CertificatesProperties(),
             new SyncProperties(),
             new InstitutionRegistryProperties());
    }

    public record OAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        List<String> defaultScopes
    ) {
        public OAuthProperties() {
            this("", "", "http://localhost:8080/api/open-finance/consents/callback",
                 List.of("accounts", "transactions", "payments"));
        }
    }

    public record CertificatesProperties(
        String clientCertificatePath,
        String privateKeyPath,
        String caCertificatePath,
        String keystorePath,
        String keystorePassword,
        boolean useSupabaseStorage,
        String supabaseStorageBucket
    ) {
        public CertificatesProperties() {
            this("", "", "", "", "", false, "certificates");
        }
    }

    public record SyncProperties(
        boolean enabled,
        int balanceSyncIntervalMinutes,
        int transactionSyncIntervalHours,
        int tokenRefreshBeforeExpirationMinutes,
        int maxRetryAttempts,
        long retryDelayMs
    ) {
        public SyncProperties() {
            this(true, 15, 24, 5, 3, 5000);
        }
    }

    public record InstitutionRegistryProperties(
        String endpoint,
        int refreshIntervalHours,
        boolean autoRefresh
    ) {
        public InstitutionRegistryProperties() {
            this("https://api.openfinancebrasil.org.br/institutions", 24, true);
        }
    }
}

