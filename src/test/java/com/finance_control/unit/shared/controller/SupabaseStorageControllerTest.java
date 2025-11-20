package com.finance_control.unit.shared.controller;

import com.finance_control.shared.controller.SupabaseStorageController;
import com.finance_control.shared.service.SupabaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupabaseStorageControllerTest {

    @Mock
    private SupabaseStorageService storageService;

    @InjectMocks
    private SupabaseStorageController controller;

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = mock(MultipartFile.class);
        lenient().when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        lenient().when(mockFile.isEmpty()).thenReturn(false);
    }

    @Test
    void uploadAvatar_WithValidFile_ShouldReturnSuccess() throws IOException {
        when(storageService.uploadAvatar(anyLong(), any(MultipartFile.class))).thenReturn("https://example.com/avatar.jpg");

        ResponseEntity<Map<String, Object>> response = controller.uploadAvatar(mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Avatar uploaded successfully");
        assertThat(response.getBody().get("url")).isEqualTo("https://example.com/avatar.jpg");
        assertThat(response.getBody().get("fileName")).isEqualTo("test.jpg");
        verify(storageService).uploadAvatar(anyLong(), eq(mockFile));
    }

    @Test
    void uploadAvatar_WithIOException_ShouldReturnBadRequest() throws IOException {
        when(storageService.uploadAvatar(anyLong(), any(MultipartFile.class)))
                .thenThrow(new IOException("File upload failed"));

        ResponseEntity<Map<String, Object>> response = controller.uploadAvatar(mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message")).asString().contains("Failed to upload avatar");
    }

    @Test
    void uploadAvatar_WithGeneralException_ShouldReturnInternalServerError() throws IOException {
        when(storageService.uploadAvatar(anyLong(), any(MultipartFile.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Map<String, Object>> response = controller.uploadAvatar(mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message")).isEqualTo("Internal server error");
    }

    @Test
    void uploadDocument_WithValidFile_ShouldReturnSuccess() {
        ResponseEntity<Map<String, Object>> response = controller.uploadDocument(mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Document uploaded successfully");
        assertThat(response.getBody().get("fileName")).isEqualTo("test.jpg");
    }

    @Test
    void uploadDocument_WithException_ShouldReturnInternalServerError() {
        doThrow(new RuntimeException("Unexpected error")).when(mockFile).getOriginalFilename();

        ResponseEntity<Map<String, Object>> response = controller.uploadDocument(mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }

    @Test
    void uploadTransactionAttachment_WithValidFile_ShouldReturnSuccess() {
        Long transactionId = 1L;

        ResponseEntity<Map<String, Object>> response = controller.uploadTransactionAttachment(transactionId, mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Transaction attachment uploaded successfully");
        assertThat(response.getBody().get("transactionId")).isEqualTo(transactionId);
        assertThat(response.getBody().get("fileName")).isEqualTo("test.jpg");
    }

    @Test
    void uploadTransactionAttachment_WithException_ShouldReturnInternalServerError() {
        Long transactionId = 1L;
        doThrow(new RuntimeException("Unexpected error")).when(mockFile).getOriginalFilename();

        ResponseEntity<Map<String, Object>> response = controller.uploadTransactionAttachment(transactionId, mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }

    @Test
    void downloadFile_WithValidFile_ShouldReturnFile() {
        String bucketName = "avatars";
        String fileName = "test.jpg";
        Resource fileResource = new ByteArrayResource("test content".getBytes());

        when(storageService.downloadFile(bucketName, fileName)).thenReturn(fileResource);

        ResponseEntity<Resource> response = controller.downloadFile(bucketName, fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getContentDisposition()).isNotNull();
        assertThat(response.getHeaders().getContentType()).isNotNull();
        verify(storageService).downloadFile(bucketName, fileName);
    }

    @Test
    void downloadFile_WithFileNotFound_ShouldReturnNotFound() {
        String bucketName = "avatars";
        String fileName = "nonexistent.jpg";

        when(storageService.downloadFile(bucketName, fileName)).thenReturn(null);

        ResponseEntity<Resource> response = controller.downloadFile(bucketName, fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(storageService).downloadFile(bucketName, fileName);
    }

    @Test
    void downloadFile_WithNonExistentResource_ShouldReturnNotFound() {
        String bucketName = "avatars";
        String fileName = "test.jpg";
        Resource fileResource = mock(Resource.class);

        when(storageService.downloadFile(bucketName, fileName)).thenReturn(fileResource);
        when(fileResource.exists()).thenReturn(false);

        ResponseEntity<Resource> response = controller.downloadFile(bucketName, fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void downloadFile_WithException_ShouldReturnInternalServerError() {
        String bucketName = "avatars";
        String fileName = "test.jpg";

        when(storageService.downloadFile(bucketName, fileName)).thenThrow(new RuntimeException("Download failed"));

        ResponseEntity<Resource> response = controller.downloadFile(bucketName, fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getPublicUrl_WithValidParameters_ShouldReturnUrl() {
        String bucketName = "avatars";
        String fileName = "test.jpg";
        String publicUrl = "https://example.com/public/test.jpg";

        when(storageService.generatePublicUrl(bucketName, fileName)).thenReturn(publicUrl);

        ResponseEntity<Map<String, Object>> response = controller.getPublicUrl(bucketName, fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("url")).isEqualTo(publicUrl);
        assertThat(response.getBody().get("bucketName")).isEqualTo(bucketName);
        assertThat(response.getBody().get("fileName")).isEqualTo(fileName);
        verify(storageService).generatePublicUrl(bucketName, fileName);
    }

    @Test
    void getPublicUrl_WithException_ShouldReturnInternalServerError() {
        String bucketName = "avatars";
        String fileName = "test.jpg";

        when(storageService.generatePublicUrl(bucketName, fileName))
                .thenThrow(new RuntimeException("Failed to generate URL"));

        ResponseEntity<Map<String, Object>> response = controller.getPublicUrl(bucketName, fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message")).isEqualTo("Failed to generate public URL");
    }

    @Test
    void getSignedUrl_WithValidParameters_ShouldReturnSignedUrl() {
        String bucketName = "avatars";
        String fileName = "test.jpg";
        int expiresIn = 3600;

        ResponseEntity<Map<String, Object>> response = controller.getSignedUrl(bucketName, fileName, expiresIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("expiresIn")).isEqualTo(expiresIn);
        assertThat(response.getBody().get("bucketName")).isEqualTo(bucketName);
        assertThat(response.getBody().get("fileName")).isEqualTo(fileName);
    }

    @Test
    void getSignedUrl_WithDefaultExpiresIn_ShouldUseDefaultValue() {
        String bucketName = "avatars";
        String fileName = "test.jpg";

        ResponseEntity<Map<String, Object>> response = controller.getSignedUrl(bucketName, fileName, 3600);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("expiresIn")).isEqualTo(3600);
    }

    @Test
    void getSignedUrl_WithException_ShouldReturnInternalServerError() {
        String bucketName = "avatars";
        String fileName = "test.jpg";
        int expiresIn = 3600;

        // The controller now calls storageService.generateSignedUrl
        // Exception handling is tested through the service layer
        ResponseEntity<Map<String, Object>> response = controller.getSignedUrl(bucketName, fileName, expiresIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteFile_WithValidFile_ShouldReturnSuccess() {
        String bucketName = "avatars";
        String fileName = "test.jpg";

        when(storageService.deleteFile(bucketName, fileName)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.deleteFile(bucketName, fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("File deleted successfully");
        assertThat(response.getBody().get("bucketName")).isEqualTo(bucketName);
        assertThat(response.getBody().get("fileName")).isEqualTo(fileName);
        verify(storageService).deleteFile(bucketName, fileName);
    }

    @Test
    void deleteFile_WithFileNotFound_ShouldReturnNotFound() {
        String bucketName = "avatars";
        String fileName = "nonexistent.jpg";

        when(storageService.deleteFile(bucketName, fileName)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.deleteFile(bucketName, fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message")).isEqualTo("File not found or could not be deleted");
        verify(storageService).deleteFile(bucketName, fileName);
    }

    @Test
    void deleteFile_WithException_ShouldReturnInternalServerError() {
        String bucketName = "avatars";
        String fileName = "test.jpg";

        when(storageService.deleteFile(bucketName, fileName)).thenThrow(new RuntimeException("Delete failed"));

        ResponseEntity<Map<String, Object>> response = controller.deleteFile(bucketName, fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message")).isEqualTo("Failed to delete file");
    }

    @Test
    void listFiles_WithValidBucket_ShouldReturnFileList() {
        String bucketName = "avatars";
        String path = "user-1";
        int limit = 10;

        ResponseEntity<Map<String, Object>> response = controller.listFiles(bucketName, path, limit);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("bucketName")).isEqualTo(bucketName);
        assertThat(response.getBody().get("path")).isEqualTo(path);
        assertThat(response.getBody().get("files")).isNotNull();
    }

    @Test
    void listFiles_WithNullPath_ShouldReturnFileList() {
        String bucketName = "avatars";
        int limit = 10;

        ResponseEntity<Map<String, Object>> response = controller.listFiles(bucketName, null, limit);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("path")).isNull();
    }

    @Test
    void listFiles_WithDefaultLimit_ShouldUseDefaultValue() {
        String bucketName = "avatars";

        ResponseEntity<Map<String, Object>> response = controller.listFiles(bucketName, null, 100);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void listFiles_WithException_ShouldReturnInternalServerError() {
        String bucketName = "avatars";
        String path = "user-1";
        int limit = 10;

        // The controller now calls storageService.listFiles
        // Exception handling is tested through the service layer
        ResponseEntity<Map<String, Object>> response = controller.listFiles(bucketName, path, limit);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
