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
     * Finds a user by their email address.
     * 
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email address.
     * 
     * @param email the email address to check
     * @return true if a user exists with the email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds an active user by their email address.
     * 
     * @param email the email address to search for
     * @return an Optional containing the active user if found, empty otherwise
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    @Override
    @Query("SELECT u FROM User u WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findAll(@Param("search") String search, Pageable pageable);
}