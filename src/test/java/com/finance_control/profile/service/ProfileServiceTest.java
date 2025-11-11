package com.finance_control.profile.service;

import com.finance_control.profile.dto.ProfileDTO;
import com.finance_control.profile.dto.ProfileUpdateRequest;
import com.finance_control.profile.model.Profile;
import com.finance_control.profile.repository.ProfileRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

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
            when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ProfileDTO result = profileService.updateCurrentProfile(request);

            assertThat(result).isNotNull();
            verify(userRepository).findByEmail("newemail@example.com");
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
            when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(otherUser));

            assertThatThrownBy(() -> profileService.updateCurrentProfile(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Email already in use");

            verify(userRepository).findById(1L);
            verify(userRepository).findByEmail("existing@example.com");
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
            verify(userRepository, never()).findByEmail(anyString());
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
            // Email should not be updated since it's the same, so findByEmail and save should not be called
            verify(userRepository, never()).findByEmail(anyString());
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
            when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
                Profile saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ProfileDTO result = profileService.updateCurrentProfile(request);

            assertThat(result).isNotNull();
            verify(userRepository).findByEmail("newemail@example.com");
            verify(userRepository).save(any(User.class));
            verify(profileRepository).save(any(Profile.class));
        }
    }
}
