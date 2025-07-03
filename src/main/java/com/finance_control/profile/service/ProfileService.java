package com.finance_control.profile.service;

import com.finance_control.profile.dto.ProfileDTO;
import com.finance_control.profile.dto.ProfileUpdateRequest;
import com.finance_control.profile.model.Profile;
import com.finance_control.profile.repository.ProfileRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    
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
        
        return mapToResponseDTO(profile);
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
        return mapToResponseDTO(savedProfile);
    }
    
    /**
     * Maps a Profile entity to a ProfileDTO for response.
     * 
     * @param profile the profile entity
     * @return the profile DTO
     */
    private ProfileDTO mapToResponseDTO(Profile profile) {
        ProfileDTO dto = new ProfileDTO();
        
        // Map common fields
        dto.setId(profile.getId());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        
        // Map profile fields
        dto.setFullName(profile.getFullName());
        dto.setBio(profile.getBio());
        dto.setPhone(profile.getPhone());
        dto.setCountry(profile.getCountry());
        dto.setAvatarUrl(profile.getAvatarUrl());
        
        // Map computed fields
        dto.setCurrency(profile.getCurrency());
        dto.setTimezone(profile.getTimezone());
        
        return dto;
    }
} 