package com.pharmacy.controller;

import com.pharmacy.model.User;
import com.pharmacy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Administration and user control")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Admin only")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Admin only")
    public ResponseEntity<User> getUserById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user account", description = "Admin only")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable @NonNull Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }


    @PatchMapping("/{id}/role")
    @Operation(summary = "Update user role", description = "Admin only")
    public ResponseEntity<User> updateRole(@PathVariable @NonNull Long id, @RequestParam @NonNull User.Role role) {
        return ResponseEntity.ok(userService.updateRole(id, role));
    }


    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate/Deactivate user account", description = "Admin only")
    public ResponseEntity<User> updateStatus(@PathVariable @NonNull Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(userService.updateStatus(id, active));
    }

}
