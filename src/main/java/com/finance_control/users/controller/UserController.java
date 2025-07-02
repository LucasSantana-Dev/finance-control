package com.finance_control.users.controller;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.users.dto.UserDTO;
import com.finance_control.users.model.User;
import com.finance_control.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * REST controller for managing user operations.
 * Provides endpoints for CRUD operations on users and user-specific
 * operations like finding users by email and checking email existence.
 */
@RestController
@Slf4j
@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController extends BaseController<User, Long, UserDTO> {

    /** The user service for business logic operations */
    private final UserService userService;

    public UserController(UserService userService) {
        super(userService);
        this.userService = userService;
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve a user by their email address.")
    public ResponseEntity<UserDTO> findByEmail(@PathVariable String email) {
        Optional<UserDTO> dto = userService.findByEmail(email);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/check-email/{email}")
    @Operation(summary = "Check email exists", description = "Check if a user with the given email exists.")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @DeleteMapping("/{id}/soft")
    @Operation(summary = "Soft delete user", description = "Deactivate a user account instead of hard deleting it.")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        userService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate user", description = "Reactivate a previously deactivated user account.")
    public ResponseEntity<Void> reactivate(@PathVariable Long id) {
        userService.reactivate(id);
        return ResponseEntity.ok().build();
    }
}