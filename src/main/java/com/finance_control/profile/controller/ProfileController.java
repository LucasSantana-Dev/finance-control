package com.finance_control.profile.controller;

import com.finance_control.profile.dto.ProfileDTO;
import com.finance_control.profile.dto.ProfileUpdateRequest;
import com.finance_control.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@Slf4j
@Tag(name = "User Profile", description = "User profile management endpoints")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    @Operation(summary = "Get current user profile", description = "Retrieve the current authenticated user's profile")
    public ResponseEntity<ProfileDTO> getCurrentProfile() {
        log.debug("GET request to retrieve current user profile");

        ProfileDTO profile = profileService.getCurrentProfile();
        log.info("Profile retrieved successfully");
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    @Operation(summary = "Update current user profile", description = "Update the current authenticated user's profile")
    public ResponseEntity<ProfileDTO> updateCurrentProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        log.debug("PUT request to update current user profile (request present: {})", request != null);

        ProfileDTO updatedProfile = profileService.updateCurrentProfile(request);
        log.info("Profile updated successfully");
        return ResponseEntity.ok(updatedProfile);
    }
}
