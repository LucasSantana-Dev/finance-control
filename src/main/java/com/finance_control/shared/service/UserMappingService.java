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

        // First, try to find by Supabase user ID (if we have a mapping field)
        Optional<User> existingUser = findBySupabaseUserId(supabaseUserId);

        if (existingUser.isPresent()) {
            log.debug("Found existing user mapped to Supabase user: {}", supabaseUserId);
            return existingUser.get().getId();
        }

        // If not found by Supabase ID, try by email
        existingUser = userRepository.findByEmail(email);

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
     * This requires a database field to store the Supabase user ID mapping.
     *
     * @param supabaseUserId The Supabase user ID
     * @return Optional containing the user if found
     */
    public Optional<User> findBySupabaseUserId(String supabaseUserId) {
        // For now, we'll use email-based lookup as a workaround
        // In a production system, you should add a supabase_user_id field to the User entity
        // TODO: Add supabase_user_id field to User entity for proper mapping

        // This is a temporary solution - in practice, you'd store the Supabase UUID in the user table
        // For now, we'll return empty and rely on email-based matching during signup/login
        return Optional.empty();
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

        // Set default values
        user.setIsActive(true);

        // Note: Password is not set for Supabase users as they authenticate via Supabase
        // You might want to set a random password or mark them as externally authenticated
        // The User entity only has email, password, and isActive fields

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
        // Update user information from Supabase if needed
        // This could include updating email confirmation status, metadata, etc.

        // Update email if it has changed
        if (supabaseUser.getEmail() != null && !supabaseUser.getEmail().equals(user.getEmail())) {
            user.setEmail(supabaseUser.getEmail());
        }

        // Mark user as confirmed if Supabase says they're confirmed
        if (supabaseUser.getEmailConfirmedAt() != null && !supabaseUser.getEmailConfirmedAt().isEmpty()) {
            // You could add an emailConfirmed field to User entity
            log.debug("User email confirmed via Supabase for user: {}", user.getId());
        }

        userRepository.save(user);
        log.debug("Updated user {} with Supabase information", user.getId());
    }

    /**
     * Gets the Supabase user ID for a local user.
     * This requires storing the mapping in the database.
     *
     * @param userId The local user ID
     * @return Optional containing the Supabase user ID if found
     */
    public Optional<String> getSupabaseUserId(Long userId) {
        // TODO: Add supabase_user_id field to User entity to store the mapping
        // For now, return empty as this field doesn't exist yet
        return Optional.empty();
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
