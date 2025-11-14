package com.finance_control.unit.shared.service;

import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.service.UserMappingService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserMappingService Tests")
class UserMappingServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserMappingService userMappingService;

    private AuthResponse.User supabaseUser;
    private AuthResponse authResponse;
    private User existingUser;

    @BeforeEach
    void setUp() {
        supabaseUser = new AuthResponse.User();
        supabaseUser.setId("supabase-uuid-123");
        supabaseUser.setEmail("test@example.com");
        supabaseUser.setEmailConfirmedAt("2024-01-01T00:00:00Z");

        authResponse = new AuthResponse();
        authResponse.setUser(supabaseUser);

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setIsActive(true);
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with existing user by email should return existing user ID")
    void findOrCreateUserFromSupabase_WithExistingUserByEmail_ShouldReturnExistingUserId() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        Long userId = userMappingService.findOrCreateUserFromSupabase(authResponse);

        assertThat(userId).isEqualTo(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with new user should create and return new user ID")
    void findOrCreateUserFromSupabase_WithNewUser_ShouldCreateAndReturnNewUserId() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("test@example.com");
        newUser.setIsActive(true);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        Long userId = userMappingService.findOrCreateUserFromSupabase(authResponse);

        assertThat(userId).isEqualTo(2L);
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with null response should throw")
    void findOrCreateUserFromSupabase_WithNullResponse_ShouldThrow() {
        assertThatThrownBy(() -> userMappingService.findOrCreateUserFromSupabase(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supabase response or user cannot be null");

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with null user should throw")
    void findOrCreateUserFromSupabase_WithNullUser_ShouldThrow() {
        AuthResponse nullUserResponse = new AuthResponse();
        nullUserResponse.setUser(null);

        assertThatThrownBy(() -> userMappingService.findOrCreateUserFromSupabase(nullUserResponse))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supabase response or user cannot be null");

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with null email should throw")
    void findOrCreateUserFromSupabase_WithNullEmail_ShouldThrow() {
        supabaseUser.setEmail(null);

        assertThatThrownBy(() -> userMappingService.findOrCreateUserFromSupabase(authResponse))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supabase user ID and email are required");

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with null Supabase user ID should throw")
    void findOrCreateUserFromSupabase_WithNullSupabaseUserId_ShouldThrow() {
        supabaseUser.setId(null);

        assertThatThrownBy(() -> userMappingService.findOrCreateUserFromSupabase(authResponse))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supabase user ID and email are required");

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with existing user should update email if changed")
    void findOrCreateUserFromSupabase_WithExistingUser_ShouldUpdateEmailIfChanged() {
        existingUser.setEmail("old@example.com");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        Long userId = userMappingService.findOrCreateUserFromSupabase(authResponse);

        assertThat(userId).isEqualTo(1L);
        assertThat(existingUser.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("findBySupabaseUserId - should return empty (not implemented yet)")
    void findBySupabaseUserId_ShouldReturnEmpty() {
        Optional<User> result = userMappingService.findBySupabaseUserId("supabase-uuid-123");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getSupabaseUserId - should return empty (not implemented yet)")
    void getSupabaseUserId_ShouldReturnEmpty() {
        Optional<String> result = userMappingService.getSupabaseUserId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("isUserLinkedToSupabase - should return false (not implemented yet)")
    void isUserLinkedToSupabase_ShouldReturnFalse() {
        boolean result = userMappingService.isUserLinkedToSupabase(1L);

        assertThat(result).isFalse();
    }
}
