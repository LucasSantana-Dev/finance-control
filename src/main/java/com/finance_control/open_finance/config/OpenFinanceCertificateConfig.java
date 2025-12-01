package com.finance_control.open_finance.config;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.properties.OpenFinanceProperties;
import com.finance_control.shared.service.SupabaseStorageService;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

/**
 * Configuration for Open Finance certificate-based authentication (mTLS).
 * Supports loading certificates from files, keystore, or Supabase Storage.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinanceCertificateConfig {

    private final AppProperties appProperties;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SupabaseStorageService supabaseStorageService;

    /**
     * Creates an SSL context configured for mutual TLS (mTLS) with Open Finance
     * APIs.
     * Supports loading certificates from files, keystore, or Supabase Storage.
     *
     * @return configured SslContext for mTLS
     */
    @Bean
    @ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
    public SslContext openFinanceSslContext() {
        OpenFinanceProperties.CertificatesProperties certConfig = appProperties.openFinance().certificates();

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();

            // Load client certificate and private key
            if (certConfig.useSupabaseStorage() && supabaseStorageService != null) {
                log.info("Loading certificates from Supabase Storage");
                loadCertificatesFromSupabase(sslContextBuilder, certConfig);
            } else if (StringUtils.hasText(certConfig.keystorePath())) {
                log.info("Loading certificates from keystore: {}", certConfig.keystorePath());
                loadCertificatesFromKeystore(sslContextBuilder, certConfig);
            } else if (StringUtils.hasText(certConfig.clientCertificatePath()) &&
                    StringUtils.hasText(certConfig.privateKeyPath())) {
                log.info("Loading certificates from files");
                loadCertificatesFromFiles(sslContextBuilder, certConfig);
            } else {
                log.warn("No certificate configuration found. Open Finance API calls may fail.");
                return sslContextBuilder.build();
            }

            // Load CA certificate for trust store
            if (StringUtils.hasText(certConfig.caCertificatePath())) {
                loadCaCertificate(sslContextBuilder, certConfig);
            } else {
                log.warn("No CA certificate configured. Using default trust store.");
            }

            return sslContextBuilder.build();

        } catch (Exception e) {
            log.error("Failed to configure SSL context for Open Finance", e);
            throw new IllegalStateException("Failed to configure Open Finance SSL context", e);
        }
    }

    /**
     * Creates a Java SSLContext configured for mutual TLS (mTLS) with Open Finance APIs.
     * This is used by RestClient for HTTP calls.
     *
     * @return configured SSLContext for mTLS
     */
    public javax.net.ssl.SSLContext openFinanceJavaSslContext() {
        OpenFinanceProperties.CertificatesProperties certConfig = appProperties.openFinance().certificates();

        try {
            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();

            // Load client certificate and private key
            if (certConfig.useSupabaseStorage() && supabaseStorageService != null) {
                log.info("Loading certificates from Supabase Storage for RestClient");
                loadCertificatesFromSupabaseJava(sslContextBuilder, certConfig);
            } else if (StringUtils.hasText(certConfig.keystorePath())) {
                log.info("Loading certificates from keystore for RestClient: {}", certConfig.keystorePath());
                loadCertificatesFromKeystoreJava(sslContextBuilder, certConfig);
            } else if (StringUtils.hasText(certConfig.clientCertificatePath()) &&
                    StringUtils.hasText(certConfig.privateKeyPath())) {
                log.info("Loading certificates from files for RestClient");
                loadCertificatesFromFilesJava(sslContextBuilder, certConfig);
            } else {
                log.warn("No certificate configuration found. Open Finance API calls may fail.");
                return sslContextBuilder.build();
            }

            // Load CA certificate for trust store
            if (StringUtils.hasText(certConfig.caCertificatePath())) {
                loadCaCertificateJava(sslContextBuilder, certConfig);
            } else {
                log.warn("No CA certificate configured. Using default trust store.");
            }

            return sslContextBuilder.build();

        } catch (Exception e) {
            log.error("Failed to configure Java SSL context for Open Finance", e);
            throw new IllegalStateException("Failed to configure Open Finance Java SSL context", e);
        }
    }

    private void loadCertificatesFromFiles(SslContextBuilder builder,
            OpenFinanceProperties.CertificatesProperties certConfig) throws Exception {
        try (FileInputStream certStream = new FileInputStream(certConfig.clientCertificatePath());
                FileInputStream keyStream = new FileInputStream(certConfig.privateKeyPath())) {

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(certStream);

            // Read private key (PEM format)
            String privateKeyPem = new String(Files.readAllBytes(Paths.get(certConfig.privateKeyPath())));
            RSAPrivateKey privateKey = parsePrivateKey(privateKeyPem);

            builder.keyManager(privateKey, certificate);
            log.info("Successfully loaded certificates from files");
        }
    }

    private void loadCertificatesFromKeystore(SslContextBuilder builder,
            OpenFinanceProperties.CertificatesProperties certConfig) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream keystoreStream = new FileInputStream(certConfig.keystorePath())) {
            keyStore.load(keystoreStream, certConfig.keystorePassword().toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, certConfig.keystorePassword().toCharArray());

            builder.keyManager(keyManagerFactory);
            log.info("Successfully loaded certificates from keystore");
        }
    }

    private void loadCertificatesFromSupabase(SslContextBuilder builder,
            OpenFinanceProperties.CertificatesProperties certConfig) throws Exception {
        if (supabaseStorageService == null) {
            throw new IllegalStateException("Supabase Storage service is not available");
        }

        // Download certificates from Supabase Storage
        var certResource = supabaseStorageService.downloadFile(
                certConfig.supabaseStorageBucket(), "client-certificate.pem");
        var keyResource = supabaseStorageService.downloadFile(
                certConfig.supabaseStorageBucket(), "private-key.pem");

        try (InputStream certStream = certResource.getInputStream();
                InputStream keyStream = keyResource.getInputStream()) {

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(certStream);

            byte[] keyBytes = keyStream.readAllBytes();
            String privateKeyPem = new String(keyBytes);
            RSAPrivateKey privateKey = parsePrivateKey(privateKeyPem);

            builder.keyManager(privateKey, certificate);
            log.info("Successfully loaded certificates from Supabase Storage");
        }
    }

    private void loadCaCertificate(SslContextBuilder builder,
            OpenFinanceProperties.CertificatesProperties certConfig) throws Exception {
        InputStream caStream;
        if (certConfig.useSupabaseStorage() && supabaseStorageService != null) {
            var caResource = supabaseStorageService.downloadFile(
                    certConfig.supabaseStorageBucket(), "ca-certificate.pem");
            caStream = caResource.getInputStream();
        } else {
            caStream = new FileInputStream(certConfig.caCertificatePath());
        }

        try (caStream) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            Certificate caCertificate = certFactory.generateCertificate(caStream);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("ca", caCertificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            builder.trustManager(trustManagerFactory);
            log.info("Successfully loaded CA certificate");
        }
    }

    private RSAPrivateKey parsePrivateKey(String privateKeyPem) throws Exception {
        // Remove PEM headers and whitespace
        String privateKeyContent = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

        // Use BouncyCastle or Java's built-in PKCS8 parser
        java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
        java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    // Java SSLContext helper methods (for RestClient)
    private void loadCertificatesFromFilesJava(SSLContextBuilder builder,
            OpenFinanceProperties.CertificatesProperties certConfig) throws Exception {
        try (FileInputStream certStream = new FileInputStream(certConfig.clientCertificatePath());
                FileInputStream keyStream = new FileInputStream(certConfig.privateKeyPath())) {

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(certStream);

            String privateKeyPem = new String(Files.readAllBytes(Paths.get(certConfig.privateKeyPath())));
            RSAPrivateKey privateKey = parsePrivateKey(privateKeyPem);

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            keyStore.setKeyEntry("client", privateKey, "".toCharArray(), new Certificate[]{certificate});

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "".toCharArray());

            builder.loadKeyMaterial(keyStore, "".toCharArray());
            log.info("Successfully loaded certificates from files for RestClient");
        }
    }

    private void loadCertificatesFromKeystoreJava(SSLContextBuilder builder,
            OpenFinanceProperties.CertificatesProperties certConfig) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream keystoreStream = new FileInputStream(certConfig.keystorePath())) {
            keyStore.load(keystoreStream, certConfig.keystorePassword().toCharArray());
            builder.loadKeyMaterial(keyStore, certConfig.keystorePassword().toCharArray());
            log.info("Successfully loaded certificates from keystore for RestClient");
        }
    }

    private void loadCertificatesFromSupabaseJava(SSLContextBuilder builder,
            OpenFinanceProperties.CertificatesProperties certConfig) throws Exception {
        if (supabaseStorageService == null) {
            throw new IllegalStateException("Supabase Storage service is not available");
        }

        var certResource = supabaseStorageService.downloadFile(
                certConfig.supabaseStorageBucket(), "client-certificate.pem");
        var keyResource = supabaseStorageService.downloadFile(
                certConfig.supabaseStorageBucket(), "private-key.pem");

        try (InputStream certStream = certResource.getInputStream();
                InputStream keyStream = keyResource.getInputStream()) {

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(certStream);

            byte[] keyBytes = keyStream.readAllBytes();
            String privateKeyPem = new String(keyBytes);
            RSAPrivateKey privateKey = parsePrivateKey(privateKeyPem);

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            keyStore.setKeyEntry("client", privateKey, "".toCharArray(), new Certificate[]{certificate});

            builder.loadKeyMaterial(keyStore, "".toCharArray());
            log.info("Successfully loaded certificates from Supabase Storage for RestClient");
        }
    }

    private void loadCaCertificateJava(SSLContextBuilder builder,
            OpenFinanceProperties.CertificatesProperties certConfig) throws Exception {
        InputStream caStream;
        if (certConfig.useSupabaseStorage() && supabaseStorageService != null) {
            var caResource = supabaseStorageService.downloadFile(
                    certConfig.supabaseStorageBucket(), "ca-certificate.pem");
            caStream = caResource.getInputStream();
        } else {
            caStream = new FileInputStream(certConfig.caCertificatePath());
        }

        try (caStream) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            Certificate caCertificate = certFactory.generateCertificate(caStream);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("ca", caCertificate);

            builder.loadTrustMaterial(trustStore, null);
            log.info("Successfully loaded CA certificate for RestClient");
        }
    }
}
