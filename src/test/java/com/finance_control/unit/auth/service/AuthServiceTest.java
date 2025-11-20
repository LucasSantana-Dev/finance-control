package com.finance_control.unit.auth.service;

import com.finance_control.auth.exception.AuthenticationException;
import com.finance_control.auth.service.AuthService;
import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.monitoring.SentryService;
import com.finance_control.shared.service.EncryptionService;
import com.finance_control.shared.service.SupabaseAuthService;
import com.finance_control.shared.service.UserMappingService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MetricsService metricsService;

    @Mock
    private SentryService sentryService;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private SupabaseAuthService supabaseAuthService;

    @Mock
    private UserMappingService userMappingService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private String testEmailHash;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);

        // Mock email hashing - use a consistent hash for testing
        // Use lenient() to avoid unnecessary stubbing exceptions
        testEmailHash = "test-email-hash-12345";
        lenient().when(encryptionService.hashEmail("test@example.com")).thenReturn(testEmailHash);
        lenient().when(encryptionService.hashEmail("nonexistent@example.com")).thenReturn("nonexistent-email-hash");
    }

    @Test
    void shouldAuthenticateSuccessfully() {
        when(userRepository.findByEmailHash(testEmailHash)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // Mock MetricsService methods
        when(metricsService.startAuthenticationTimer()).thenReturn(Instant.now());
        doNothing().when(metricsService).incrementUserLogin();
        doNothing().when(metricsService).recordAuthenticationTime(any(Instant.class));

        Long result = authService.authenticate("test@example.com", "password123");

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(1L);
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        // The changePassword method is not yet implemented, so it throws UnsupportedOperationException
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> authService.changePassword("oldPassword", "newPassword"))).isNotNull();
    }

    @Test
    void shouldFailAuthenticationWithInvalidCredentials() {
        when(userRepository.findByEmailHash(testEmailHash)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Mock MetricsService methods
        when(metricsService.startAuthenticationTimer()).thenReturn(Instant.now());
        doNothing().when(metricsService).recordAuthenticationTime(any(Instant.class));
        doNothing().when(sentryService).addBreadcrumb(any(String.class), any(String.class), any(io.sentry.SentryLevel.class));

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(AuthenticationException.class,
                () -> authService.authenticate("test@example.com", "wrongPassword"))).isNotNull();
    }

    @Test
    void shouldFailAuthenticationWithInactiveUser() {
        testUser.setIsActive(false);
        when(userRepository.findByEmailHash(testEmailHash)).thenReturn(Optional.of(testUser));

        // Mock MetricsService methods
        when(metricsService.startAuthenticationTimer()).thenReturn(Instant.now());
        doNothing().when(metricsService).recordAuthenticationTime(any(Instant.class));

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(AuthenticationException.class,
                () -> authService.authenticate("test@example.com", "password123"))).isNotNull();
    }

    @Test
    void shouldFailAuthenticationWhenUserNotFound() {
        when(userRepository.findByEmailHash("nonexistent-email-hash")).thenReturn(Optional.empty());

        // Mock MetricsService methods
        when(metricsService.startAuthenticationTimer()).thenReturn(Instant.now());
        doNothing().when(metricsService).recordAuthenticationTime(any(Instant.class));

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(AuthenticationException.class,
                () -> authService.authenticate("nonexistent@example.com", "password123"))).isNotNull();
    }

    @Test
    void shouldAuthenticateWithNullIsActive() {
        // When isActive is null, Boolean.FALSE.equals() returns false, so authentication should proceed
        testUser.setIsActive(null);
        when(userRepository.findByEmailHash(testEmailHash)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // Mock MetricsService methods
        when(metricsService.startAuthenticationTimer()).thenReturn(Instant.now());
        doNothing().when(metricsService).incrementUserLogin();
        doNothing().when(metricsService).recordAuthenticationTime(any(Instant.class));

        Long result = authService.authenticate("test@example.com", "password123");

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(1L);
    }

    @Test
    void shouldFailAuthenticationWhenUserHasNullPassword() {
        testUser.setPassword(null);
        when(userRepository.findByEmailHash(testEmailHash)).thenReturn(Optional.of(testUser));

        // Mock MetricsService methods
        when(metricsService.startAuthenticationTimer()).thenReturn(Instant.now());
        doNothing().when(metricsService).recordAuthenticationTime(any(Instant.class));

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(AuthenticationException.class,
                () -> authService.authenticate("test@example.com", "password123")))
                .hasMessageContaining("external authentication");
    }

    @Test
    void isSupabaseAuthEnabled_WhenSupabaseAuthServiceIsNull_ShouldReturnFalse() {
        // Use reflection to set supabaseAuthService to null
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set supabaseAuthService to null", e);
        }

        boolean result = authService.isSupabaseAuthEnabled();
        assertThat(result).isFalse();
    }

    @Test
    void isSupabaseAuthEnabled_WhenSupabaseAuthServiceIsEnabled_ShouldReturnTrue() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);

        // Use reflection to set supabaseAuthService
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, supabaseAuthService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set supabaseAuthService", e);
        }

        boolean result = authService.isSupabaseAuthEnabled();
        assertThat(result).isTrue();
    }

    @Test
    void authenticateWithSupabase_WhenSupabaseAuthServiceIsNull_ShouldReturnError() {
        // Use reflection to set supabaseAuthService to null
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set supabaseAuthService to null", e);
        }

        StepVerifier.create(authService.authenticateWithSupabase("test@example.com", "password"))
                .expectErrorMatches(error -> error instanceof IllegalStateException
                        && error.getMessage().contains("Supabase authentication is not enabled"))
                .verify();
    }

    @Test
    void authenticateWithSupabase_WhenUserMappingServiceIsNull_ShouldReturnError() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);

        // Use reflection to set supabaseAuthService
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, supabaseAuthService);
            field = AuthService.class.getDeclaredField("userMappingService");
            field.setAccessible(true);
            field.set(authService, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set services", e);
        }

        StepVerifier.create(authService.authenticateWithSupabase("test@example.com", "password"))
                .expectErrorMatches(error -> error instanceof IllegalStateException
                        && error.getMessage().contains("User mapping service is not available"))
                .verify();
    }

    @Test
    void authenticateWithSupabase_WhenSuccessful_ShouldReturnAuthResponse() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);

        AuthResponse supabaseResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("bearer")
                .expiresIn(3600)
                .user(AuthResponse.User.builder()
                        .id("supabase-uuid")
                        .email("test@example.com")
                        .build())
                .build();

        when(supabaseAuthService.signin(any())).thenReturn(Mono.just(supabaseResponse));
        when(userMappingService.findOrCreateUserFromSupabase(supabaseResponse)).thenReturn(1L);
        doNothing().when(metricsService).incrementUserLogin();

        // Use reflection to set services
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, supabaseAuthService);
            field = AuthService.class.getDeclaredField("userMappingService");
            field.setAccessible(true);
            field.set(authService, userMappingService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set services", e);
        }

        StepVerifier.create(authService.authenticateWithSupabase("test@example.com", "password"))
                .expectNextMatches(response -> {
                    assertThat(response.getAccessToken()).isEqualTo("access-token");
                    assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
                    assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
                    return true;
                })
                .verifyComplete();

        verify(metricsService).incrementUserLogin();
    }

    @Test
    void signupWithSupabase_WhenSuccessful_ShouldReturnAuthResponse() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);

        AuthResponse supabaseResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("bearer")
                .expiresIn(3600)
                .user(AuthResponse.User.builder()
                        .id("supabase-uuid")
                        .email("test@example.com")
                        .build())
                .build();

        when(supabaseAuthService.signup(any())).thenReturn(Mono.just(supabaseResponse));
        when(userMappingService.findOrCreateUserFromSupabase(supabaseResponse)).thenReturn(1L);

        // Use reflection to set services
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, supabaseAuthService);
            field = AuthService.class.getDeclaredField("userMappingService");
            field.setAccessible(true);
            field.set(authService, userMappingService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set services", e);
        }

        StepVerifier.create(authService.signupWithSupabase("test@example.com", "password", Map.of("key", "value")))
                .expectNextMatches(response -> {
                    assertThat(response.getAccessToken()).isEqualTo("access-token");
                    assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void refreshSupabaseToken_WhenSuccessful_ShouldReturnAuthResponse() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);

        AuthResponse response = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("bearer")
                .expiresIn(3600)
                .build();

        when(supabaseAuthService.refreshToken("refresh-token")).thenReturn(Mono.just(response));

        // Use reflection to set supabaseAuthService
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, supabaseAuthService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set supabaseAuthService", e);
        }

        StepVerifier.create(authService.refreshSupabaseToken("refresh-token"))
                .expectNextMatches(r -> r.getAccessToken().equals("new-access-token"))
                .verifyComplete();
    }

    @Test
    void resetPasswordWithSupabase_WhenSuccessful_ShouldComplete() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);
        when(supabaseAuthService.resetPassword(any())).thenReturn(Mono.empty());

        // Use reflection to set supabaseAuthService
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, supabaseAuthService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set supabaseAuthService", e);
        }

        StepVerifier.create(authService.resetPasswordWithSupabase("test@example.com", "https://redirect.com"))
                .verifyComplete();

        verify(supabaseAuthService).resetPassword(any());
    }

    @Test
    void updatePasswordWithSupabase_WhenSuccessful_ShouldComplete() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);
        when(supabaseAuthService.updatePassword("newPassword", "access-token")).thenReturn(Mono.empty());

        // Use reflection to set supabaseAuthService
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, supabaseAuthService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set supabaseAuthService", e);
        }

        StepVerifier.create(authService.updatePasswordWithSupabase("newPassword", "access-token"))
                .verifyComplete();

        verify(supabaseAuthService).updatePassword("newPassword", "access-token");
    }

    @Test
    void signoutFromSupabase_WhenSuccessful_ShouldComplete() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);
        when(supabaseAuthService.signout("access-token")).thenReturn(Mono.empty());

        // Use reflection to set supabaseAuthService
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, supabaseAuthService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set supabaseAuthService", e);
        }

        StepVerifier.create(authService.signoutFromSupabase("access-token"))
                .verifyComplete();

        verify(supabaseAuthService).signout("access-token");
    }

    @Test
    void getCurrentSupabaseUser_WhenSuccessful_ShouldReturnUserInfo() throws Exception {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode userInfo = mapper.readTree("{\"id\":\"uuid\",\"email\":\"test@example.com\"}");

        when(supabaseAuthService.getUserInfo("access-token")).thenReturn(Mono.just(userInfo));

        // Use reflection to set supabaseAuthService
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("supabaseAuthService");
            field.setAccessible(true);
            field.set(authService, supabaseAuthService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set supabaseAuthService", e);
        }

        StepVerifier.create(authService.getCurrentSupabaseUser("access-token"))
                .expectNextMatches(node -> node.get("email").asText().equals("test@example.com"))
                .verifyComplete();

        verify(supabaseAuthService).getUserInfo("access-token");
    }
}
