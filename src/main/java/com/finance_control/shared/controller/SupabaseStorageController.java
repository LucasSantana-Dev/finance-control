package com.finance_control.shared.controller;

import com.finance_control.shared.service.SupabaseStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for Supabase Storage operations.
 * Provides endpoints for file upload, download, deletion, and URL generation.
 */
@Slf4j
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.storage.enabled", havingValue = "true", matchIfMissing = false)
@Tag(name = "Supabase Storage", description = "File storage operations using Supabase Storage")
public class SupabaseStorageController {

    private final SupabaseStorageService storageService;

    /**
     * Uploads an avatar image for the authenticated user.
     *
     * @param avatarFile the avatar image file
     * @return response with upload result
     */
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload user avatar", description = "Uploads an avatar image for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @Parameter(description = "Avatar image file") @RequestParam("file") MultipartFile avatarFile) {

        try {
            // TODO: Get user ID from security context
            Long userId = getCurrentUserId();

            String avatarUrl = storageService.uploadAvatar(userId, avatarFile);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avatar uploaded successfully");
            response.put("url", avatarUrl);
            response.put("fileName", avatarFile.getOriginalFilename());

            log.info("Avatar uploaded for user {}: {}", userId, avatarUrl);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Failed to upload avatar", e);
            return createErrorResponse("Failed to upload avatar: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unexpected error during avatar upload", e);
            return createErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Uploads a document file for the authenticated user.
     *
     * @param documentFile the document file
     * @return response with upload result
     */
    @PostMapping("/document")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload document", description = "Uploads a document file for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @Parameter(description = "Document file") @RequestParam("file") MultipartFile documentFile) {

        try {
            Long userId = getCurrentUserId();

            // TODO: Implement uploadDocument in SupabaseStorageService
            // String documentUrl = storageService.uploadDocument(userId, documentFile);
            String documentUrl = "TODO: Implement uploadDocument";

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document uploaded successfully");
            response.put("url", documentUrl);
            response.put("fileName", documentFile.getOriginalFilename());

            log.info("Document uploaded for user {}: {}", userId, documentUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Unexpected error during document upload", e);
            return createErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Uploads an attachment for a specific transaction.
     *
     * @param transactionId the transaction ID
     * @param attachmentFile the attachment file
     * @return response with upload result
     */
    @PostMapping("/transaction/{transactionId}/attachment")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload transaction attachment", description = "Uploads an attachment file for a specific transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not transaction owner"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> uploadTransactionAttachment(
            @Parameter(description = "Transaction ID") @PathVariable Long transactionId,
            @Parameter(description = "Attachment file") @RequestParam("file") MultipartFile attachmentFile) {

        try {
            Long userId = getCurrentUserId();

            // TODO: Implement uploadTransactionAttachment in SupabaseStorageService
            // String attachmentUrl = storageService.uploadTransactionAttachment(userId, transactionId, attachmentFile);
            String attachmentUrl = "TODO: Implement uploadTransactionAttachment";

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transaction attachment uploaded successfully");
            response.put("url", attachmentUrl);
            response.put("fileName", attachmentFile.getOriginalFilename());
            response.put("transactionId", transactionId);

            log.info("Transaction attachment uploaded for user {} and transaction {}: {}", userId, transactionId, attachmentUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Unexpected error during transaction attachment upload", e);
            return createErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Downloads a file from storage.
     *
     * @param bucketName the bucket name
     * @param fileName the file name
     * @return the file as a downloadable resource
     */
    @GetMapping("/download/{bucketName}/{fileName}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Download file", description = "Downloads a file from Supabase Storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Storage bucket name") @PathVariable String bucketName,
            @Parameter(description = "File name") @PathVariable String fileName) {

        try {
            Resource fileResource = storageService.downloadFile(bucketName, fileName);

            if (fileResource == null || !fileResource.exists()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            log.info("File downloaded: {}/{}", bucketName, fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileResource);

        } catch (Exception e) {
            log.error("Failed to download file {}/{}", bucketName, fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generates a public URL for a file.
     *
     * @param bucketName the bucket name
     * @param fileName the file name
     * @return response with public URL
     */
    @GetMapping("/url/{bucketName}/{fileName}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate public URL", description = "Generates a public URL for accessing a file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getPublicUrl(
            @Parameter(description = "Storage bucket name") @PathVariable String bucketName,
            @Parameter(description = "File name") @PathVariable String fileName) {

        try {
            String publicUrl = storageService.generatePublicUrl(bucketName, fileName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("url", publicUrl);
            response.put("bucketName", bucketName);
            response.put("fileName", fileName);

            log.debug("Generated public URL for {}/{}: {}", bucketName, fileName, publicUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to generate public URL for {}/{}", bucketName, fileName, e);
            return createErrorResponse("Failed to generate public URL", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates a signed URL for private file access.
     *
     * @param bucketName the bucket name
     * @param fileName the file name
     * @param expiresIn expiration time in seconds (optional, defaults to 3600)
     * @return response with signed URL
     */
    @GetMapping("/signed-url/{bucketName}/{fileName}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate signed URL", description = "Generates a signed URL for private file access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signed URL generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getSignedUrl(
            @Parameter(description = "Storage bucket name") @PathVariable String bucketName,
            @Parameter(description = "File name") @PathVariable String fileName,
            @Parameter(description = "Expiration time in seconds") @RequestParam(defaultValue = "3600") int expiresIn) {

        try {
            // TODO: Implement generateSignedUrl in SupabaseStorageService
            // String signedUrl = storageService.generateSignedUrl(bucketName, fileName, expiresIn);
            String signedUrl = "TODO: Implement generateSignedUrl";

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("signedUrl", signedUrl);
            response.put("expiresIn", expiresIn);
            response.put("bucketName", bucketName);
            response.put("fileName", fileName);

            log.debug("Generated signed URL for {}/{} with expiration {}s", bucketName, fileName, expiresIn);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to generate signed URL for {}/{}", bucketName, fileName, e);
            return createErrorResponse("Failed to generate signed URL", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a file from storage.
     *
     * @param bucketName the bucket name
     * @param fileName the file name
     * @return response with deletion result
     */
    @DeleteMapping("/{bucketName}/{fileName}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete file", description = "Deletes a file from Supabase Storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> deleteFile(
            @Parameter(description = "Storage bucket name") @PathVariable String bucketName,
            @Parameter(description = "File name") @PathVariable String fileName) {

        try {
            boolean deleted = storageService.deleteFile(bucketName, fileName);

            if (!deleted) {
                return createErrorResponse("File not found or could not be deleted", HttpStatus.NOT_FOUND);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File deleted successfully");
            response.put("bucketName", bucketName);
            response.put("fileName", fileName);

            log.info("File deleted: {}/{}", bucketName, fileName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to delete file {}/{}", bucketName, fileName, e);
            return createErrorResponse("Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Lists files in a bucket (with optional path filtering).
     *
     * @param bucketName the bucket name
     * @param path optional path prefix
     * @param limit maximum number of files to return
     * @return response with file list
     */
    @GetMapping("/list/{bucketName}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List files", description = "Lists files in a Supabase Storage bucket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files listed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> listFiles(
            @Parameter(description = "Storage bucket name") @PathVariable String bucketName,
            @Parameter(description = "Path prefix filter") @RequestParam(required = false) String path,
            @Parameter(description = "Maximum number of files") @RequestParam(defaultValue = "100") int limit) {

        try {
            // TODO: Implement listFiles in SupabaseStorageService
            // String[] files = storageService.listFiles(bucketName, path, limit);
            String[] files = new String[]{"TODO: Implement listFiles"};

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bucketName", bucketName);
            response.put("path", path);
            response.put("files", files);
            response.put("count", files.length);

            log.debug("Listed {} files in bucket {} with path {}", files.length, bucketName, path);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to list files in bucket {}", bucketName, e);
            return createErrorResponse("Failed to list files", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a standardized error response.
     *
     * @param message the error message
     * @param status the HTTP status
     * @return ResponseEntity with error details
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Gets the current authenticated user ID.
     * TODO: Implement proper user ID extraction from security context
     *
     * @return the current user ID
     */
    private Long getCurrentUserId() {
        // TODO: Extract from security context
        // For now, return a placeholder - this should be implemented properly
        return 1L; // Placeholder
    }
}
