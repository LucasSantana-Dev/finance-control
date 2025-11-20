package com.finance_control.shared.config.properties;

import java.util.List;

/**
 * Supabase configuration properties.
 */
public record SupabaseProperties(
    boolean enabled,
    String url,
    String anonKey,
    String jwtSigner,
    String serviceRoleKey,
    SupabaseDatabaseProperties database,
    StorageProperties storage,
    RealtimeProperties realtime
) {
    public SupabaseProperties() {
        this(true, "", "", "", "", new SupabaseDatabaseProperties(), new StorageProperties(), new RealtimeProperties());
    }

    public record SupabaseDatabaseProperties(
        boolean enabled,
        String host,
        int port,
        String database,
        String username,
        String password,
        boolean sslEnabled,
        String sslMode
    ) {
        public SupabaseDatabaseProperties() {
            this(true, "", 5432, "", "", "", true, "require");
        }

        public String getJdbcUrl() {
            StringBuilder url = new StringBuilder("jdbc:postgresql://");
            url.append(host).append(":").append(port).append("/").append(database);

            if (sslEnabled) {
                url.append("?sslmode=").append(sslMode);
            }

            return url.toString();
        }
    }

    public record StorageProperties(
        boolean enabled,
        String avatarsBucket,
        String documentsBucket,
        String transactionsBucket,
        CompressionProperties compression
    ) {
        public StorageProperties() {
            this(true, "avatars", "documents", "transactions", new CompressionProperties());
        }
    }

    public record CompressionProperties(
        boolean enabled,
        int level,
        double minReductionRatio,
        long minFileSizeBytes,
        List<String> skipContentTypes
    ) {
        public CompressionProperties() {
            this(true, 6, 0.1, 1024, List.of(
                "image/jpeg", "image/png", "image/gif", "image/webp",
                "application/pdf", "application/zip", "application/gzip",
                "application/x-gzip", "application/x-compress"
            ));
        }
    }

    public record RealtimeProperties(
        boolean enabled,
        List<String> channels
    ) {
        public RealtimeProperties() {
            this(false, List.of("transactions", "dashboard", "goals"));
        }
    }
}

