package com.finance_control.usersettings.service;

import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import com.finance_control.usersettings.dto.UserSettingsDTO;
import com.finance_control.usersettings.enums.CurrencyFormat;
import com.finance_control.usersettings.enums.DateFormat;
import com.finance_control.usersettings.enums.Theme;
import com.finance_control.usersettings.model.UserSettings;
import com.finance_control.usersettings.repository.UserSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    public UserSettingsService(UserSettingsRepository userSettingsRepository, UserRepository userRepository) {
        this.userSettingsRepository = userSettingsRepository;
        this.userRepository = userRepository;
    }

    public UserSettingsDTO getCurrentUserSettings() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Getting user settings for user {}", userId);
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserSettings defaultSettings = new UserSettings();
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User", "id", userId));
                    defaultSettings.setUser(user);
                    defaultSettings.setCurrencyFormat(CurrencyFormat.BRL);
                    defaultSettings.setDateFormat(DateFormat.DD_MM_YYYY);
                    defaultSettings.setNotificationEmail(true);
                    defaultSettings.setNotificationPush(false);
                    defaultSettings.setNotificationTransactions(true);
                    defaultSettings.setNotificationGoals(true);
                    defaultSettings.setNotificationWeeklySummary(true);
                    defaultSettings.setTheme(Theme.LIGHT);
                    defaultSettings.setLanguage("pt-BR");
                    return userSettingsRepository.save(defaultSettings);
                });
        return mapToDTO(settings);
    }

    public UserSettingsDTO getUserSettings(Long userId) {
        log.debug("Getting user settings for user {}", userId);
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserSettings", "userId", userId));
        return mapToDTO(settings);
    }

    public UserSettingsDTO updateCurrentUserSettings(UserSettingsDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Updating user settings for user {}", userId);
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User", "id", userId));
                    UserSettings defaultSettings = new UserSettings();
                    defaultSettings.setUser(user);
                    defaultSettings.setCurrencyFormat(CurrencyFormat.BRL);
                    defaultSettings.setDateFormat(DateFormat.DD_MM_YYYY);
                    defaultSettings.setNotificationEmail(true);
                    defaultSettings.setNotificationPush(false);
                    defaultSettings.setNotificationTransactions(true);
                    defaultSettings.setNotificationGoals(true);
                    defaultSettings.setNotificationWeeklySummary(true);
                    defaultSettings.setTheme(Theme.LIGHT);
                    defaultSettings.setLanguage("pt-BR");
                    return userSettingsRepository.save(defaultSettings);
                });

        updateSettingsFromDTO(settings, dto);
        UserSettings saved = userSettingsRepository.save(settings);
        log.info("User settings updated successfully for user {}", userId);
        return mapToDTO(saved);
    }

    public UserSettingsDTO createDefaultSettings(Long userId) {
        log.debug("Creating default settings for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", "id", userId));

        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setCurrencyFormat(CurrencyFormat.BRL);
        settings.setDateFormat(DateFormat.DD_MM_YYYY);
        settings.setNotificationEmail(true);
        settings.setNotificationPush(false);
        settings.setNotificationTransactions(true);
        settings.setNotificationGoals(true);
        settings.setNotificationWeeklySummary(true);
        settings.setTheme(Theme.LIGHT);
        settings.setLanguage("pt-BR");

        UserSettings saved = userSettingsRepository.save(settings);
        log.info("Default user settings created for user {}", userId);
        return mapToDTO(saved);
    }

    private void updateSettingsFromDTO(UserSettings settings, UserSettingsDTO dto) {
        if (dto.getCurrencyFormat() != null) {
            settings.setCurrencyFormat(dto.getCurrencyFormat());
        }
        if (dto.getDateFormat() != null) {
            settings.setDateFormat(dto.getDateFormat());
        }
        if (dto.getNotificationEmail() != null) {
            settings.setNotificationEmail(dto.getNotificationEmail());
        }
        if (dto.getNotificationPush() != null) {
            settings.setNotificationPush(dto.getNotificationPush());
        }
        if (dto.getNotificationTransactions() != null) {
            settings.setNotificationTransactions(dto.getNotificationTransactions());
        }
        if (dto.getNotificationGoals() != null) {
            settings.setNotificationGoals(dto.getNotificationGoals());
        }
        if (dto.getNotificationWeeklySummary() != null) {
            settings.setNotificationWeeklySummary(dto.getNotificationWeeklySummary());
        }
        if (dto.getTheme() != null) {
            settings.setTheme(dto.getTheme());
        }
        if (dto.getLanguage() != null) {
            settings.setLanguage(dto.getLanguage());
        }
    }

    private UserSettingsDTO mapToDTO(UserSettings settings) {
        UserSettingsDTO dto = UserSettingsDTO.builder()
                .userId(settings.getUser() != null ? settings.getUser().getId() : null)
                .currencyFormat(settings.getCurrencyFormat())
                .dateFormat(settings.getDateFormat())
                .notificationEmail(settings.getNotificationEmail())
                .notificationPush(settings.getNotificationPush())
                .notificationTransactions(settings.getNotificationTransactions())
                .notificationGoals(settings.getNotificationGoals())
                .notificationWeeklySummary(settings.getNotificationWeeklySummary())
                .theme(settings.getTheme())
                .language(settings.getLanguage())
                .build();
        dto.setId(settings.getId());
        dto.setCreatedAt(settings.getCreatedAt());
        dto.setUpdatedAt(settings.getUpdatedAt());
        return dto;
    }
}
