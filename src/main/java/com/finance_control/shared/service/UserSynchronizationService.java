package com.finance_control.shared.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for synchronizing user data between Supabase and the local application.
 * Ensures user profiles stay consistent across both systems.
 */
@Slf4j
@Service
@ConditionalOnBean(SupabaseAuthService.class)
@RequiredArgsConstructor
public class UserSynchronizationService {

    private final UserRepository userRepository;
    private final SupabaseAuthService supabaseAuthService;

    /**
     * Synchronizes user data from Supabase to the local application.
     * This should be called when user data might have changed in Supabase.
     *
     * @param localUserId The local user ID
     * @param accessToken The Supabase access token for API calls
     */
    @Transactional
    public void syncUserFromSupabase(Long localUserId, String accessToken) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            log.debug("Supabase auth not enabled, skipping sync for user: {}", localUserId);
            return;
        }

        try {
            // Get user info from Supabase
            Optional<JsonNode> supabaseUserInfo = supabaseAuthService.getUserInfo(accessToken).blockOptional();

            if (supabaseUserInfo.isPresent()) {
                JsonNode userData = supabaseUserInfo.get();
                updateLocalUserFromSupabase(localUserId, userData);
                log.info("Successfully synced user data from Supabase for user: {}", localUserId);
            } else {
                log.warn("Could not retrieve user info from Supabase for user: {}", localUserId);
            }

        } catch (Exception e) {
            log.error("Error syncing user data from Supabase for user {}: {}", localUserId, e.getMessage(), e);
        }
    }

    /**
     * Updates local user data based on Supabase user information.
     *
     * @param localUserId The local user ID
     * @param supabaseUserData The user data from Supabase
     */
    private void updateLocalUserFromSupabase(Long localUserId, JsonNode supabaseUserData) {
        Optional<User> userOpt = userRepository.findById(localUserId);
        if (userOpt.isEmpty()) {
            log.warn("Local user not found for sync: {}", localUserId);
            return;
        }

        User user = userOpt.get();
        boolean updated = false;

        // Update email if it has changed
        JsonNode emailNode = supabaseUserData.get("email");
        if (emailNode != null && !emailNode.asText().equals(user.getEmail())) {
            user.setEmail(emailNode.asText());
            updated = true;
            log.debug("Updated email for user {}: {}", localUserId, emailNode.asText());
        }

        // Update email confirmation status
        JsonNode emailConfirmedAt = supabaseUserData.get("email_confirmed_at");
        if (emailConfirmedAt != null && !emailConfirmedAt.isNull()) {
            // You could add an emailConfirmed field to track this
            log.debug("User {} email confirmed at: {}", localUserId, emailConfirmedAt.asText());
        }

        // Update user metadata
        JsonNode userMetadata = supabaseUserData.get("user_metadata");
        if (userMetadata != null && !userMetadata.isNull()) {
            updateUserMetadata(user, userMetadata);
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
            log.debug("Saved updated user data for user: {}", localUserId);
        }
    }

    /**
     * Updates user metadata from Supabase.
     *
     * @param user The local user
     * @param metadata The metadata from Supabase
     */
    private void updateUserMetadata(User user, JsonNode metadata) {
        // Extract common metadata fields
        JsonNode nameNode = metadata.get("name");
        if (nameNode != null && nameNode.isTextual()) {
            // You could add a fullName field to the User entity
            log.debug("User metadata name: {}", nameNode.asText());
        }

        JsonNode avatarUrl = metadata.get("avatar_url");
        if (avatarUrl != null && avatarUrl.isTextual()) {
            // You could store this in user preferences or profile
            log.debug("User metadata avatar_url: {}", avatarUrl.asText());
        }

        // Add more metadata fields as needed based on your User entity
    }

    /**
     * Synchronizes user data to Supabase.
     * This updates the Supabase user profile with local application data.
     *
     * @param localUserId The local user ID
     * @param accessToken The Supabase access token
     */
    @Transactional
    public void syncUserToSupabase(Long localUserId, String accessToken) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            log.debug("Supabase auth not enabled, skipping sync to Supabase for user: {}", localUserId);
            return;
        }

        try {
            Optional<User> userOpt = userRepository.findById(localUserId);
            if (userOpt.isEmpty()) {
                log.warn("Local user not found for Supabase sync: {}", localUserId);
                return;
            }

            User user = userOpt.get();

            // Prepare metadata to update in Supabase
            // Note: This would require adding an updateUserMetadata method to SupabaseAuthService
            // For now, we just log that sync was prepared
            // TODO: Implement actual Supabase metadata update when SupabaseProfileService.updateUserMetadata is available
            log.info("Prepared user data for Supabase sync for user: {} (email: {})", localUserId, user.getEmail());

        } catch (Exception e) {
            log.error("Error syncing user data to Supabase for user {}: {}", localUserId, e.getMessage(), e);
        }
    }

    /**
     * Checks if a user's data is in sync between systems.
     * This is a basic implementation - in production, you'd want more sophisticated sync logic.
     *
     * @param localUserId The local user ID
     * @param accessToken The Supabase access token
     * @return true if data appears to be in sync
     */
    public boolean isUserInSync(Long localUserId, String accessToken) {
        // Basic sync check - compare last update timestamps
        // In a real implementation, you'd compare actual data fields
        return true; // Placeholder - always return true for now
    }
}
