package com.finance_control.shared.service;

import com.finance_control.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting sensitive data using AES-256-GCM.
 * Provides secure encryption with authenticated encryption mode for data integrity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits for GCM
    private static final int GCM_TAG_LENGTH = 16; // 128 bits for authentication tag
    private static final int AES_KEY_SIZE = 256; // 256 bits for AES-256

    private final AppProperties appProperties;
    private SecretKey secretKey;

    /**
     * Initializes the encryption service with the secret key from configuration.
     * Generates a key if not provided (development only).
     */
    @jakarta.annotation.PostConstruct
    public void initialize() {
        com.finance_control.shared.config.properties.SecurityProperties.EncryptionProperties encryption = appProperties.security().encryption();

        if (!encryption.enabled()) {
            log.warn("Encryption is disabled. Email encryption will not be applied.");
            return;
        }

        String keyString = encryption.key();
        if (!StringUtils.hasText(keyString)) {
            log.warn("Encryption key not configured. Generating a temporary key for development.");
            log.warn("WARNING: This key will be lost on restart. Set APP_SECURITY_ENCRYPTION_KEY in production!");
            secretKey = generateKey();
        } else {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(keyString);
                if (keyBytes.length != 32) {
                    throw new IllegalArgumentException("Encryption key must be 32 bytes (256 bits) for AES-256");
                }
                secretKey = new SecretKeySpec(keyBytes, "AES");
                log.info("Encryption service initialized with configured key");
            } catch (IllegalArgumentException e) {
                log.error("Invalid encryption key format. Key must be Base64-encoded 32-byte value.", e);
                throw new IllegalStateException("Failed to initialize encryption service", e);
            }
        }
    }

    /**
     * Encrypts a plaintext string using AES-256-GCM.
     *
     * @param plaintext the plaintext to encrypt
     * @return Base64-encoded ciphertext with IV prepended
     * @throws IllegalStateException if encryption is disabled or key is not initialized
     */
    public String encrypt(String plaintext) {
        if (!appProperties.security().encryption().enabled() || secretKey == null) {
            return plaintext; // Return as-is if encryption is disabled
        }

        if (plaintext == null) {
            return null;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = cipher.doFinal(plaintextBytes);

            // Prepend IV to ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertextBytes.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertextBytes);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a Base64-encoded ciphertext string using AES-256-GCM.
     *
     * @param ciphertext the Base64-encoded ciphertext with IV prepended
     * @return the decrypted plaintext
     * @throws IllegalStateException if encryption is disabled or key is not initialized
     * @throws RuntimeException if decryption fails (invalid ciphertext, wrong key, etc.)
     */
    public String decrypt(String ciphertext) {
        if (!appProperties.security().encryption().enabled() || secretKey == null) {
            return ciphertext; // Return as-is if encryption is disabled
        }

        if (ciphertext == null) {
            return null;
        }

        try {
            byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);

            if (ciphertextBytes.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
                throw new IllegalArgumentException("Ciphertext too short");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(ciphertextBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plaintextBytes = cipher.doFinal(encryptedBytes);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt data", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Generates a hash of the email for searchable lookups.
     * Uses SHA-256 to create a deterministic hash.
     *
     * @param email the email to hash
     * @return Base64-encoded SHA-256 hash
     */
    public String hashEmail(String email) {
        if (email == null) {
            return null;
        }

        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(email.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            log.error("Failed to hash email", e);
            throw new RuntimeException("Email hashing failed", e);
        }
    }

    /**
     * Checks if encryption is enabled.
     *
     * @return true if encryption is enabled and key is initialized
     */
    public boolean isEnabled() {
        return appProperties.security().encryption().enabled() && secretKey != null;
    }

    /**
     * Generates a new AES-256 key (for development/testing only).
     *
     * @return a new SecretKey
     */
    private SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            log.error("Failed to generate encryption key", e);
            throw new RuntimeException("Key generation failed", e);
        }
    }

    /**
     * Gets the Base64-encoded encryption key (for configuration purposes).
     * WARNING: Only use in development/testing. Never log or expose in production.
     *
     * @return Base64-encoded key or null if not initialized
     */
    public String getKeyBase64() {
        if (secretKey == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
