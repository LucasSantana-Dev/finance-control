package com.finance_control.unit.shared.service;

import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.shared.service.UserMappingService;
import com.finance_control.shared.service.EncryptionService;
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
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserMappingService Tests")
class UserMappingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private UserMappingService userMappingService;

    private AuthResponse.User supabaseUser;
    private AuthResponse authResponse;
    private User existingUser;
    private String testEmailHash;

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

        // Setup email hash for testing
        testEmailHash = "test-email-hash-12345";
        // Use lenient for the specific email first, then fallback
        lenient().when(encryptionService.hashEmail("test@example.com")).thenReturn(testEmailHash);
        lenient().when(encryptionService.hashEmail("old@example.com")).thenReturn("old-email-hash");
        lenient().when(encryptionService.hashEmail(any(String.class))).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            if (email == null) return null;
            if ("test@example.com".equals(email)) return testEmailHash;
            if ("old@example.com".equals(email)) return "old-email-hash";
            return "hash-" + email;
        });
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with existing user by email should return existing user ID")
    void findOrCreateUserFromSupabase_WithExistingUserByEmail_ShouldReturnExistingUserId() {
        // First try by Supabase ID (not found)
        when(userRepository.findBySupabaseUserId("supabase-uuid-123"))
                .thenReturn(Optional.empty());
        // Then try by email hash (found)
        when(userRepository.findByEmailHash(testEmailHash))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        Long userId = userMappingService.findOrCreateUserFromSupabase(authResponse);

        assertThat(userId).isEqualTo(1L);
        verify(userRepository).findBySupabaseUserId("supabase-uuid-123");
        verify(userRepository).findByEmailHash(testEmailHash);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with new user should create and return new user ID")
    void findOrCreateUserFromSupabase_WithNewUser_ShouldCreateAndReturnNewUserId() {
        // First try by Supabase ID (not found)
        when(userRepository.findBySupabaseUserId("supabase-uuid-123"))
                .thenReturn(Optional.empty());
        // Then try by email hash (not found)
        when(userRepository.findByEmailHash(testEmailHash))
                .thenReturn(Optional.empty());

        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("test@example.com");
        newUser.setSupabaseUserId("supabase-uuid-123");
        newUser.setEmailHash(testEmailHash);
        newUser.setIsActive(true);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        Long userId = userMappingService.findOrCreateUserFromSupabase(authResponse);

        assertThat(userId).isEqualTo(2L);
        verify(userRepository).findBySupabaseUserId("supabase-uuid-123");
        verify(userRepository).findByEmailHash(testEmailHash);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with null response should throw")
    void findOrCreateUserFromSupabase_WithNullResponse_ShouldThrow() {
        assertThatThrownBy(() -> userMappingService.findOrCreateUserFromSupabase(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supabase response or user cannot be null");

        verify(userRepository, never()).findBySupabaseUserId(any());
        verify(userRepository, never()).findByEmailHash(any());
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

        verify(userRepository, never()).findBySupabaseUserId(any());
        verify(userRepository, never()).findByEmailHash(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with null email should throw")
    void findOrCreateUserFromSupabase_WithNullEmail_ShouldThrow() {
        supabaseUser.setEmail(null);

        assertThatThrownBy(() -> userMappingService.findOrCreateUserFromSupabase(authResponse))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supabase user ID and email are required");

        verify(userRepository, never()).findBySupabaseUserId(any());
        verify(userRepository, never()).findByEmailHash(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with null Supabase user ID should throw")
    void findOrCreateUserFromSupabase_WithNullSupabaseUserId_ShouldThrow() {
        supabaseUser.setId(null);

        assertThatThrownBy(() -> userMappingService.findOrCreateUserFromSupabase(authResponse))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Supabase user ID and email are required");

        verify(userRepository, never()).findBySupabaseUserId(any());
        verify(userRepository, never()).findByEmailHash(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateUserFromSupabase - with existing user should update email if changed")
    void findOrCreateUserFromSupabase_WithExistingUser_ShouldUpdateEmailIfChanged() {
        existingUser.setEmail("old@example.com");
        // First try by Supabase ID (not found)
        when(userRepository.findBySupabaseUserId("supabase-uuid-123"))
                .thenReturn(Optional.empty());
        // Then try by email hash (found)
        when(userRepository.findByEmailHash(testEmailHash))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        Long userId = userMappingService.findOrCreateUserFromSupabase(authResponse);

        assertThat(userId).isEqualTo(1L);
        assertThat(existingUser.getEmail()).isEqualTo("test@example.com");
        assertThat(existingUser.getSupabaseUserId()).isEqualTo("supabase-uuid-123");
        verify(userRepository).findBySupabaseUserId("supabase-uuid-123");
        verify(userRepository).findByEmailHash(testEmailHash);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("findBySupabaseUserId - should return user when found")
    void findBySupabaseUserId_ShouldReturnUserWhenFound() {
        when(userRepository.findBySupabaseUserId("supabase-uuid-123"))
                .thenReturn(Optional.of(existingUser));

        Optional<User> result = userMappingService.findBySupabaseUserId("supabase-uuid-123");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findBySupabaseUserId - should return empty when not found")
    void findBySupabaseUserId_ShouldReturnEmptyWhenNotFound() {
        when(userRepository.findBySupabaseUserId("nonexistent-uuid"))
                .thenReturn(Optional.empty());

        Optional<User> result = userMappingService.findBySupabaseUserId("nonexistent-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getSupabaseUserId - should return Supabase user ID when found")
    void getSupabaseUserId_ShouldReturnSupabaseUserIdWhenFound() {
        existingUser.setSupabaseUserId("supabase-uuid-123");
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        Optional<String> result = userMappingService.getSupabaseUserId(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("supabase-uuid-123");
    }

    @Test
    @DisplayName("getSupabaseUserId - should return empty when user not found")
    void getSupabaseUserId_ShouldReturnEmptyWhenUserNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<String> result = userMappingService.getSupabaseUserId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getSupabaseUserId - should return empty when user has no Supabase ID")
    void getSupabaseUserId_ShouldReturnEmptyWhenNoSupabaseId() {
        existingUser.setSupabaseUserId(null);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        Optional<String> result = userMappingService.getSupabaseUserId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("isUserLinkedToSupabase - should return true when user has Supabase ID")
    void isUserLinkedToSupabase_ShouldReturnTrueWhenLinked() {
        existingUser.setSupabaseUserId("supabase-uuid-123");
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        boolean result = userMappingService.isUserLinkedToSupabase(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isUserLinkedToSupabase - should return false when user has no Supabase ID")
    void isUserLinkedToSupabase_ShouldReturnFalseWhenNotLinked() {
        existingUser.setSupabaseUserId(null);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        boolean result = userMappingService.isUserLinkedToSupabase(1L);

        assertThat(result).isFalse();
    }
}
