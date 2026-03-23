package com.pharmacy.dto;

import com.pharmacy.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ── Login Request ───────────────────────────────────────────
class LoginRequest {
    @NotBlank(message = "Username is required")
    public String username;

    @NotBlank(message = "Password is required")
    public String password;
}

// ── Register Request ────────────────────────────────────────
class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    public String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    public String password;

    @NotBlank(message = "Full name is required")
    public String fullName;

    @Email(message = "Valid email is required")
    public String email;

    public User.Role role = User.Role.PHARMACIST; // Default role
}

// ── Auth Response ────────────────────────────────────────────
class AuthResponse {
    public String token;
    public String username;
    public String fullName;
    public String role;
    public String message;

    public static AuthResponse of(String token, String username, String fullName, String role) {
        AuthResponse r = new AuthResponse();
        r.token    = token;
        r.username = username;
        r.fullName = fullName;
        r.role     = role;
        r.message  = "Login successful";
        return r;
    }
}

// ── API Response wrapper ─────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }
}
