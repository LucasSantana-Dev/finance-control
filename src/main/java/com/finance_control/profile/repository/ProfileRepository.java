package com.finance_control.profile.repository;

import com.finance_control.profile.model.Profile;
import com.finance_control.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends BaseRepository<Profile, Long> {

    /**
     * Find profile by user ID.
     *
     * @param userId the user ID
     * @return optional containing the profile if found
     */
    Optional<Profile> findByUserId(Long userId);

    /**
     * Check if a profile exists for the given user ID.
     *
     * @param userId the user ID
     * @return true if profile exists, false otherwise
     */
    boolean existsByUserId(Long userId);
}
