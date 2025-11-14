package com.finance_control.unit.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.exception.GlobalExceptionHandler;
import com.finance_control.shared.monitoring.SentryService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private SentryService sentryService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler(sentryService))
                .build();

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

    // Error handling tests

    @Test
    @DisplayName("findByEmail_WithServiceException_ShouldReturnInternalServerError")
    void findByEmail_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        when(userService.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(get("/users/email/test@example.com"))
                .andExpect(status().isInternalServerError());

        verify(userService).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("softDelete_WithNonExistentId_ShouldThrowException")
    void softDelete_WithNonExistentId_ShouldThrowException() throws Exception {
        doThrow(new EntityNotFoundException("User not found with id: 999"))
                .when(userService).softDelete(999L);

        mockMvc.perform(delete("/users/999/soft"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));

        verify(userService).softDelete(999L);
    }

    @Test
    @DisplayName("reactivate_WithNonExistentId_ShouldThrowException")
    void reactivate_WithNonExistentId_ShouldThrowException() throws Exception {
        doThrow(new EntityNotFoundException("User not found with id: 999"))
                .when(userService).reactivate(999L);

        mockMvc.perform(post("/users/999/reactivate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));

        verify(userService).reactivate(999L);
    }

    @Test
    @DisplayName("resetPassword_WithNonExistentId_ShouldThrowException")
    void resetPassword_WithNonExistentId_ShouldThrowException() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");
        request.setReason("Administrative reset");

        doThrow(new EntityNotFoundException("User not found with id: 999"))
                .when(userService).resetPassword(eq(999L), anyString(), anyString());

        mockMvc.perform(put("/users/999/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));

        verify(userService).resetPassword(eq(999L), anyString(), anyString());
    }

    @Test
    @DisplayName("resetPassword_WithInvalidPassword_ShouldReturnBadRequest")
    void resetPassword_WithInvalidPassword_ShouldReturnBadRequest() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("short");
        request.setConfirmPassword("short");
        request.setReason("Administrative reset");

        mockMvc.perform(put("/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).resetPassword(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("resetPassword_WithNullPassword_ShouldReturnBadRequest")
    void resetPassword_WithNullPassword_ShouldReturnBadRequest() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword(null);
        request.setConfirmPassword(null);
        request.setReason("Administrative reset");

        mockMvc.perform(put("/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).resetPassword(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("updateStatus_WithNonExistentId_ShouldThrowException")
    void updateStatus_WithNonExistentId_ShouldThrowException() throws Exception {
        UserStatusRequest request = new UserStatusRequest();
        request.setActive(true);
        request.setReason("User account reactivated");

        when(userService.updateStatus(eq(999L), anyBoolean(), anyString()))
                .thenThrow(new EntityNotFoundException("User not found with id: 999"));

        mockMvc.perform(put("/users/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));

        verify(userService).updateStatus(eq(999L), anyBoolean(), anyString());
    }

    @Test
    @DisplayName("updateStatus_WithNullActive_ShouldReturnBadRequest")
    void updateStatus_WithNullActive_ShouldReturnBadRequest() throws Exception {
        UserStatusRequest request = new UserStatusRequest();
        request.setActive(null);
        request.setReason("User account reactivated");

        mockMvc.perform(put("/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateStatus(anyLong(), anyBoolean(), anyString());
    }

    // BaseController CRUD operations tests

    @Test
    @DisplayName("findById_WithExistingId_ShouldReturnOk")
    void findById_WithExistingId_ShouldReturnOk() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUserDTO));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).findById(1L);
    }

    @Test
    @DisplayName("findById_WithNonExistentId_ShouldReturnNotFound")
    void findById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).findById(999L);
    }

    @Test
    @DisplayName("findAll_WithDefaultParameters_ShouldReturnOk")
    void findAll_WithDefaultParameters_ShouldReturnOk() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(List.of(testUserDTO), PageRequest.of(0, 20), 1);

        when(userService.findAll(any(), anyMap(), any(), any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(userService).findAll(any(), anyMap(), any(), any(), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("findAll_WithSearchParameter_ShouldReturnFilteredResults")
    void findAll_WithSearchParameter_ShouldReturnFilteredResults() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(List.of(testUserDTO), PageRequest.of(0, 20), 1);

        when(userService.findAll(eq("test"), anyMap(), any(), any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/users")
                        .param("search", "test")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)));

        verify(userService).findAll(eq("test"), anyMap(), any(), any(), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("findAll_WithPagination_ShouldReturnPagedResults")
    void findAll_WithPagination_ShouldReturnPagedResults() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 5), 15);

        when(userService.findAll(any(), anyMap(), any(), any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/users")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));

        verify(userService).findAll(any(), anyMap(), any(), any(), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("findAll_WithSorting_ShouldReturnSortedResults")
    void findAll_WithSorting_ShouldReturnSortedResults() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(List.of(testUserDTO), PageRequest.of(0, 20), 1);

        when(userService.findAll(any(), anyMap(), eq("email"), eq("desc"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/users")
                        .param("sortBy", "email")
                        .param("sortDirection", "desc")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)));

        verify(userService).findAll(any(), anyMap(), eq("email"), eq("desc"), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("findAll_WithEmptyResults_ShouldReturnEmptyPage")
    void findAll_WithEmptyResults_ShouldReturnEmptyPage() throws Exception {
        Page<UserDTO> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

        when(userService.findAll(any(), anyMap(), any(), any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(userService).findAll(any(), anyMap(), any(), any(), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("create_WithValidUser_ShouldReturnCreated")
    void create_WithValidUser_ShouldReturnCreated() throws Exception {
        UserDTO createDTO = new UserDTO();
        createDTO.setEmail("newuser@example.com");
        createDTO.setPassword("Password123");

        UserDTO createdDTO = new UserDTO();
        createdDTO.setId(2L);
        createdDTO.setEmail("newuser@example.com");
        createdDTO.setIsActive(true);

        when(userService.create(any(UserDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        verify(userService).create(any(UserDTO.class));
    }

    @Test
    @DisplayName("create_WithInvalidUser_ShouldReturnBadRequest")
    void create_WithInvalidUser_ShouldReturnBadRequest() throws Exception {
        UserDTO createDTO = new UserDTO();
        createDTO.setEmail("invalid-email");
        createDTO.setPassword("short");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(UserDTO.class));
    }

    @Test
    @DisplayName("update_WithValidUser_ShouldReturnOk")
    void update_WithValidUser_ShouldReturnOk() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("updated@example.com");

        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setId(1L);
        updatedDTO.setEmail("updated@example.com");
        updatedDTO.setIsActive(true);

        when(userService.update(eq(1L), any(UserDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService).update(eq(1L), any(UserDTO.class));
    }

    @Test
    @DisplayName("update_WithNonExistentId_ShouldReturnNotFound")
    void update_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("updated@example.com");

        when(userService.update(eq(999L), any(UserDTO.class)))
                .thenThrow(new EntityNotFoundException("User not found with id: 999"));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));

        verify(userService).update(eq(999L), any(UserDTO.class));
    }

    @Test
    @DisplayName("delete_WithValidId_ShouldReturnNoContent")
    void delete_WithValidId_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    @DisplayName("delete_WithNonExistentId_ShouldThrowException")
    void delete_WithNonExistentId_ShouldThrowException() throws Exception {
        doThrow(new EntityNotFoundException("User not found with id: 999"))
                .when(userService).delete(999L);

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));

        verify(userService).delete(999L);
    }

    // Invalid parameter tests

    @Test
    @DisplayName("findByEmail_WithSpecialCharacters_ShouldHandleGracefully")
    void findByEmail_WithSpecialCharacters_ShouldHandleGracefully() throws Exception {
        String emailWithSpecialChars = "test+user@example.com";
        when(userService.findByEmail(emailWithSpecialChars)).thenReturn(Optional.of(testUserDTO));

        mockMvc.perform(get("/users/email/" + emailWithSpecialChars))
                .andExpect(status().isOk());

        verify(userService).findByEmail(emailWithSpecialChars);
    }

    @Test
    @DisplayName("resetPassword_WithEmptyReason_ShouldReturnOk")
    void resetPassword_WithEmptyReason_ShouldReturnOk() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");
        request.setReason("");

        doNothing().when(userService).resetPassword(eq(1L), eq("newPassword123"), eq(""));

        mockMvc.perform(put("/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).resetPassword(eq(1L), eq("newPassword123"), eq(""));
    }
}
