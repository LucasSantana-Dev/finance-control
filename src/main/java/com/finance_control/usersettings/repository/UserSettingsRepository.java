package com.finance_control.usersettings.repository;

import com.finance_control.usersettings.model.UserSettings;
import com.finance_control.shared.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends BaseRepository<UserSettings, Long> {

    Optional<UserSettings> findByUserId(Long userId);

    @Query("SELECT us FROM UserSettings us WHERE us.user.id = :userId")
    Optional<UserSettings> findUserSettingsByUserId(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);
}
