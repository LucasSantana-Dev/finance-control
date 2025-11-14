package com.finance_control.unit.shared.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.shared.service.SupabaseAuthService;
import com.finance_control.shared.service.UserSynchronizationService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSynchronizationService Tests")
class UserSynchronizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SupabaseAuthService supabaseAuthService;

    @InjectMocks
    private UserSynchronizationService userSynchronizationService;

    private ObjectMapper objectMapper;
    private User testUser;
    private JsonNode supabaseUserData;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);

        String jsonData = """
                {
                    "email": "updated@example.com",
                    "email_confirmed_at": "2024-01-01T00:00:00Z",
                    "user_metadata": {
                        "name": "Test User",
                        "avatar_url": "https://example.com/avatar.jpg"
                    }
                }
                """;
        supabaseUserData = objectMapper.readTree(jsonData);
    }

    @Test
    @DisplayName("syncUserFromSupabase - with Supabase auth disabled should skip sync")
    void syncUserFromSupabase_WithSupabaseAuthDisabled_ShouldSkipSync() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(false);

        userSynchronizationService.syncUserFromSupabase(1L, "token");

        verify(supabaseAuthService, never()).getUserInfo(anyString());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("syncUserFromSupabase - with null SupabaseAuthService should skip sync")
    void syncUserFromSupabase_WithNullSupabaseAuthService_ShouldSkipSync() {
        UserSynchronizationService service = new UserSynchronizationService(
                userRepository, null);

        service.syncUserFromSupabase(1L, "token");

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("syncUserFromSupabase - with valid data should update user")
    void syncUserFromSupabase_WithValidData_ShouldUpdateUser() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);
        when(supabaseAuthService.getUserInfo("token"))
                .thenReturn(Mono.just(supabaseUserData));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userSynchronizationService.syncUserFromSupabase(1L, "token");

        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        assertThat(testUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("syncUserFromSupabase - with user not found should log warning")
    void syncUserFromSupabase_WithUserNotFound_ShouldLogWarning() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);
        when(supabaseAuthService.getUserInfo("token"))
                .thenReturn(Mono.just(supabaseUserData));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        userSynchronizationService.syncUserFromSupabase(1L, "token");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("syncUserFromSupabase - with no Supabase user info should log warning")
    void syncUserFromSupabase_WithNoSupabaseUserInfo_ShouldLogWarning() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);
        when(supabaseAuthService.getUserInfo("token"))
                .thenReturn(Mono.empty());

        userSynchronizationService.syncUserFromSupabase(1L, "token");

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("syncUserFromSupabase - with same email and no metadata should not update")
    void syncUserFromSupabase_WithSameEmailAndNoMetadata_ShouldNotUpdate() throws Exception {
        String jsonData = """
                {
                    "email": "test@example.com"
                }
                """;
        JsonNode sameEmailData = objectMapper.readTree(jsonData);

        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);
        when(supabaseAuthService.getUserInfo("token"))
                .thenReturn(Mono.just(sameEmailData));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userSynchronizationService.syncUserFromSupabase(1L, "token");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("syncUserFromSupabase - with same email but metadata should update")
    void syncUserFromSupabase_WithSameEmailButMetadata_ShouldUpdate() throws Exception {
        String jsonData = """
                {
                    "email": "test@example.com",
                    "user_metadata": {
                        "name": "Test User"
                    }
                }
                """;
        JsonNode sameEmailData = objectMapper.readTree(jsonData);

        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);
        when(supabaseAuthService.getUserInfo("token"))
                .thenReturn(Mono.just(sameEmailData));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userSynchronizationService.syncUserFromSupabase(1L, "token");

        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("syncUserToSupabase - with Supabase auth disabled should skip sync")
    void syncUserToSupabase_WithSupabaseAuthDisabled_ShouldSkipSync() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(false);

        userSynchronizationService.syncUserToSupabase(1L, "token");

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("syncUserToSupabase - with user not found should log warning")
    void syncUserToSupabase_WithUserNotFound_ShouldLogWarning() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        userSynchronizationService.syncUserToSupabase(1L, "token");

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("syncUserToSupabase - with valid user should prepare sync")
    void syncUserToSupabase_WithValidUser_ShouldPrepareSync() {
        when(supabaseAuthService.isSupabaseAuthEnabled()).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userSynchronizationService.syncUserToSupabase(1L, "token");

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("isUserInSync - should return true (placeholder implementation)")
    void isUserInSync_ShouldReturnTrue() {
        boolean result = userSynchronizationService.isUserInSync(1L, "token");

        assertThat(result).isTrue();
    }
}
