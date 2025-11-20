package com.finance_control.unit.shared.service;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.properties.SupabaseProperties;
import com.finance_control.shared.dto.SignupRequest;
import com.finance_control.shared.dto.LoginRequest;
import com.finance_control.shared.dto.PasswordResetRequest;
import com.finance_control.shared.service.SupabaseAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupabaseAuthServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private AppProperties appProperties;

    @Mock
    private SupabaseProperties supabaseConfig;

    private ObjectMapper objectMapper;
    private SupabaseAuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        when(appProperties.supabase()).thenReturn(supabaseConfig);
        when(supabaseConfig.enabled()).thenReturn(true);
        when(supabaseConfig.url()).thenReturn("https://test.supabase.co");
        when(supabaseConfig.anonKey()).thenReturn("test-anon-key");

        // Create service instance with mocked WebClient
        authService = new SupabaseAuthService(webClient, appProperties, objectMapper);

        // Mock the WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(createMockAuthResponse()));
    }

    @Test
    void signup_WithValidRequest_ShouldReturnAuthResponse() {
        // Given
        SignupRequest request = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .metadata(Map.of("full_name", "Test User"))
                .build();

        // When & Then
        StepVerifier.create(authService.signup(request))
                .expectNextMatches(response -> {
                    assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
                    assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
                    return true;
                })
                .verifyComplete();

        verify(webClient, times(1)).post();
        verify(requestBodyUriSpec).uri("/auth/v1/signup");
    }

    @Test
    void signin_WithValidCredentials_ShouldReturnAuthResponse() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        // When & Then
        StepVerifier.create(authService.signin(request))
                .expectNextMatches(response -> {
                    assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
                    assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
                    return true;
                })
                .verifyComplete();

        verify(requestBodyUriSpec).uri("/auth/v1/token?grant_type=password");
    }

    @Test
    void refreshToken_WithValidRefreshToken_ShouldReturnNewTokens() {
        // Given
        String refreshToken = "refresh-token-123";

        // When & Then
        StepVerifier.create(authService.refreshToken(refreshToken))
                .expectNextMatches(response -> {
                    assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
                    return true;
                })
                .verifyComplete();

        verify(requestBodyUriSpec).uri("/auth/v1/token?grant_type=refresh_token");
    }

    @Test
    void resetPassword_WithValidEmail_ShouldCompleteSuccessfully() {
        // Given
        PasswordResetRequest request = PasswordResetRequest.builder()
                .email("test@example.com")
                .build();

        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(authService.resetPassword(request))
                .verifyComplete();

        verify(requestBodyUriSpec).uri("/auth/v1/recover");
    }

    @Test
    void updatePassword_WithValidToken_ShouldCompleteSuccessfully() {
        // Given
        String newPassword = "newpassword123";
        String accessToken = "access-token-123";

        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // Mock PUT request chain
        WebClient.RequestBodyUriSpec putUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec putBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> putHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec putResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.put()).thenReturn(putUriSpec);
        when(putUriSpec.uri(anyString())).thenReturn(putBodySpec);
        when(putBodySpec.header(anyString(), anyString())).thenReturn(putBodySpec);
        when(putBodySpec.contentType(any())).thenReturn(putBodySpec);
        doReturn(putHeadersSpec).when(putBodySpec).bodyValue(any());
        when(putHeadersSpec.retrieve()).thenReturn(putResponseSpec);
        when(putResponseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(authService.updatePassword(newPassword, accessToken))
                .verifyComplete();

        verify(webClient, times(1)).put();
        verify(putUriSpec).uri("/auth/v1/user");
    }

    @Test
    void updateEmail_WithValidToken_ShouldCompleteSuccessfully() {
        // Given
        String newEmail = "newemail@example.com";
        String accessToken = "access-token-123";

        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // Mock PUT request chain
        WebClient.RequestBodyUriSpec putUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec putBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> putHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec putResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.put()).thenReturn(putUriSpec);
        when(putUriSpec.uri(anyString())).thenReturn(putBodySpec);
        when(putBodySpec.header(anyString(), anyString())).thenReturn(putBodySpec);
        when(putBodySpec.contentType(any())).thenReturn(putBodySpec);
        doReturn(putHeadersSpec).when(putBodySpec).bodyValue(any());
        when(putHeadersSpec.retrieve()).thenReturn(putResponseSpec);
        when(putResponseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(authService.updateEmail(newEmail, accessToken))
                .verifyComplete();

        verify(webClient, times(1)).put();
        verify(putUriSpec).uri("/auth/v1/user");
    }

    @Test
    void signout_WithValidToken_ShouldCompleteSuccessfully() {
        // Given
        String accessToken = "access-token-123";

        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // Mock POST request chain for signout
        WebClient.RequestBodyUriSpec postUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec postResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.header(anyString(), anyString())).thenReturn(postBodySpec);
        doReturn(postResponseSpec).when(postBodySpec).retrieve();
        doReturn(postHeadersSpec).when(postBodySpec).bodyValue(any());
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(authService.signout(accessToken))
                .verifyComplete();

        verify(webClient, times(1)).post();
        verify(postUriSpec).uri("/auth/v1/logout");
    }

    @Test
    void verifyEmail_WithValidToken_ShouldReturnAuthResponse() {
        // Given
        String token = "verification-token";
        String type = "signup";
        String email = "test@example.com";

        // When & Then
        StepVerifier.create(authService.verifyEmail(token, type, email))
                .expectNextMatches(response -> {
                    assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
                    return true;
                })
                .verifyComplete();

        verify(requestBodyUriSpec).uri("/auth/v1/verify");
    }

    @Test
    void getUserInfo_WithValidToken_ShouldReturnUserData() {
        // Given
        String accessToken = "access-token-123";

        // Mock GET request chain
        WebClient.RequestHeadersUriSpec<?> getUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> getHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec getResponseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(getUriSpec).when(webClient).get();
        doReturn(getHeadersSpec).when(getUriSpec).uri(anyString());
        doReturn(getHeadersSpec).when(getHeadersSpec).header(anyString(), anyString());
        when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);
        when(getResponseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(createMockUserInfo()));

        // When & Then
        StepVerifier.create(authService.getUserInfo(accessToken))
                .expectNextMatches(userInfo -> {
                    assertThat(userInfo.get("email").asText()).isEqualTo("test@example.com");
                    return true;
                })
                .verifyComplete();

        verify(webClient, times(1)).get();
        verify(getUriSpec).uri("/auth/v1/user");
    }

    @Test
    void signup_WithSupabaseError_ShouldHandleError() {
        // Given
        SignupRequest request = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        WebClientResponseException error = WebClientResponseException.create(
                400, "Bad Request", null, "Invalid email".getBytes(), null);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.error(error));

        // When & Then
        StepVerifier.create(authService.signup(request))
                .expectError(WebClientResponseException.class)
                .verify();
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

    private JsonNode createMockAuthResponse() {
        try {
            String json = """
                    {
                        "access_token": "mock-access-token",
                        "refresh_token": "mock-refresh-token",
                        "token_type": "bearer",
                        "expires_in": 3600,
                        "expires_at": 1638360000,
                        "user": {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "email": "test@example.com",
                            "email_confirmed_at": "2023-01-01T00:00:00Z",
                            "role": "authenticated",
                            "user_metadata": {},
                            "app_metadata": {},
                            "created_at": "2023-01-01T00:00:00Z",
                            "updated_at": "2023-01-01T00:00:00Z",
                            "confirmed_at": "2023-01-01T00:00:00Z"
                        }
                    }
                    """;
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode createMockUserInfo() {
        try {
            String json = """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "email": "test@example.com",
                        "email_confirmed_at": "2023-01-01T00:00:00Z",
                        "role": "authenticated",
                        "user_metadata": {"full_name": "Test User"},
                        "app_metadata": {},
                        "created_at": "2023-01-01T00:00:00Z",
                        "updated_at": "2023-01-01T00:00:00Z"
                    }
                    """;
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
