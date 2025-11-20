package com.finance_control.profile.service;

import com.finance_control.profile.dto.ProfileDTO;
import com.finance_control.profile.dto.ProfileUpdateRequest;
import com.finance_control.profile.model.Profile;
import com.finance_control.profile.repository.ProfileRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.service.BaseService;
import com.finance_control.shared.service.EncryptionService;
import com.finance_control.shared.service.SupabaseStorageService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ProfileService extends BaseService<Profile, Long, ProfileDTO> {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    @Autowired(required = false)
    private SupabaseStorageService storageService;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository, EncryptionService encryptionService) {
        super(profileRepository);
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    protected boolean isUserAware() {
        return true;
    }

    @Override
    protected Profile mapToEntity(ProfileDTO dto) {
        Profile profile = new Profile();
        profile.setFullName(dto.getFullName());
        profile.setBio(dto.getBio());
        profile.setPhone(dto.getPhone());
        profile.setCountry(dto.getCountry());
        profile.setAvatarUrl(dto.getAvatarUrl());
        return profile;
    }

    @Override
    protected void updateEntityFromDTO(Profile entity, ProfileDTO dto) {
        entity.setFullName(dto.getFullName());
        entity.setBio(dto.getBio());
        entity.setPhone(dto.getPhone());
        entity.setCountry(dto.getCountry());
        entity.setAvatarUrl(dto.getAvatarUrl());
    }

    @Override
    protected ProfileDTO mapToResponseDTO(Profile entity) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setFullName(entity.getFullName());
        dto.setBio(entity.getBio());
        dto.setPhone(entity.getPhone());
        dto.setCountry(entity.getCountry());
        dto.setAvatarUrl(entity.getAvatarUrl());
        dto.setCurrency(entity.getCurrency());
        dto.setTimezone(entity.getTimezone());
        return dto;
    }

    public ProfileDTO convertToResponseDTO(Profile entity) {
        return mapToResponseDTO(entity);
    }

    /**
     * Retrieves the current authenticated user's profile.
     *
     * @return the profile DTO with all information
     * @throws RuntimeException if user is not found
     */
    @Transactional(readOnly = true)
    public ProfileDTO getCurrentProfile() {
        Long currentUserId = UserContext.getCurrentUserId();
        log.debug("Retrieving profile (user present: {})", currentUserId != null);

        Profile profile = profileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return convertToResponseDTO(profile);
    }

    /**
     * Updates the current authenticated user's profile.
     *
     * @param request the profile update request
     * @return the updated profile DTO
     * @throws RuntimeException if user is not found or update fails
     */
    @Transactional
    public ProfileDTO updateCurrentProfile(ProfileUpdateRequest request) {
        Long currentUserId = UserContext.getCurrentUserId();
        log.debug("Updating profile (user present: {}) with request: [REDACTED]", currentUserId != null);

        // Validate request
        request.validate();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user email if changed
        if (!user.getEmail().equals(request.getEmail())) {
            // Check if email is already taken by another user
            String emailHash = encryptionService.hashEmail(request.getEmail());
            userRepository.findByEmailHash(emailHash)
                    .filter(existingUser -> !existingUser.getId().equals(currentUserId))
                    .ifPresent(existingUser -> {
                        throw new RuntimeException("Email already in use");
                    });
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }

        // Update or create user profile
        Profile profile = profileRepository.findByUserId(currentUserId).orElse(new Profile());
        profile.setUser(user);
        profile.setFullName(request.getFullName());
        profile.setBio(request.getBio());
        profile.setPhone(request.getPhone());
        profile.setCountry(request.getCountry());
        profile.setAvatarUrl(request.getAvatarUrl());

        // Save profile
        Profile savedProfile = profileRepository.save(profile);

        log.info("Profile updated successfully (user present: {})", currentUserId != null);
        return convertToResponseDTO(savedProfile);
    }

    /**
     * Uploads and updates the user's avatar image.
     *
     * @param avatarFile the avatar image file
     * @return the updated profile with new avatar URL
     * @throws IllegalStateException if Supabase Storage is not configured
     * @throws RuntimeException if upload fails
     */
    @Transactional
    public ProfileDTO uploadAvatar(MultipartFile avatarFile) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        // Check if Supabase Storage is available
        if (storageService == null) {
            throw new IllegalStateException("Supabase Storage service is not available. Avatar upload is disabled.");
        }

        // Validate file
        validateAvatarFile(avatarFile);

        try {
            // Upload avatar to Supabase Storage
            String avatarUrl = storageService.uploadAvatar(currentUserId, avatarFile);

            // Update profile with new avatar URL
            Profile profile = profileRepository.findByUserId(currentUserId).orElse(null);
            if (profile == null) {
                throw new IllegalStateException("Profile not found for user: " + currentUserId);
            }

            // Delete old avatar if it exists and is a Supabase URL
            if (profile.getAvatarUrl() != null && isSupabaseUrl(profile.getAvatarUrl())) {
                try {
                    deleteOldAvatar(profile.getAvatarUrl());
                } catch (Exception e) {
                    log.warn("Failed to delete old avatar, continuing with upload: {}", e.getMessage());
                }
            }

            profile.setAvatarUrl(avatarUrl);
            Profile updatedProfile = profileRepository.save(profile);

            log.info("Avatar updated successfully for user {}: {}", currentUserId, avatarUrl);

            return convertToResponseDTO(updatedProfile);

        } catch (Exception e) {
            log.error("Failed to upload avatar for user {}", currentUserId, e);
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage(), e);
        }
    }

    /**
     * Validates avatar file constraints.
     *
     * @param file the avatar file to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, GIF, and WebP images are allowed.");
        }

        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }
    }

    /**
     * Checks if the content type is a valid image type.
     *
     * @param contentType the content type to check
     * @return true if valid image type
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }

    /**
     * Checks if the URL is a Supabase Storage URL.
     *
     * @param url the URL to check
     * @return true if it's a Supabase URL
     */
    private boolean isSupabaseUrl(String url) {
        return url != null && url.contains("supabase.co/storage/v1/object/public");
    }

    /**
     * Attempts to delete the old avatar file from Supabase Storage.
     *
     * @param avatarUrl the old avatar URL
     */
    private void deleteOldAvatar(String avatarUrl) {
        try {
            // Extract bucket and file name from Supabase URL
            // URL format: https://project.supabase.co/storage/v1/object/public/bucket/file
            String[] parts = avatarUrl.split("/storage/v1/object/public/");
            if (parts.length == 2) {
                String[] pathParts = parts[1].split("/");
                if (pathParts.length >= 2) {
                    String bucket = pathParts[0];
                    String fileName = String.join("/", java.util.Arrays.copyOfRange(pathParts, 1, pathParts.length));

                    boolean deleted = storageService.deleteFile(bucket, fileName);
                    if (deleted) {
                        log.info("Old avatar deleted successfully: {}", fileName);
                    } else {
                        log.warn("Failed to delete old avatar: {}", fileName);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error while trying to delete old avatar: {}", e.getMessage());
        }
    }

}
