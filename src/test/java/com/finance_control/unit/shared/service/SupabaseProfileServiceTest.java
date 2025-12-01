package com.finance_control.unit.shared.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.shared.service.SupabaseProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SupabaseProfileService.
 * Note: RestClient's fluent API is complex to mock, so these tests focus on
 * verifying the service methods execute without exceptions and handle errors correctly.
 * For more comprehensive testing, consider integration tests with MockRestServiceServer.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("SupabaseProfileService Tests")
class SupabaseProfileServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private SupabaseProfileService supabaseProfileService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("updateUserMetadata - should call RestClient")
    void updateUserMetadata_ShouldCallRestClient() {
        // Setup mocks for RestClient fluent API
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        Map<String, Object> metadata = Map.of("key", "value");

        // Should not throw exception
        supabaseProfileService.updateUserMetadata("token", metadata);

        // Verify RestClient was called
        verify(restClient).put();
        verify(requestBodyUriSpec).uri("/auth/v1/user");
        verify(requestBodySpec).header("Authorization", "Bearer token");
    }

    @Test
    @DisplayName("updateUserEmail - should call RestClient")
    void updateUserEmail_ShouldCallRestClient() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        supabaseProfileService.updateUserEmail("token", "new@example.com");

        verify(restClient).put();
        verify(requestBodyUriSpec).uri("/auth/v1/user");
    }

    @Test
    @DisplayName("updateUserPassword - should call RestClient")
    void updateUserPassword_ShouldCallRestClient() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        supabaseProfileService.updateUserPassword("token", "newPassword123");

        verify(restClient).put();
    }

    @Test
    @DisplayName("getUserProfile - should return user profile")
    @SuppressWarnings("unchecked")
    void getUserProfile_ShouldReturnUserProfile() throws Exception {
        String profileJson = "{\"id\":\"123\",\"email\":\"test@example.com\"}";
        JsonNode expectedProfile = objectMapper.readTree(profileJson);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(JsonNode.class)).thenReturn(expectedProfile);

        JsonNode result = supabaseProfileService.getUserProfile("token");

        assertThat(result).isNotNull();
        assertThat(result.get("id").asText()).isEqualTo("123");
        assertThat(result.get("email").asText()).isEqualTo("test@example.com");
        verify(restClient).get();
    }

    @Test
    @DisplayName("updateUserPreferences - should add timestamp")
    void updateUserPreferences_ShouldAddTimestamp() {
        Map<String, Object> preferences = new java.util.HashMap<>(Map.of("theme", "dark"));

        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        supabaseProfileService.updateUserPreferences("token", preferences);

        // Verify preferences were updated with timestamp
        assertThat(preferences).containsKey("preferences_updated_at");
        verify(restClient).put();
    }

    @Test
    @DisplayName("linkLocalUser - should call updateUserMetadata")
    void linkLocalUser_ShouldCallUpdateUserMetadata() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        supabaseProfileService.linkLocalUser("token", 1L);

        verify(restClient).put();
    }

    @Test
    @DisplayName("updateUserAvatar - should call updateUserMetadata")
    void updateUserAvatar_ShouldCallUpdateUserMetadata() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        supabaseProfileService.updateUserAvatar("token", "https://example.com/avatar.jpg");

        verify(restClient).put();
    }

    @Test
    @DisplayName("updateDisplayName - should call updateUserMetadata")
    void updateDisplayName_ShouldCallUpdateUserMetadata() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        supabaseProfileService.updateDisplayName("token", "John Doe");

        verify(restClient).put();
    }

    @Test
    @DisplayName("updateUserMetadata - should handle RestClientException")
    void updateUserMetadata_ShouldHandleRestClientException() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new org.springframework.web.client.RestClientException("API Error"));

        Map<String, Object> metadata = Map.of("key", "value");

        assertThatThrownBy(() -> supabaseProfileService.updateUserMetadata("token", metadata))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update user metadata");
    }
}
