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
    ) {}

    public record CompressionProperties(
        boolean enabled,
        int level,
        double minReductionRatio,
        long minFileSizeBytes,
        List<String> skipContentTypes
    ) {}

    public record RealtimeProperties(
        boolean enabled,
        List<String> channels
    ) {}
}
