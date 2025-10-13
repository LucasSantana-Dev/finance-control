package com.finance_control.profile.service;

import com.finance_control.profile.dto.ProfileDTO;
import com.finance_control.profile.dto.ProfileUpdateRequest;
import com.finance_control.profile.model.Profile;
import com.finance_control.profile.repository.ProfileRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.service.BaseService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ProfileService extends BaseService<Profile, Long, ProfileDTO> {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        super(profileRepository);
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
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
        log.debug("Retrieving profile for user ID: {}", currentUserId);

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
        log.debug("Updating profile for user ID: {} with request: {}", currentUserId, request);

        // Validate request
        request.validate();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user email if changed
        if (!user.getEmail().equals(request.getEmail())) {
            // Check if email is already taken by another user
            userRepository.findByEmail(request.getEmail())
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

        log.info("Profile updated successfully for user ID: {}", currentUserId);
        return convertToResponseDTO(savedProfile);
    }

}
