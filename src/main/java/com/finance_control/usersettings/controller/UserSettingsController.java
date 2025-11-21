package com.finance_control.usersettings.controller;

import com.finance_control.usersettings.dto.UserSettingsDTO;
import com.finance_control.usersettings.service.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user-settings")
@Tag(name = "User Settings", description = "User preferences and application settings endpoints")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    public UserSettingsController(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @GetMapping
    @Operation(summary = "Get current user settings", description = "Retrieve the current user's application settings and preferences")
    public ResponseEntity<UserSettingsDTO> getCurrentUserSettings() {
        log.debug("GET request to retrieve current user settings");
        UserSettingsDTO settings = userSettingsService.getCurrentUserSettings();
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    @Operation(summary = "Update current user settings", description = "Update the current user's application settings and preferences")
    public ResponseEntity<UserSettingsDTO> updateCurrentUserSettings(@Valid @RequestBody UserSettingsDTO dto) {
        log.debug("PUT request to update current user settings");
        UserSettingsDTO updated = userSettingsService.updateCurrentUserSettings(dto);
        return ResponseEntity.ok(updated);
    }
}
