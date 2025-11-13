package com.finance_control.integration;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.service.SupabaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Integration tests for SupabaseStorageService.
 * These tests require a real Supabase project and are disabled by default.
 * To run these tests:
 * 1. Set up a Supabase project
 * 2. Create the required buckets (avatars, documents, transactions)
 * 3. Set environment variables: SUPABASE_URL, SUPABASE_ANON_KEY
 * 4. Remove @Disabled annotation
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires real Supabase project setup")
class SupabaseStorageIntegrationTest {

    @Autowired
    private SupabaseStorageService storageService;

    @Autowired
    private AppProperties appProperties;

    private MockMultipartFile testImageFile;
    private MockMultipartFile testDocumentFile;

    @BeforeEach
    void setUp() {
        // Create test files
        testImageFile = new MockMultipartFile(
            "avatar",
            "test-avatar.jpg",
            "image/jpeg",
            "fake image content for testing".getBytes()
        );

        testDocumentFile = new MockMultipartFile(
            "document",
            "test-document.pdf",
            "application/pdf",
            "fake pdf content for testing".getBytes()
        );
    }

    @Test
    void uploadAvatar_WithValidImage_ShouldUploadAndReturnUrl() throws IOException {
        // Given
        Long userId = 999L; // Test user ID

        // When
        String avatarUrl = storageService.uploadAvatar(userId, testImageFile);

        // Then
        assertThat(avatarUrl).isNotNull();
        assertThat(avatarUrl).contains("supabase.co/storage/v1/object/public/avatars");
        assertThat(avatarUrl).contains("test-avatar.jpg");

        // Verify the file can be accessed (optional - depends on bucket permissions)
        // This would require making an HTTP request to the returned URL
    }

    @Test
    void uploadDocument_WithValidFile_ShouldUploadAndReturnUrl() throws IOException {
        // Given
        Long userId = 999L;

        // When
        String documentUrl = storageService.uploadDocument(userId, testDocumentFile);

        // Then
        assertThat(documentUrl).isNotNull();
        assertThat(documentUrl).contains("supabase.co/storage/v1/object/public/documents");
        assertThat(documentUrl).contains("test-document.pdf");
    }

    @Test
    void uploadTransactionAttachment_WithValidFile_ShouldUploadAndReturnUrl() throws IOException {
        // Given
        Long userId = 999L;
        Long transactionId = 12345L;

        // When
        String attachmentUrl = storageService.uploadTransactionAttachment(userId, transactionId, testDocumentFile);

        // Then
        assertThat(attachmentUrl).isNotNull();
        assertThat(attachmentUrl).contains("supabase.co/storage/v1/object/public/transactions");
        assertThat(attachmentUrl).contains("test-document.pdf");
    }

    @Test
    void downloadFile_WithExistingFile_ShouldReturnResource() {
        // Given - upload a file first
        String bucketName = appProperties.supabase().storage().avatarsBucket();
        String fileName = "test-download.jpg";

        // Upload file first
        try {
            storageService.uploadFile(bucketName, fileName, testImageFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to setup test file", e);
        }

        // When
        Resource resource = storageService.downloadFile(bucketName, fileName);

        // Then
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void downloadFile_WithNonExistentFile_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> storageService.downloadFile("avatars", "nonexistent-file.jpg"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("File not found");
    }

    @Test
    void deleteFile_WithExistingFile_ShouldReturnTrue() {
        // Given - upload a file first
        String bucketName = appProperties.supabase().storage().avatarsBucket();
        String fileName = "test-delete.jpg";

        try {
            storageService.uploadFile(bucketName, fileName, testImageFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to setup test file", e);
        }

        // When
        boolean deleted = storageService.deleteFile(bucketName, fileName);

        // Then
        assertThat(deleted).isTrue();

        // Verify file is actually deleted
        assertThatThrownBy(() -> storageService.downloadFile(bucketName, fileName))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void generatePublicUrl_ShouldReturnValidUrl() {
        // When
        String publicUrl = storageService.generatePublicUrl("avatars", "test-file.jpg");

        // Then
        assertThat(publicUrl).isNotNull();
        assertThat(publicUrl).startsWith(appProperties.supabase().url());
        assertThat(publicUrl).contains("/storage/v1/object/public/avatars/test-file.jpg");
    }

    @Test
    void generateSignedUrl_ShouldReturnValidSignedUrl() {
        // When
        String signedUrl = storageService.generateSignedUrl("documents", "test-doc.pdf", 3600);

        // Then
        assertThat(signedUrl).isNotNull();
        assertThat(signedUrl).contains("supabase.co");
        // Signed URLs typically contain token parameters
        assertThat(signedUrl).contains("token=");
    }

    @Test
    void listFiles_ShouldReturnFileList() {
        // Given - upload some files first
        String bucketName = appProperties.supabase().storage().documentsBucket();

        try {
            storageService.uploadFile(bucketName, "test-file-1.pdf", testDocumentFile);
            storageService.uploadFile(bucketName, "test-file-2.pdf", testDocumentFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to setup test files", e);
        }

        // When
        String[] files = storageService.listFiles(bucketName, null, 10);

        // Then
        assertThat(files).isNotNull();
        // Note: The actual implementation may return empty array initially
        // depending on how the Supabase client library handles listing
    }

    @Test
    void uploadFile_WithLargeFile_ShouldHandleProperly() {
        // Create a large file (simulate)
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        MockMultipartFile largeFile = new MockMultipartFile(
            "largeFile",
            "large-test.jpg",
            "image/jpeg",
            largeContent
        );

        // When & Then - should handle large files appropriately
        assertThatThrownBy(() -> storageService.uploadFile("documents", "large-test.jpg", largeFile))
            .isInstanceOf(RuntimeException.class); // Or succeed depending on Supabase limits
    }

    @Test
    void uploadFile_WithInvalidBucket_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> storageService.uploadFile("nonexistent-bucket", "test.jpg", testImageFile))
            .isInstanceOf(RuntimeException.class);
    }
}
