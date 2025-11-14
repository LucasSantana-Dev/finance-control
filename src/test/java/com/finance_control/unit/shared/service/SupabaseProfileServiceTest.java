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
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupabaseProfileService Tests")
class SupabaseProfileServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SupabaseProfileService supabaseProfileService;

    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.RequestBodySpec requestBodySpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        requestBodySpec = mock(WebClient.RequestBodySpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        ReflectionTestUtils.setField(supabaseProfileService, "webClient", webClient);
        ReflectionTestUtils.setField(supabaseProfileService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("updateUserMetadata - should call Supabase API")
    void updateUserMetadata_ShouldCallSupabaseApi() {
        Map<String, Object> metadata = Map.of("key", "value");

        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/auth/v1/user")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        Mono<Void> result = supabaseProfileService.updateUserMetadata("token", metadata);

        StepVerifier.create(result)
                .verifyComplete();

        verify(webClient).put();
        verify(requestBodyUriSpec).uri("/auth/v1/user");
        verify(requestBodySpec).header("Authorization", "Bearer token");
        verify(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("updateUserEmail - should call Supabase API")
    void updateUserEmail_ShouldCallSupabaseApi() {
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/auth/v1/user")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        Mono<Void> result = supabaseProfileService.updateUserEmail("token", "new@example.com");

        StepVerifier.create(result)
                .verifyComplete();

        verify(webClient).put();
        verify(requestBodyUriSpec).uri("/auth/v1/user");
        verify(requestBodySpec).header("Authorization", "Bearer token");
    }

    @Test
    @DisplayName("updateUserPassword - should call Supabase API")
    void updateUserPassword_ShouldCallSupabaseApi() {
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/auth/v1/user")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        Mono<Void> result = supabaseProfileService.updateUserPassword("token", "newPassword123");

        StepVerifier.create(result)
                .verifyComplete();

        verify(webClient).put();
        verify(requestBodyUriSpec).uri("/auth/v1/user");
        verify(requestBodySpec).header("Authorization", "Bearer token");
    }

    @Test
    @DisplayName("getUserProfile - should return user profile")
    void getUserProfile_ShouldReturnUserProfile() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode profileData = mapper.readTree("{\"id\":\"123\",\"email\":\"test@example.com\"}");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/auth/v1/user")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(eq("Authorization"), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(profileData));

        Mono<JsonNode> result = supabaseProfileService.getUserProfile("token");

        StepVerifier.create(result)
                .expectNext(profileData)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/auth/v1/user");
        verify(requestHeadersSpec).header("Authorization", "Bearer token");
    }

    @Test
    @DisplayName("updateUserPreferences - should add timestamp and call updateUserMetadata")
    void updateUserPreferences_ShouldAddTimestampAndCallUpdateUserMetadata() {
        Map<String, Object> preferences = new java.util.HashMap<>(Map.of("theme", "dark"));

        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/auth/v1/user")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        Mono<Void> result = supabaseProfileService.updateUserPreferences("token", preferences);

        StepVerifier.create(result)
                .verifyComplete();

        verify(webClient).put();
    }

    @Test
    @DisplayName("linkLocalUser - should call updateUserMetadata with link data")
    void linkLocalUser_ShouldCallUpdateUserMetadataWithLinkData() {
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/auth/v1/user")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        Mono<Void> result = supabaseProfileService.linkLocalUser("token", 1L);

        StepVerifier.create(result)
                .verifyComplete();

        verify(webClient).put();
    }

    @Test
    @DisplayName("updateUserAvatar - should call updateUserMetadata with avatar URL")
    void updateUserAvatar_ShouldCallUpdateUserMetadataWithAvatarUrl() {
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/auth/v1/user")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        Mono<Void> result = supabaseProfileService.updateUserAvatar("token", "https://example.com/avatar.jpg");

        StepVerifier.create(result)
                .verifyComplete();

        verify(webClient).put();
    }

    @Test
    @DisplayName("updateDisplayName - should call updateUserMetadata with display name")
    void updateDisplayName_ShouldCallUpdateUserMetadataWithDisplayName() {
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/auth/v1/user")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        Mono<Void> result = supabaseProfileService.updateDisplayName("token", "John Doe");

        StepVerifier.create(result)
                .verifyComplete();

        verify(webClient).put();
    }
}
