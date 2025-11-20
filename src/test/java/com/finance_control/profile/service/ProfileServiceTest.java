package com.finance_control.profile.service;

import com.finance_control.profile.dto.ProfileDTO;
import com.finance_control.profile.dto.ProfileUpdateRequest;
import com.finance_control.profile.model.Profile;
import com.finance_control.profile.repository.ProfileRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.service.EncryptionService;
import com.finance_control.shared.service.SupabaseStorageService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SupabaseStorageService storageService;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);

        testProfile = new Profile();
        testProfile.setId(1L);
        testProfile.setFullName("John Doe");
        testProfile.setBio("Test bio");
        testProfile.setPhone("+1234567890");
        testProfile.setCountry("US");
        testProfile.setAvatarUrl("https://example.com/avatar.jpg");
        testProfile.setUser(testUser);
        testProfile.setCreatedAt(LocalDateTime.now());
        testProfile.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getCurrentProfile_WithValidUser_ShouldReturnProfile() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

            ProfileDTO result = profileService.getCurrentProfile();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFullName()).isEqualTo("John Doe");
            assertThat(result.getBio()).isEqualTo("Test bio");
            assertThat(result.getPhone()).isEqualTo("+1234567890");
            assertThat(result.getCountry()).isEqualTo("US");
            assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
            verify(profileRepository).findByUserId(1L);
        }
    }

    @Test
    void getCurrentProfile_WhenProfileNotFound_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.getCurrentProfile())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Profile not found");

            verify(profileRepository).findByUserId(1L);
        }
    }

    @Test
    void updateCurrentProfile_WithValidRequest_ShouldUpdateProfile() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "Jane Doe",
                    "test@example.com",
                    "Updated bio",
                    "+9876543210",
                    "BR",
                    "https://example.com/new-avatar.jpg"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ProfileDTO result = profileService.updateCurrentProfile(request);

            assertThat(result).isNotNull();
            assertThat(result.getFullName()).isEqualTo("Jane Doe");
            assertThat(result.getBio()).isEqualTo("Updated bio");
            assertThat(result.getPhone()).isEqualTo("+9876543210");
            assertThat(result.getCountry()).isEqualTo("BR");
            assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/new-avatar.jpg");

            verify(userRepository).findById(1L);
            verify(profileRepository).findByUserId(1L);
            verify(profileRepository).save(any(Profile.class));
        }
    }

    @Test
    void updateCurrentProfile_WhenUserNotFound_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "Jane Doe",
                    "test@example.com",
                    "Updated bio",
                    "+9876543210",
                    "BR",
                    "https://example.com/new-avatar.jpg"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.updateCurrentProfile(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");

            verify(userRepository).findById(1L);
            verify(profileRepository, never()).save(any(Profile.class));
        }
    }

    @Test
    void updateCurrentProfile_WithEmailChange_ShouldUpdateUserEmail() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "John Doe",
                    "newemail@example.com",
                    "Test bio",
                    "+1234567890",
                    "US",
                    "https://example.com/avatar.jpg"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(encryptionService.hashEmail("newemail@example.com")).thenReturn("hashed-newemail");
            when(userRepository.findByEmailHash("hashed-newemail")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ProfileDTO result = profileService.updateCurrentProfile(request);

            assertThat(result).isNotNull();
            verify(encryptionService).hashEmail("newemail@example.com");
            verify(userRepository).findByEmailHash("hashed-newemail");
            verify(userRepository).save(any(User.class));
            assertThat(testUser.getEmail()).isEqualTo("newemail@example.com");
        }
    }

    @Test
    void updateCurrentProfile_WithEmailConflict_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            User otherUser = new User();
            otherUser.setId(2L);
            otherUser.setEmail("existing@example.com");

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "John Doe",
                    "existing@example.com",
                    "Test bio",
                    "+1234567890",
                    "US",
                    "https://example.com/avatar.jpg"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(encryptionService.hashEmail("existing@example.com")).thenReturn("hashed-existing");
            when(userRepository.findByEmailHash("hashed-existing")).thenReturn(Optional.of(otherUser));

            assertThatThrownBy(() -> profileService.updateCurrentProfile(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Email already in use");

            verify(userRepository).findById(1L);
            verify(encryptionService).hashEmail("existing@example.com");
            verify(userRepository).findByEmailHash("hashed-existing");
            verify(userRepository, never()).save(any(User.class));
            verify(profileRepository, never()).save(any(Profile.class));
        }
    }

    @Test
    void updateCurrentProfile_WithSameEmail_ShouldNotUpdateUserEmail() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "John Doe",
                    "test@example.com",
                    "Updated bio",
                    "+1234567890",
                    "US",
                    "https://example.com/avatar.jpg"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ProfileDTO result = profileService.updateCurrentProfile(request);

            assertThat(result).isNotNull();
            verify(encryptionService, never()).hashEmail(anyString());
            verify(userRepository, never()).findByEmailHash(anyString());
            verify(userRepository, never()).save(any(User.class));
            verify(profileRepository).save(any(Profile.class));
        }
    }

    @Test
    void updateCurrentProfile_WhenProfileDoesNotExist_ShouldCreateNewProfile() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "New User",
                    "test@example.com",
                    "New bio",
                    "+1234567890",
                    "US",
                    "https://example.com/avatar.jpg"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                saved.setCreatedAt(LocalDateTime.now());
                saved.setUpdatedAt(LocalDateTime.now());
                return saved;
            });

            ProfileDTO result = profileService.updateCurrentProfile(request);

            assertThat(result).isNotNull();
            assertThat(result.getFullName()).isEqualTo("New User");
            assertThat(result.getBio()).isEqualTo("New bio");
            verify(profileRepository).findByUserId(1L);
            verify(profileRepository).save(any(Profile.class));
        }
    }

    @Test
    void updateCurrentProfile_WithInvalidRequest_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "",
                    "test@example.com",
                    "Test bio",
                    "+1234567890",
                    "US",
                    "https://example.com/avatar.jpg"
            );

            assertThatThrownBy(() -> profileService.updateCurrentProfile(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Full name");

            verify(userRepository, never()).findById(anyLong());
            verify(profileRepository, never()).save(any(Profile.class));
        }
    }

    @Test
    void convertToResponseDTO_ShouldMapEntityCorrectly() {
        ProfileDTO result = profileService.convertToResponseDTO(testProfile);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testProfile.getId());
        assertThat(result.getFullName()).isEqualTo(testProfile.getFullName());
        assertThat(result.getBio()).isEqualTo(testProfile.getBio());
        assertThat(result.getPhone()).isEqualTo(testProfile.getPhone());
        assertThat(result.getCountry()).isEqualTo(testProfile.getCountry());
        assertThat(result.getAvatarUrl()).isEqualTo(testProfile.getAvatarUrl());
        assertThat(result.getCurrency()).isEqualTo(testProfile.getCurrency());
        assertThat(result.getTimezone()).isEqualTo(testProfile.getTimezone());
    }

    @Test
    void getCurrentProfile_WithNullUserContext_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);

            when(profileRepository.findByUserId(null)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.getCurrentProfile())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Profile not found");

            verify(profileRepository).findByUserId(null);
        }
    }

    @Test
    void updateCurrentProfile_WithNullUserContext_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "John Doe",
                    "test@example.com",
                    "Test bio",
                    "+1234567890",
                    "US",
                    "https://example.com/avatar.jpg"
            );

            when(userRepository.findById(null)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.updateCurrentProfile(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");

            verify(userRepository).findById(null);
            verify(profileRepository, never()).save(any(Profile.class));
        }
    }

    @Test
    void updateCurrentProfile_WithEmailChangeToOwnEmail_ShouldNotThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            // User wants to change email to an email that already exists, but it's their own
            // Since the email is the same as the current user's email, the condition
            // !user.getEmail().equals(request.getEmail()) is false, so the email update branch is not taken
            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "John Doe",
                    "test@example.com", // Same email as current user
                    "Test bio",
                    "+1234567890",
                    "US",
                    "https://example.com/avatar.jpg"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // Since email is the same, the email update branch is not taken
            ProfileDTO result = profileService.updateCurrentProfile(request);

            assertThat(result).isNotNull();
            // Email should not be updated since it's the same, so hashEmail and findByEmailHash should not be called
            verify(encryptionService, never()).hashEmail(anyString());
            verify(userRepository, never()).findByEmailHash(anyString());
            verify(userRepository, never()).save(any(User.class));
            verify(profileRepository).save(any(Profile.class));
        }
    }

    @Test
    void updateCurrentProfile_WithEmailChangeToNewEmailNotTaken_ShouldUpdateEmail() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "John Doe",
                    "newemail@example.com",
                    "Test bio",
                    "+1234567890",
                    "US",
                    "https://example.com/avatar.jpg"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(encryptionService.hashEmail("newemail@example.com")).thenReturn("hashed-newemail");
            when(userRepository.findByEmailHash("hashed-newemail")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ProfileDTO result = profileService.updateCurrentProfile(request);

            assertThat(result).isNotNull();
            verify(encryptionService).hashEmail("newemail@example.com");
            verify(userRepository).findByEmailHash("hashed-newemail");
            verify(userRepository).save(any(User.class));
            verify(profileRepository).save(any(Profile.class));
        }
    }

    @Test
    void uploadAvatar_WithValidFile_ShouldUploadAndUpdateProfile() throws IOException {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            MultipartFile avatarFile = mock(MultipartFile.class);
            when(avatarFile.isEmpty()).thenReturn(false);
            when(avatarFile.getContentType()).thenReturn("image/jpeg");
            when(avatarFile.getSize()).thenReturn(1024L);
            lenient().when(avatarFile.getBytes()).thenReturn(new byte[1024]);

            String newAvatarUrl = "https://project.supabase.co/storage/v1/object/public/avatars/user-1/avatar.jpg";
            when(storageService.uploadAvatar(1L, avatarFile)).thenReturn(newAvatarUrl);
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // Use reflection to set storageService
            try {
                java.lang.reflect.Field field = ProfileService.class.getDeclaredField("storageService");
                field.setAccessible(true);
                field.set(profileService, storageService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set storageService", e);
            }

            ProfileDTO result = profileService.uploadAvatar(avatarFile);

            assertThat(result).isNotNull();
            assertThat(result.getAvatarUrl()).isEqualTo(newAvatarUrl);
            verify(storageService).uploadAvatar(1L, avatarFile);
            verify(profileRepository).save(any(Profile.class));
        }
    }

    @Test
    void uploadAvatar_WhenUserContextIsNull_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);

            MultipartFile avatarFile = mock(MultipartFile.class);

            assertThatThrownBy(() -> profileService.uploadAvatar(avatarFile))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No authenticated user found");
        }
    }

    @Test
    void uploadAvatar_WhenStorageServiceIsNull_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            // Use reflection to set storageService to null
            try {
                java.lang.reflect.Field field = ProfileService.class.getDeclaredField("storageService");
                field.setAccessible(true);
                field.set(profileService, null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set storageService to null", e);
            }

            MultipartFile avatarFile = mock(MultipartFile.class);

            assertThatThrownBy(() -> profileService.uploadAvatar(avatarFile))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Supabase Storage service is not available");
        }
    }

    @Test
    void uploadAvatar_WithNullFile_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            // Use reflection to set storageService
            try {
                java.lang.reflect.Field field = ProfileService.class.getDeclaredField("storageService");
                field.setAccessible(true);
                field.set(profileService, storageService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set storageService", e);
            }

            assertThatThrownBy(() -> profileService.uploadAvatar(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Avatar file cannot be empty");
        }
    }

    @Test
    void uploadAvatar_WithEmptyFile_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            MultipartFile avatarFile = mock(MultipartFile.class);
            when(avatarFile.isEmpty()).thenReturn(true);

            // Use reflection to set storageService
            try {
                java.lang.reflect.Field field = ProfileService.class.getDeclaredField("storageService");
                field.setAccessible(true);
                field.set(profileService, storageService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set storageService", e);
            }

            assertThatThrownBy(() -> profileService.uploadAvatar(avatarFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Avatar file cannot be empty");
        }
    }

    @Test
    void uploadAvatar_WithInvalidContentType_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            MultipartFile avatarFile = mock(MultipartFile.class);
            when(avatarFile.isEmpty()).thenReturn(false);
            when(avatarFile.getContentType()).thenReturn("application/pdf");

            // Use reflection to set storageService
            try {
                java.lang.reflect.Field field = ProfileService.class.getDeclaredField("storageService");
                field.setAccessible(true);
                field.set(profileService, storageService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set storageService", e);
            }

            assertThatThrownBy(() -> profileService.uploadAvatar(avatarFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid file type");
        }
    }

    @Test
    void uploadAvatar_WithFileTooLarge_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            MultipartFile avatarFile = mock(MultipartFile.class);
            when(avatarFile.isEmpty()).thenReturn(false);
            when(avatarFile.getContentType()).thenReturn("image/jpeg");
            when(avatarFile.getSize()).thenReturn(6 * 1024 * 1024L); // 6MB

            // Use reflection to set storageService
            try {
                java.lang.reflect.Field field = ProfileService.class.getDeclaredField("storageService");
                field.setAccessible(true);
                field.set(profileService, storageService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set storageService", e);
            }

            assertThatThrownBy(() -> profileService.uploadAvatar(avatarFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File size exceeds maximum");
        }
    }

    @Test
    void uploadAvatar_WhenProfileNotFound_ShouldThrowException() throws IOException {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            MultipartFile avatarFile = mock(MultipartFile.class);
            when(avatarFile.isEmpty()).thenReturn(false);
            when(avatarFile.getContentType()).thenReturn("image/jpeg");
            when(avatarFile.getSize()).thenReturn(1024L);

            String newAvatarUrl = "https://project.supabase.co/storage/v1/object/public/avatars/user-1/avatar.jpg";
            when(storageService.uploadAvatar(1L, avatarFile)).thenReturn(newAvatarUrl);
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

            // Use reflection to set storageService
            try {
                java.lang.reflect.Field field = ProfileService.class.getDeclaredField("storageService");
                field.setAccessible(true);
                field.set(profileService, storageService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set storageService", e);
            }

            assertThatThrownBy(() -> profileService.uploadAvatar(avatarFile))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Profile not found");
        }
    }

    @Test
    void uploadAvatar_WithOldSupabaseAvatar_ShouldDeleteOldAvatar() throws IOException {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            testProfile.setAvatarUrl("https://project.supabase.co/storage/v1/object/public/avatars/user-1/old-avatar.jpg");

            MultipartFile avatarFile = mock(MultipartFile.class);
            when(avatarFile.isEmpty()).thenReturn(false);
            when(avatarFile.getContentType()).thenReturn("image/png");
            when(avatarFile.getSize()).thenReturn(1024L);
            lenient().when(avatarFile.getBytes()).thenReturn(new byte[1024]);

            String newAvatarUrl = "https://project.supabase.co/storage/v1/object/public/avatars/user-1/new-avatar.png";
            when(storageService.uploadAvatar(1L, avatarFile)).thenReturn(newAvatarUrl);
            lenient().when(storageService.deleteFile("avatars", "user-1/old-avatar.jpg")).thenReturn(true);
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // Use reflection to set storageService
            try {
                java.lang.reflect.Field field = ProfileService.class.getDeclaredField("storageService");
                field.setAccessible(true);
                field.set(profileService, storageService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set storageService", e);
            }

            ProfileDTO result = profileService.uploadAvatar(avatarFile);

            assertThat(result).isNotNull();
            assertThat(result.getAvatarUrl()).isEqualTo(newAvatarUrl);
            verify(storageService).deleteFile("avatars", "user-1/old-avatar.jpg");
        }
    }

    @Test
    void uploadAvatar_WithNonSupabaseAvatar_ShouldNotDeleteOldAvatar() throws IOException {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            testProfile.setAvatarUrl("https://example.com/avatar.jpg");

            MultipartFile avatarFile = mock(MultipartFile.class);
            when(avatarFile.isEmpty()).thenReturn(false);
            when(avatarFile.getContentType()).thenReturn("image/gif");
            when(avatarFile.getSize()).thenReturn(1024L);
            lenient().when(avatarFile.getBytes()).thenReturn(new byte[1024]);

            String newAvatarUrl = "https://project.supabase.co/storage/v1/object/public/avatars/user-1/new-avatar.gif";
            when(storageService.uploadAvatar(1L, avatarFile)).thenReturn(newAvatarUrl);
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // Use reflection to set storageService
            try {
                java.lang.reflect.Field field = ProfileService.class.getDeclaredField("storageService");
                field.setAccessible(true);
                field.set(profileService, storageService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set storageService", e);
            }

            ProfileDTO result = profileService.uploadAvatar(avatarFile);

            assertThat(result).isNotNull();
            verify(storageService, never()).deleteFile(anyString(), anyString());
        }
    }
}
