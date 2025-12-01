package com.finance_control.shared.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Service for managing user profiles in Supabase.
 * Provides methods to update user metadata, preferences, and profile information.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.enabled", havingValue = "true", matchIfMissing = false)
public class SupabaseProfileService {

    @Qualifier("supabaseRestClient")
    private final RestClient restClient;

    /**
     * Updates user metadata in Supabase.
     *
     * @param accessToken The user's access token
     * @param metadata The metadata to update
     */
    public void updateUserMetadata(String accessToken, Map<String, Object> metadata) {
        log.debug("Updating user metadata in Supabase");

        try {
            restClient.put()
                    .uri("/auth/v1/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("data", metadata))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully updated user metadata in Supabase");
        } catch (RestClientException e) {
            log.error("Failed to update user metadata in Supabase: {}", e.getMessage());
            throw new RuntimeException("Failed to update user metadata", e);
        }
    }

    /**
     * Updates user email in Supabase.
     * Note: This will send a confirmation email to both old and new email addresses.
     *
     * @param accessToken The user's access token
     * @param newEmail The new email address
     */
    public void updateUserEmail(String accessToken, String newEmail) {
        log.debug("Updating user email in Supabase to: {}", newEmail);

        try {
            restClient.put()
                    .uri("/auth/v1/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", newEmail))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully initiated email update in Supabase for: {}", newEmail);
        } catch (RestClientException e) {
            log.error("Failed to update user email in Supabase: {}", e.getMessage());
            throw new RuntimeException("Failed to update user email", e);
        }
    }

    /**
     * Updates user password in Supabase.
     *
     * @param accessToken The user's access token
     * @param newPassword The new password
     */
    public void updateUserPassword(String accessToken, String newPassword) {
        log.debug("Updating user password in Supabase");

        try {
            restClient.put()
                    .uri("/auth/v1/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("password", newPassword))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully updated user password in Supabase");
        } catch (RestClientException e) {
            log.error("Failed to update user password in Supabase: {}", e.getMessage());
            throw new RuntimeException("Failed to update user password", e);
        }
    }

    /**
     * Gets comprehensive user profile information from Supabase.
     *
     * @param accessToken The user's access token
     * @return user profile data
     */
    public JsonNode getUserProfile(String accessToken) {
        log.debug("Getting user profile from Supabase");

        try {
            JsonNode profile = restClient.get()
                    .uri("/auth/v1/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);

            log.debug("Successfully retrieved user profile from Supabase");
            return profile;
        } catch (RestClientException e) {
            log.error("Failed to get user profile from Supabase: {}", e.getMessage());
            throw new RuntimeException("Failed to get user profile", e);
        }
    }

    /**
     * Updates user preferences/metadata in Supabase.
     * This is a convenience method for common profile updates.
     *
     * @param accessToken The user's access token
     * @param preferences User preferences to update
     */
    public void updateUserPreferences(String accessToken, Map<String, Object> preferences) {
        log.debug("Updating user preferences in Supabase");

        preferences.put("preferences_updated_at", System.currentTimeMillis());
        updateUserMetadata(accessToken, Map.of("preferences", preferences));
    }

    /**
     * Links a local application user ID to the Supabase user profile.
     * This helps maintain the relationship between systems.
     *
     * @param accessToken The user's access token
     * @param localUserId The local application user ID
     */
    public void linkLocalUser(String accessToken, Long localUserId) {
        log.debug("Linking local user ID to Supabase profile: {}", localUserId);

        Map<String, Object> linkData = Map.of(
            "local_user_id", localUserId.toString(),
            "linked_at", System.currentTimeMillis()
        );

        updateUserMetadata(accessToken, linkData);
    }

    /**
     * Updates user avatar URL in Supabase metadata.
     *
     * @param accessToken The user's access token
     * @param avatarUrl The avatar URL (could be from Supabase Storage or external)
     */
    public void updateUserAvatar(String accessToken, String avatarUrl) {
        log.debug("Updating user avatar in Supabase: {}", avatarUrl);

        updateUserMetadata(accessToken, Map.of("avatar_url", avatarUrl));
    }

    /**
     * Updates user's display name in Supabase metadata.
     *
     * @param accessToken The user's access token
     * @param displayName The display name
     */
    public void updateDisplayName(String accessToken, String displayName) {
        log.debug("Updating display name in Supabase: {}", displayName);

        updateUserMetadata(accessToken, Map.of("display_name", displayName));
    }
}
