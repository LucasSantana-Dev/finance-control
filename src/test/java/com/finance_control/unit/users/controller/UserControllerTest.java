package com.finance_control.unit.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.users.controller.UserController;
import com.finance_control.users.dto.PasswordResetRequest;
import com.finance_control.users.dto.UserDTO;
import com.finance_control.users.dto.UserStatusRequest;
import com.finance_control.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setIsActive(true);
        testUserDTO.setCreatedAt(LocalDateTime.now());
        testUserDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /users/email/{email} should return user when found")
    void findByEmail_WithExistingEmail_ShouldReturnOk() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUserDTO));

        mockMvc.perform(get("/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("GET /users/email/{email} should return 404 when user not found")
    void findByEmail_WithNonExistingEmail_ShouldReturnNotFound() throws Exception {
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/email/nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(userService).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("GET /users/check-email/{email} should return true when email exists")
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() throws Exception {
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        mockMvc.perform(get("/users/check-email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(true));

        verify(userService).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("GET /users/check-email/{email} should return false when email does not exist")
    void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() throws Exception {
        when(userService.existsByEmail("nonexistent@example.com")).thenReturn(false);

        mockMvc.perform(get("/users/check-email/nonexistent@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(false));

        verify(userService).existsByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("DELETE /users/{id}/soft should deactivate user")
    void softDelete_WithValidId_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).softDelete(1L);

        mockMvc.perform(delete("/users/1/soft"))
                .andExpect(status().isNoContent());

        verify(userService).softDelete(1L);
    }

    @Test
    @DisplayName("POST /users/{id}/reactivate should reactivate user")
    void reactivate_WithValidId_ShouldReturnOk() throws Exception {
        doNothing().when(userService).reactivate(1L);

        mockMvc.perform(post("/users/1/reactivate"))
                .andExpect(status().isOk());

        verify(userService).reactivate(1L);
    }

    @Test
    @DisplayName("PUT /users/{id}/password should reset password when confirmation matches")
    void resetPassword_WithValidConfirmation_ShouldReturnOk() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");
        request.setReason("Administrative reset");

        doNothing().when(userService).resetPassword(eq(1L), eq("newPassword123"), eq("Administrative reset"));

        mockMvc.perform(put("/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).resetPassword(eq(1L), eq("newPassword123"), eq("Administrative reset"));
    }

    @Test
    @DisplayName("PUT /users/{id}/password should return 400 when password confirmation does not match")
    void resetPassword_WithMismatchedConfirmation_ShouldReturnBadRequest() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("differentPassword");
        request.setReason("Administrative reset");

        mockMvc.perform(put("/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).resetPassword(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("PUT /users/{id}/status should update user status to active")
    void updateStatus_WithActiveTrue_ShouldReturnOk() throws Exception {
        UserStatusRequest request = new UserStatusRequest();
        request.setActive(true);
        request.setReason("User account reactivated");

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(1L);
        updatedUserDTO.setEmail("test@example.com");
        updatedUserDTO.setIsActive(true);
        updatedUserDTO.setUpdatedAt(LocalDateTime.now());

        when(userService.updateStatus(eq(1L), eq(true), eq("User account reactivated")))
                .thenReturn(updatedUserDTO);

        mockMvc.perform(put("/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService).updateStatus(eq(1L), eq(true), eq("User account reactivated"));
    }

    @Test
    @DisplayName("PUT /users/{id}/status should update user status to inactive")
    void updateStatus_WithActiveFalse_ShouldReturnOk() throws Exception {
        UserStatusRequest request = new UserStatusRequest();
        request.setActive(false);
        request.setReason("User account deactivated");

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(1L);
        updatedUserDTO.setEmail("test@example.com");
        updatedUserDTO.setIsActive(false);
        updatedUserDTO.setUpdatedAt(LocalDateTime.now());

        when(userService.updateStatus(eq(1L), eq(false), eq("User account deactivated")))
                .thenReturn(updatedUserDTO);

        mockMvc.perform(put("/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(userService).updateStatus(eq(1L), eq(false), eq("User account deactivated"));
    }
}
