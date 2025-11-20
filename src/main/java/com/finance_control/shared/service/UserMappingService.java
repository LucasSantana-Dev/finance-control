package com.finance_control.shared.service;

import com.finance_control.shared.dto.AuthResponse;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for mapping between Supabase users and local application users.
 * Handles automatic user creation and synchronization when using Supabase authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserMappingService {

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    /**
     * Finds or creates a local user based on Supabase authentication response.
     * This method ensures that Supabase users have corresponding local user accounts.
     *
     * @param supabaseResponse The Supabase authentication response
     * @return The local user ID
     */
    @Transactional
    public Long findOrCreateUserFromSupabase(AuthResponse supabaseResponse) {
        if (supabaseResponse == null || supabaseResponse.getUser() == null) {
            throw new IllegalArgumentException("Supabase response or user cannot be null");
        }

        AuthResponse.User supabaseUser = supabaseResponse.getUser();
        String supabaseUserId = supabaseUser.getId();
        String email = supabaseUser.getEmail();

        if (supabaseUserId == null || email == null) {
            throw new IllegalArgumentException("Supabase user ID and email are required");
        }

        log.debug("Finding or creating local user for Supabase user: {} ({})", supabaseUserId, email);

        // First, try to find by Supabase user ID
        Optional<User> existingUser = userRepository.findBySupabaseUserId(supabaseUserId);

        if (existingUser.isPresent()) {
            log.debug("Found existing user mapped to Supabase user: {}", supabaseUserId);
            return existingUser.get().getId();
        }

        // If not found by Supabase ID, try by email hash
        String emailHash = encryptionService.hashEmail(email);
        existingUser = userRepository.findByEmailHash(emailHash);

        if (existingUser.isPresent()) {
            log.debug("Found existing user by email: {}", email);
            // Update the user to include Supabase mapping if not already present
            User user = existingUser.get();
            updateUserWithSupabaseInfo(user, supabaseUser);
            return user.getId();
        }

        // User doesn't exist, create new one
        log.info("Creating new local user for Supabase user: {} ({})", supabaseUserId, email);
        User newUser = createUserFromSupabase(supabaseUser);
        User savedUser = userRepository.save(newUser);

        log.info("Created new local user with ID: {} for Supabase user: {}", savedUser.getId(), supabaseUserId);
        return savedUser.getId();
    }

    /**
     * Finds a user by their Supabase user ID.
     *
     * @param supabaseUserId The Supabase user ID
     * @return Optional containing the user if found
     */
    public Optional<User> findBySupabaseUserId(String supabaseUserId) {
        return userRepository.findBySupabaseUserId(supabaseUserId);
    }

    /**
     * Finds the local user ID by Supabase user ID.
     * Used by JWT filter to map Supabase tokens to local user IDs.
     *
     * @param supabaseUserId The Supabase user ID (UUID)
     * @return The local user ID, or null if not found
     */
    public Long findUserIdBySupabaseId(String supabaseUserId) {
        return findBySupabaseUserId(supabaseUserId)
                .map(User::getId)
                .orElse(null);
    }

    /**
     * Creates a new local user from Supabase user information.
     *
     * @param supabaseUser The Supabase user information
     * @return The created User entity
     */
    private User createUserFromSupabase(AuthResponse.User supabaseUser) {
        User user = new User();
        user.setEmail(supabaseUser.getEmail());
        // Set email hash for efficient lookups
        user.setEmailHash(encryptionService.hashEmail(supabaseUser.getEmail()));
        // Set Supabase user ID for mapping
        user.setSupabaseUserId(supabaseUser.getId());
        // Set default values
        user.setIsActive(true);
        // Password is null for Supabase users as they authenticate via Supabase

        return user;
    }

    /**
     * Updates an existing user with Supabase information.
     * This is useful when a user signs up locally first, then uses Supabase auth.
     *
     * @param user The existing user
     * @param supabaseUser The Supabase user information
     */
    private void updateUserWithSupabaseInfo(User user, AuthResponse.User supabaseUser) {
        // Update Supabase user ID if not already set
        if (user.getSupabaseUserId() == null && supabaseUser.getId() != null) {
            user.setSupabaseUserId(supabaseUser.getId());
        }

        // Update email if it has changed
        if (supabaseUser.getEmail() != null && !supabaseUser.getEmail().equals(user.getEmail())) {
            user.setEmail(supabaseUser.getEmail());
            // Update email hash when email changes
            user.setEmailHash(encryptionService.hashEmail(supabaseUser.getEmail()));
        }

        // Mark user as confirmed if Supabase says they're confirmed
        if (supabaseUser.getEmailConfirmedAt() != null && !supabaseUser.getEmailConfirmedAt().isEmpty()) {
            log.debug("User email confirmed via Supabase for user: {}", user.getId());
        }

        userRepository.save(user);
        log.debug("Updated user {} with Supabase information", user.getId());
    }

    /**
     * Gets the Supabase user ID for a local user.
     *
     * @param userId The local user ID
     * @return Optional containing the Supabase user ID if found
     */
    public Optional<String> getSupabaseUserId(Long userId) {
        return userRepository.findById(userId)
                .map(User::getSupabaseUserId)
                .filter(id -> id != null && !id.isEmpty());
    }

    /**
     * Checks if a local user is linked to a Supabase account.
     *
     * @param userId The local user ID
     * @return true if the user is linked to Supabase
     */
    public boolean isUserLinkedToSupabase(Long userId) {
        return getSupabaseUserId(userId).isPresent();
    }
}
