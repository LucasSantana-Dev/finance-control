package com.finance_control.users.repository;

import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.users.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity data access operations.
 * Provides methods for finding users by email and checking email existence,
 * in addition to the standard CRUD operations from BaseRepository.
 */
@Repository
public interface UserRepository extends BaseRepository<User, Long> {

    /**
     * Finds a user by their email hash.
     * Use this method for efficient lookups without decrypting emails.
     *
     * @param emailHash the SHA-256 hash of the email address
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmailHash(String emailHash);

    /**
     * Checks if a user exists with the given email hash.
     *
     * @param emailHash the SHA-256 hash of the email address
     * @return true if a user exists with the email hash, false otherwise
     */
    boolean existsByEmailHash(String emailHash);

    /**
     * Finds an active user by their email hash.
     *
     * @param emailHash the SHA-256 hash of the email address
     * @return an Optional containing the active user if found, empty otherwise
     */
    Optional<User> findByEmailHashAndIsActiveTrue(String emailHash);

    /**
     * Finds a user by their Supabase user ID.
     *
     * @param supabaseUserId the Supabase Auth user ID (UUID)
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findBySupabaseUserId(String supabaseUserId);

    /**
     * Checks if a user exists with the given Supabase user ID.
     *
     * @param supabaseUserId the Supabase Auth user ID (UUID)
     * @return true if a user exists with the Supabase user ID, false otherwise
     */
    boolean existsBySupabaseUserId(String supabaseUserId);

    /**
     * Finds a user by their email address.
     * NOTE: This method is deprecated for performance reasons.
     * Use findByEmailHash() instead after hashing the email.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     * @deprecated Use findByEmailHash() with hashed email for better performance
     */
    @Deprecated
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email address.
     * NOTE: This method is deprecated for performance reasons.
     * Use existsByEmailHash() instead after hashing the email.
     *
     * @param email the email address to check
     * @return true if a user exists with the email, false otherwise
     * @deprecated Use existsByEmailHash() with hashed email for better performance
     */
    @Deprecated
    boolean existsByEmail(String email);

    /**
     * Finds an active user by their email address.
     * NOTE: This method is deprecated for performance reasons.
     * Use findByEmailHashAndIsActiveTrue() instead after hashing the email.
     *
     * @param email the email address to search for
     * @return an Optional containing the active user if found, empty otherwise
     * @deprecated Use findByEmailHashAndIsActiveTrue() with hashed email for better performance
     */
    @Deprecated
    Optional<User> findByEmailAndIsActiveTrue(String email);

    @Override
    @Query("SELECT u FROM User u WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "u.emailHash LIKE CONCAT('%', :search, '%'))")
    Page<User> findAll(@Param("search") String search, Pageable pageable);
}
