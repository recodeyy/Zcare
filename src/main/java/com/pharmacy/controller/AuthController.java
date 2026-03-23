package com.pharmacy.controller;

import com.pharmacy.model.User;
import com.pharmacy.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    // ── Register ──────────────────────────────────────────────
    @PostMapping("/register")
    @Operation(summary = "Register a new user",
               description = "Creates a new pharmacist or admin account")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        Map<String, Object> result = authService.register(
                req.username, req.password, req.fullName, req.email, req.role);
        return ResponseEntity.ok(result);
    }

    // ── Login ─────────────────────────────────────────────────
    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token",
               description = "Returns a JWT token. Use it in the Authorization header as: Bearer <token>")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Map<String, Object> result = authService.login(req.username, req.password);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    // ── DTOs (inner classes for simplicity) ───────────────────

    @Data
    static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Full name is required")
        private String fullName;

        @Email(message = "Enter a valid email")
        private String email;

        private User.Role role = User.Role.PHARMACIST;
    }
}
