package com.finance_control.unit.shared.service;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.properties.SupabaseProperties;
import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.dto.LoginRequest;
import com.finance_control.shared.dto.PasswordResetRequest;
import com.finance_control.shared.dto.SignupRequest;
import com.finance_control.shared.service.SupabaseAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SupabaseAuthService.
 * Uses Mockito to mock RestClient's fluent API for testing.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("SupabaseAuthService Tests")
class SupabaseAuthServiceTest {

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

    @Mock
    private AppProperties appProperties;

    @Mock
    private SupabaseProperties supabaseConfig;

    private SupabaseAuthService authService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        when(appProperties.supabase()).thenReturn(supabaseConfig);
        when(supabaseConfig.enabled()).thenReturn(true);
        when(supabaseConfig.url()).thenReturn("https://test.supabase.co");
        when(supabaseConfig.anonKey()).thenReturn("test-anon-key");

        // Create service with mocked RestClient
        authService = new SupabaseAuthService(restClient, appProperties, objectMapper);
    }

    @Test
    @DisplayName("signup - should return AuthResponse")
    void signup_WithValidRequest_ShouldReturnAuthResponse() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .metadata(Map.of("name", "Test User"))
                .build();

        String responseJson = """
                {
                    "access_token": "access-token",
                    "refresh_token": "refresh-token",
                    "token_type": "bearer",
                    "expires_in": 3600,
                    "user": {
                        "id": "user-uuid",
                        "email": "test@example.com"
                    }
                }
                """;
        JsonNode responseNode = objectMapper.readTree(responseJson);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(JsonNode.class)).thenReturn(responseNode);

        AuthResponse result = authService.signup(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
        verify(restClient).post();
    }

    @Test
    @DisplayName("signin - should return AuthResponse")
    void signin_WithValidCredentials_ShouldReturnAuthResponse() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        String responseJson = """
                {
                    "access_token": "access-token",
                    "refresh_token": "refresh-token",
                    "token_type": "bearer",
                    "expires_in": 3600,
                    "user": {
                        "id": "user-uuid",
                        "email": "test@example.com"
                    }
                }
                """;
        JsonNode responseNode = objectMapper.readTree(responseJson);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(JsonNode.class)).thenReturn(responseNode);

        AuthResponse result = authService.signin(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
        verify(restClient).post();
    }

    @Test
    @DisplayName("refreshToken - should return new tokens")
    void refreshToken_WithValidRefreshToken_ShouldReturnNewTokens() throws Exception {
        String refreshToken = "refresh-token";
        String responseJson = """
                {
                    "access_token": "new-access-token",
                    "refresh_token": "new-refresh-token",
                    "token_type": "bearer",
                    "expires_in": 3600
                }
                """;
        JsonNode responseNode = objectMapper.readTree(responseJson);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(JsonNode.class)).thenReturn(responseNode);

        AuthResponse result = authService.refreshToken(refreshToken);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
        verify(restClient).post();
    }

    @Test
    @DisplayName("resetPassword - should complete successfully")
    void resetPassword_WithValidEmail_ShouldCompleteSuccessfully() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .email("test@example.com")
                .redirectTo("https://example.com/reset")
                .build();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        authService.resetPassword(request);

        verify(restClient).post();
        verify(requestBodyUriSpec).uri("/auth/v1/recover");
    }

    @Test
    @DisplayName("updatePassword - should complete successfully")
    void updatePassword_WithValidToken_ShouldCompleteSuccessfully() {
        String newPassword = "newPassword123";
        String accessToken = "access-token";

        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        authService.updatePassword(newPassword, accessToken);

        verify(restClient).put();
        verify(requestBodySpec).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    @Test
    @DisplayName("updateEmail - should complete successfully")
    void updateEmail_WithValidToken_ShouldCompleteSuccessfully() {
        String newEmail = "new@example.com";
        String accessToken = "access-token";

        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        authService.updateEmail(newEmail, accessToken);

        verify(restClient).put();
        verify(requestBodySpec).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    @Test
    @DisplayName("signout - should complete successfully")
    void signout_WithValidToken_ShouldCompleteSuccessfully() {
        String accessToken = "access-token";

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        authService.signout(accessToken);

        verify(restClient).post();
        verify(requestBodySpec).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    @Test
    @DisplayName("verifyEmail - should return AuthResponse")
    void verifyEmail_WithValidToken_ShouldReturnAuthResponse() throws Exception {
        String token = "verification-token";
        String type = "signup";
        String email = "test@example.com";

        String responseJson = """
                {
                    "access_token": "access-token",
                    "refresh_token": "refresh-token",
                    "token_type": "bearer",
                    "expires_in": 3600,
                    "user": {
                        "id": "user-uuid",
                        "email": "test@example.com"
                    }
                }
                """;
        JsonNode responseNode = objectMapper.readTree(responseJson);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(JsonNode.class)).thenReturn(responseNode);

        AuthResponse result = authService.verifyEmail(token, type, email);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getUser().getEmail()).isEqualTo(email);
        verify(restClient).post();
    }

    @Test
    @DisplayName("getUserInfo - should return user data")
    @SuppressWarnings("unchecked")
    void getUserInfo_WithValidToken_ShouldReturnUserData() throws Exception {
        String accessToken = "access-token";
        String userJson = """
                {
                    "id": "user-uuid",
                    "email": "test@example.com",
                    "email_confirmed_at": "2024-01-01T00:00:00Z"
                }
                """;
        JsonNode userNode = objectMapper.readTree(userJson);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(JsonNode.class)).thenReturn(userNode);

        JsonNode result = authService.getUserInfo(accessToken);

        assertThat(result).isNotNull();
        assertThat(result.get("email").asText()).isEqualTo("test@example.com");
        verify(restClient).get();
        verify(requestHeadersSpec).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    @Test
    @DisplayName("signup - should handle Supabase error")
    void signup_WithSupabaseError_ShouldHandleError() {
        SignupRequest request = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(JsonNode.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Email already registered"));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Supabase authentication error");
    }

    @Test
    void isSupabaseAuthEnabled_WithValidConfig_ShouldReturnTrue() {
        // When
        boolean enabled = authService.isSupabaseAuthEnabled();

        // Then
        assertThat(enabled).isTrue();
    }

    @Test
    void isSupabaseAuthEnabled_WithMissingUrl_ShouldReturnFalse() {
        // Given
        when(supabaseConfig.url()).thenReturn("");

        // When
        boolean enabled = authService.isSupabaseAuthEnabled();

        // Then
        assertThat(enabled).isFalse();
    }
}
