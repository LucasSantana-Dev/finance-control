package com.finance_control.shared.service;

import com.finance_control.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

/**
 * Service for managing file storage operations with Supabase Storage.
 * Provides file upload, download, deletion, and URL generation capabilities using WebClient.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.storage.enabled", havingValue = "true", matchIfMissing = false)
public class SupabaseStorageService {

    private final AppProperties appProperties;

    @Qualifier("supabaseWebClient")
    private final WebClient webClient;

    private String supabaseUrl;
    private String anonKey;

    @PostConstruct
    public void initialize() {
        this.supabaseUrl = appProperties.supabase().url();
        this.anonKey = appProperties.supabase().anonKey();

        if (!StringUtils.hasText(supabaseUrl) || !StringUtils.hasText(anonKey)) {
            log.warn("Supabase Storage service not configured. URL or anon key is missing.");
            return;
        }

        log.info("Initialized Supabase Storage service with URL: {}", supabaseUrl);
    }

    /**
     * Uploads a file to the specified Supabase Storage bucket.
     *
     * @param bucketName the name of the bucket
     * @param fileName the name of the file in storage
     * @param file the multipart file to upload
     * @return the public URL of the uploaded file
     * @throws IOException if file processing fails
     */
    public String uploadFile(String bucketName, String fileName, MultipartFile file) throws IOException {
        validateConfiguration();
        validateFile(file);

        try {
            byte[] fileBytes = file.getBytes();
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            webClient.post()
                    .uri("/storage/v1/object/{bucket}/{path}", bucketName, fileName)
                    .contentType(MediaType.parseMediaType(contentType))
                    .bodyValue(fileBytes)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Successfully uploaded file {} to bucket {}", fileName, bucketName);
            return generatePublicUrl(bucketName, fileName);

        } catch (Exception e) {
            log.error("Failed to upload file {} to bucket {}: {}", fileName, bucketName, e.getMessage(), e);
            throw new IOException("Failed to upload file to Supabase Storage", e);
        }
    }

    /**
     * Downloads a file from the specified Supabase Storage bucket.
     *
     * @param bucketName the name of the bucket
     * @param fileName the name of the file to download
     * @return the file content as a Resource
     */
    public Resource downloadFile(String bucketName, String fileName) {
        validateConfiguration();

        try {
            byte[] fileContent = webClient.get()
                    .uri("/storage/v1/object/{bucket}/{path}", bucketName, fileName)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (fileContent == null || fileContent.length == 0) {
                throw new RuntimeException("File not found or empty: " + fileName);
            }

            log.debug("Successfully downloaded file {} from bucket {}", fileName, bucketName);
            return new ByteArrayResource(fileContent);

        } catch (Exception e) {
            log.error("Failed to download file {} from bucket {}: {}", fileName, bucketName, e.getMessage(), e);
            throw new RuntimeException("Failed to download file from Supabase Storage", e);
        }
    }

    /**
     * Deletes a file from the specified Supabase Storage bucket.
     *
     * @param bucketName the name of the bucket
     * @param fileName the name of the file to delete
     * @return true if the file was deleted successfully
     */
    public boolean deleteFile(String bucketName, String fileName) {
        validateConfiguration();

        try {
            webClient.delete()
                    .uri("/storage/v1/object/{bucket}/{path}", bucketName, fileName)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Successfully deleted file {} from bucket {}", fileName, bucketName);
            return true;

        } catch (Exception e) {
            log.error("Failed to delete file {} from bucket {}: {}", fileName, bucketName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Uploads an avatar image for a user.
     *
     * @param userId the user ID
     * @param avatarFile the avatar image file
     * @return the public URL of the uploaded avatar
     * @throws IOException if file processing fails
     */
    public String uploadAvatar(Long userId, MultipartFile avatarFile) throws IOException {
        validateFile(avatarFile);
        validateImageFile(avatarFile);

        String bucketName = appProperties.supabase().storage().avatarsBucket();
        String fileName = generateAvatarFileName(userId, avatarFile.getOriginalFilename());

        return uploadFile(bucketName, fileName, avatarFile);
    }

    /**
     * Uploads a document file for a user.
     *
     * @param userId the user ID
     * @param documentFile the document file
     * @return the public URL of the uploaded document
     * @throws IOException if file processing fails
     */
    public String uploadDocument(Long userId, MultipartFile documentFile) throws IOException {
        validateFile(documentFile);

        String bucketName = appProperties.supabase().storage().documentsBucket();
        String fileName = generateDocumentFileName(userId, documentFile.getOriginalFilename());

        return uploadFile(bucketName, fileName, documentFile);
    }

    /**
     * Uploads a transaction attachment file.
     *
     * @param userId the user ID
     * @param transactionId the transaction ID
     * @param attachmentFile the attachment file
     * @return the public URL of the uploaded attachment
     * @throws IOException if file processing fails
     */
    public String uploadTransactionAttachment(Long userId, Long transactionId, MultipartFile attachmentFile) throws IOException {
        validateFile(attachmentFile);

        String bucketName = appProperties.supabase().storage().transactionsBucket();
        String fileName = generateTransactionAttachmentFileName(userId, transactionId, attachmentFile.getOriginalFilename());

        return uploadFile(bucketName, fileName, attachmentFile);
    }

    /**
     * Generates the public URL for a file in Supabase Storage.
     *
     * @param bucketName the bucket name
     * @param fileName the file name
     * @return the public URL
     */
    public String generatePublicUrl(String bucketName, String fileName) {
        return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, fileName);
    }

    /**
     * Generates a signed URL for private file access with expiration.
     *
     * @param bucketName the bucket name
     * @param fileName the file name
     * @param expiresIn expiration time in seconds
     * @return the signed URL
     */
    public String generateSignedUrl(String bucketName, String fileName, int expiresIn) {
        validateConfiguration();

        try {
            // Supabase signed URL generation requires service role key
            // For now, return a placeholder - this would need Supabase Storage API implementation
            // TODO: Implement actual signed URL generation using Supabase Storage API
            log.warn("Signed URL generation not fully implemented. Using placeholder.");
            return String.format("%s/storage/v1/object/sign/%s/%s?expiresIn=%d", supabaseUrl, bucketName, fileName, expiresIn);
        } catch (Exception e) {
            log.error("Failed to generate signed URL for {}/{}: {}", bucketName, fileName, e.getMessage(), e);
            throw new RuntimeException("Failed to generate signed URL", e);
        }
    }

    /**
     * Lists files in a Supabase Storage bucket.
     *
     * @param bucketName the bucket name
     * @param path the path prefix filter (optional)
     * @param limit the maximum number of files to return
     * @return array of file names
     */
    public String[] listFiles(String bucketName, String path, int limit) {
        validateConfiguration();

        try {
            // Supabase Storage list API implementation
            // TODO: Implement actual file listing using Supabase Storage API
            log.warn("File listing not fully implemented. Returning empty array.");
            return new String[0];
        } catch (Exception e) {
            log.error("Failed to list files in bucket {}: {}", bucketName, e.getMessage(), e);
            return new String[0];
        }
    }

    /**
     * Checks if a URL is a Supabase Storage URL.
     *
     * @param url the URL to check
     * @return true if the URL is from Supabase Storage
     */
    public boolean isSupabaseUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        return url.startsWith(supabaseUrl + "/storage/v1/object/public/");
    }

    /**
     * Extracts the file path from a Supabase Storage URL.
     *
     * @param url the Supabase Storage URL
     * @return the file path (bucket/filename)
     */
    public String extractFilePathFromUrl(String url) {
        if (!isSupabaseUrl(url)) {
            throw new IllegalArgumentException("URL is not a valid Supabase Storage URL");
        }

        String prefix = supabaseUrl + "/storage/v1/object/public/";
        return url.substring(prefix.length());
    }

    /**
     * Validates the service configuration.
     */
    private void validateConfiguration() {
        if (!StringUtils.hasText(supabaseUrl) || !StringUtils.hasText(anonKey)) {
            throw new IllegalStateException("Supabase Storage service is not properly configured");
        }
    }

    /**
     * Validates a multipart file.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new IllegalArgumentException("File size cannot exceed 10MB");
        }
    }

    /**
     * Validates that a file is an image.
     */
    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Check for common image formats
        if (!contentType.equals("image/jpeg") &&
            !contentType.equals("image/png") &&
            !contentType.equals("image/gif") &&
            !contentType.equals("image/webp")) {
            throw new IllegalArgumentException("Unsupported image format. Use JPEG, PNG, GIF, or WebP");
        }
    }

    /**
     * Generates a unique filename for an avatar.
     */
    private String generateAvatarFileName(Long userId, String originalFilename) {
        String extension = "";
        if (StringUtils.hasText(originalFilename)) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
        }

        return String.format("avatars/%d/%s%s", userId, UUID.randomUUID().toString(), extension);
    }

    /**
     * Generates a unique filename for a document.
     */
    private String generateDocumentFileName(Long userId, String originalFilename) {
        String extension = "";
        if (StringUtils.hasText(originalFilename)) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
        }

        return String.format("documents/%d/%s%s", userId, UUID.randomUUID().toString(), extension);
    }

    /**
     * Generates a unique filename for a transaction attachment.
     */
    private String generateTransactionAttachmentFileName(Long userId, Long transactionId, String originalFilename) {
        String extension = "";
        if (StringUtils.hasText(originalFilename)) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
        }

        return String.format("transactions/%d/%d/%s%s", userId, transactionId, UUID.randomUUID().toString(), extension);
    }
}
