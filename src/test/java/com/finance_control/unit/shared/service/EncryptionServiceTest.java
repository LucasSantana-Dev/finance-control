package com.finance_control.unit.shared.service;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.properties.SecurityProperties;
import com.finance_control.shared.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EncryptionServiceTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private SecurityProperties security;

    @Mock
    private SecurityProperties.EncryptionProperties encryption;

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        when(appProperties.security()).thenReturn(security);
        when(security.encryption()).thenReturn(encryption);
        when(encryption.enabled()).thenReturn(true);
        when(encryption.key()).thenReturn(""); // Empty key will generate one
        when(encryption.algorithm()).thenReturn("AES/GCM/NoPadding");

        encryptionService = new EncryptionService(appProperties);
        encryptionService.initialize();
    }

    @Test
    void encrypt_WithValidPlaintext_ShouldReturnEncryptedString() {
        // Given
        String plaintext = "test@example.com";

        // When
        String encrypted = encryptionService.encrypt(plaintext);

        // Then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(encrypted.length()).isGreaterThan(plaintext.length());
    }

    @Test
    void decrypt_WithValidCiphertext_ShouldReturnOriginalPlaintext() {
        // Given
        String plaintext = "test@example.com";
        String encrypted = encryptionService.encrypt(plaintext);

        // When
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void encrypt_WithNull_ShouldReturnNull() {
        // When
        String result = encryptionService.encrypt(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void decrypt_WithNull_ShouldReturnNull() {
        // When
        String result = encryptionService.decrypt(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void encrypt_WithEmptyString_ShouldReturnEncryptedString() {
        // Given
        String plaintext = "";

        // When
        String encrypted = encryptionService.encrypt(plaintext);

        // Then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEmpty();
    }

    @Test
    void decrypt_WithInvalidCiphertext_ShouldThrowException() {
        // Given
        String invalidCiphertext = "invalid_base64_string";

        // When/Then
        assertThatThrownBy(() -> encryptionService.decrypt(invalidCiphertext))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    void hashEmail_WithValidEmail_ShouldReturnConsistentHash() {
        // Given
        String email = "test@example.com";

        // When
        String hash1 = encryptionService.hashEmail(email);
        String hash2 = encryptionService.hashEmail(email);

        // Then
        assertThat(hash1).isNotNull();
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1.length()).isGreaterThan(0);
    }

    @Test
    void hashEmail_WithNull_ShouldReturnNull() {
        // When
        String result = encryptionService.hashEmail(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void hashEmail_WithDifferentCase_ShouldReturnSameHash() {
        // Given
        String email1 = "Test@Example.com";
        String email2 = "test@example.com";
        String email3 = "TEST@EXAMPLE.COM";

        // When
        String hash1 = encryptionService.hashEmail(email1);
        String hash2 = encryptionService.hashEmail(email2);
        String hash3 = encryptionService.hashEmail(email3);

        // Then
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash2).isEqualTo(hash3);
    }

    @Test
    void hashEmail_WithWhitespace_ShouldNormalize() {
        // Given
        String email1 = "test@example.com";
        String email2 = " test@example.com ";
        String email3 = "  test@example.com  ";

        // When
        String hash1 = encryptionService.hashEmail(email1);
        String hash2 = encryptionService.hashEmail(email2);
        String hash3 = encryptionService.hashEmail(email3);

        // Then
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash2).isEqualTo(hash3);
    }

    @Test
    void isEnabled_WhenEncryptionEnabled_ShouldReturnTrue() {
        // When
        boolean result = encryptionService.isEnabled();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void encrypt_WhenEncryptionDisabled_ShouldReturnPlaintext() {
        // Given
        when(encryption.enabled()).thenReturn(false);
        encryptionService = new EncryptionService(appProperties);
        encryptionService.initialize();

        String plaintext = "test@example.com";

        // When
        String result = encryptionService.encrypt(plaintext);

        // Then
        assertThat(result).isEqualTo(plaintext);
    }

    @Test
    void decrypt_WhenEncryptionDisabled_ShouldReturnCiphertext() {
        // Given
        when(encryption.enabled()).thenReturn(false);
        encryptionService = new EncryptionService(appProperties);
        encryptionService.initialize();

        String ciphertext = "encrypted_string";

        // When
        String result = encryptionService.decrypt(ciphertext);

        // Then
        assertThat(result).isEqualTo(ciphertext);
    }

    @Test
    void encryptDecrypt_WithSpecialCharacters_ShouldWork() {
        // Given
        String plaintext = "user+tag@example-domain.co.uk";

        // When
        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void encryptDecrypt_WithUnicodeCharacters_ShouldWork() {
        // Given
        String plaintext = "用户@例子.测试";

        // When
        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void encryptDecrypt_WithLongString_ShouldWork() {
        // Given
        String plaintext = "a".repeat(1000);

        // When
        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plaintext);
    }
}
