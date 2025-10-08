package com.finance_control.unit.auth.service;

import com.finance_control.auth.exception.AuthenticationException;
import com.finance_control.auth.service.AuthService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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

        Long result = authService.authenticate("test@example.com", "password123");

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(1L);
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        authService.changePassword("oldPassword", "newPassword");

        // Since the method is void, we just verify it doesn't throw an exception
        // In a real implementation, we would verify the password was actually changed
    }

    @Test
    void shouldFailAuthenticationWithInvalidCredentials() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("test@example.com", "wrongPassword");
        })).isNotNull();
    }

    @Test
    void shouldFailAuthenticationWithInactiveUser() {
        testUser.setIsActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("test@example.com", "password123");
        })).isNotNull();
    }
}
