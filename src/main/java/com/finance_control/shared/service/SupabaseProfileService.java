package com.finance_control.shared.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

    @Qualifier("supabaseWebClient")
    private final WebClient webClient;

    /**
     * Updates user metadata in Supabase.
     *
     * @param accessToken The user's access token
     * @param metadata The metadata to update
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> updateUserMetadata(String accessToken, Map<String, Object> metadata) {
        log.debug("Updating user metadata in Supabase");

        return webClient.put()
                .uri("/auth/v1/user")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("data", metadata))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Successfully updated user metadata in Supabase"))
                .doOnError(error -> log.error("Failed to update user metadata in Supabase: {}", error.getMessage()));
    }

    /**
     * Updates user email in Supabase.
     * Note: This will send a confirmation email to both old and new email addresses.
     *
     * @param accessToken The user's access token
     * @param newEmail The new email address
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> updateUserEmail(String accessToken, String newEmail) {
        log.debug("Updating user email in Supabase to: {}", newEmail);

        return webClient.put()
                .uri("/auth/v1/user")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", newEmail))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Successfully initiated email update in Supabase for: {}", newEmail))
                .doOnError(error -> log.error("Failed to update user email in Supabase: {}", error.getMessage()));
    }

    /**
     * Updates user password in Supabase.
     *
     * @param accessToken The user's access token
     * @param newPassword The new password
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> updateUserPassword(String accessToken, String newPassword) {
        log.debug("Updating user password in Supabase");

        return webClient.put()
                .uri("/auth/v1/user")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("password", newPassword))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Successfully updated user password in Supabase"))
                .doOnError(error -> log.error("Failed to update user password in Supabase: {}", error.getMessage()));
    }

    /**
     * Gets comprehensive user profile information from Supabase.
     *
     * @param accessToken The user's access token
     * @return Mono containing user profile data
     */
    public Mono<JsonNode> getUserProfile(String accessToken) {
        log.debug("Getting user profile from Supabase");

        return webClient.get()
                .uri("/auth/v1/user")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnSuccess(profile -> log.debug("Successfully retrieved user profile from Supabase"))
                .doOnError(error -> log.error("Failed to get user profile from Supabase: {}", error.getMessage()));
    }

    /**
     * Updates user preferences/metadata in Supabase.
     * This is a convenience method for common profile updates.
     *
     * @param accessToken The user's access token
     * @param preferences User preferences to update
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> updateUserPreferences(String accessToken, Map<String, Object> preferences) {
        log.debug("Updating user preferences in Supabase");

        // Add a timestamp to track when preferences were last updated
        preferences.put("preferences_updated_at", System.currentTimeMillis());

        return updateUserMetadata(accessToken, Map.of("preferences", preferences));
    }

    /**
     * Links a local application user ID to the Supabase user profile.
     * This helps maintain the relationship between systems.
     *
     * @param accessToken The user's access token
     * @param localUserId The local application user ID
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> linkLocalUser(String accessToken, Long localUserId) {
        log.debug("Linking local user ID to Supabase profile: {}", localUserId);

        Map<String, Object> linkData = Map.of(
            "local_user_id", localUserId.toString(),
            "linked_at", System.currentTimeMillis()
        );

        return updateUserMetadata(accessToken, linkData);
    }

    /**
     * Updates user avatar URL in Supabase metadata.
     *
     * @param accessToken The user's access token
     * @param avatarUrl The avatar URL (could be from Supabase Storage or external)
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> updateUserAvatar(String accessToken, String avatarUrl) {
        log.debug("Updating user avatar in Supabase: {}", avatarUrl);

        return updateUserMetadata(accessToken, Map.of("avatar_url", avatarUrl));
    }

    /**
     * Updates user's display name in Supabase metadata.
     *
     * @param accessToken The user's access token
     * @param displayName The display name
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> updateDisplayName(String accessToken, String displayName) {
        log.debug("Updating display name in Supabase: {}", displayName);

        return updateUserMetadata(accessToken, Map.of("display_name", displayName));
    }
}
