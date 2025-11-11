package com.finance_control.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.profile.dto.ProfileDTO;
import com.finance_control.profile.dto.ProfileUpdateRequest;
import com.finance_control.profile.service.ProfileService;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.security.CustomUserDetails;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProfileDTO testProfileDTO;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);

        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        testUserDetails = new CustomUserDetails(testUser);

        testProfileDTO = new ProfileDTO();
        testProfileDTO.setId(1L);
        testProfileDTO.setFullName("John Doe");
        testProfileDTO.setBio("Test bio");
        testProfileDTO.setPhone("+1234567890");
        testProfileDTO.setCountry("US");
        testProfileDTO.setAvatarUrl("https://example.com/avatar.jpg");
        testProfileDTO.setCreatedAt(LocalDateTime.now());
        testProfileDTO.setUpdatedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getCurrentProfile_WithValidUser_ShouldReturnOk() throws Exception {
        when(profileService.getCurrentProfile()).thenReturn(testProfileDTO);

        mockMvc.perform(get("/api/profile")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.bio").value("Test bio"))
                .andExpect(jsonPath("$.phone").value("+1234567890"))
                .andExpect(jsonPath("$.country").value("US"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"));

        verify(profileService).getCurrentProfile();
    }

    @Test
    void getCurrentProfile_WhenProfileNotFound_ShouldReturnInternalServerError() throws Exception {
        when(profileService.getCurrentProfile())
                .thenThrow(new RuntimeException("Profile not found"));

        mockMvc.perform(get("/api/profile")
                        .with(user(testUserDetails)))
                .andExpect(status().isInternalServerError());

        verify(profileService).getCurrentProfile();
    }

    @Test
    void updateCurrentProfile_WithValidRequest_ShouldReturnOk() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "Jane Doe",
                "test@example.com",
                "Updated bio",
                "+9876543210",
                "BR",
                "https://example.com/new-avatar.jpg"
        );

        ProfileDTO updatedProfileDTO = new ProfileDTO();
        updatedProfileDTO.setId(1L);
        updatedProfileDTO.setFullName("Jane Doe");
        updatedProfileDTO.setBio("Updated bio");
        updatedProfileDTO.setPhone("+9876543210");
        updatedProfileDTO.setCountry("BR");
        updatedProfileDTO.setAvatarUrl("https://example.com/new-avatar.jpg");
        updatedProfileDTO.setUpdatedAt(LocalDateTime.now());

        when(profileService.updateCurrentProfile(any(ProfileUpdateRequest.class)))
                .thenReturn(updatedProfileDTO);

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.bio").value("Updated bio"))
                .andExpect(jsonPath("$.phone").value("+9876543210"))
                .andExpect(jsonPath("$.country").value("BR"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/new-avatar.jpg"));

        verify(profileService).updateCurrentProfile(any(ProfileUpdateRequest.class));
    }

    @Test
    void updateCurrentProfile_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "",
                "test@example.com",
                "Test bio",
                "+1234567890",
                "US",
                "https://example.com/avatar.jpg"
        );

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testUserDetails)))
                .andExpect(status().isBadRequest());

        verify(profileService, never()).updateCurrentProfile(any(ProfileUpdateRequest.class));
    }

    @Test
    void updateCurrentProfile_WithEmailConflict_ShouldReturnInternalServerError() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "John Doe",
                "existing@example.com",
                "Test bio",
                "+1234567890",
                "US",
                "https://example.com/avatar.jpg"
        );

        when(profileService.updateCurrentProfile(any(ProfileUpdateRequest.class)))
                .thenThrow(new RuntimeException("Email already in use"));

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testUserDetails)))
                .andExpect(status().isInternalServerError());

        verify(profileService).updateCurrentProfile(any(ProfileUpdateRequest.class));
    }

    @Test
    void updateCurrentProfile_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        String invalidJson = "{\"fullName\":\"John Doe\"}";

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                        .with(user(testUserDetails)))
                .andExpect(status().isBadRequest());

        verify(profileService, never()).updateCurrentProfile(any(ProfileUpdateRequest.class));
    }
}




