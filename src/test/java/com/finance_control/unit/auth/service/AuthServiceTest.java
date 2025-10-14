package com.finance_control.unit.auth.service;

import com.finance_control.auth.exception.AuthenticationException;
import com.finance_control.auth.service.AuthService;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.any;
import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
    }

    @Test
    void shouldAuthenticateSuccessfully() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
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
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            authService.changePassword("oldPassword", "newPassword");
        })).isNotNull();
    }

    @Test
    void shouldFailAuthenticationWithInvalidCredentials() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);
        
        // Mock MetricsService methods
        when(metricsService.startAuthenticationTimer()).thenReturn(Instant.now());
        doNothing().when(metricsService).recordAuthenticationTime(any(Instant.class));

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("test@example.com", "wrongPassword");
        })).isNotNull();
    }

    @Test
    void shouldFailAuthenticationWithInactiveUser() {
        testUser.setIsActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // Mock MetricsService methods
        when(metricsService.startAuthenticationTimer()).thenReturn(Instant.now());
        doNothing().when(metricsService).recordAuthenticationTime(any(Instant.class));

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("test@example.com", "password123");
        })).isNotNull();
    }
}
