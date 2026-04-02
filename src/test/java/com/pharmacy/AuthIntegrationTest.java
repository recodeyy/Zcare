package com.pharmacy;

import com.pharmacy.dto.AuthRequest;
import com.pharmacy.dto.AuthResponse;
import com.pharmacy.dto.RegisterRequest;
import com.pharmacy.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
public class AuthIntegrationTest extends BaseIntegrationTest {

        @Test
        void shouldRegisterAndLogin() throws Exception {
                // 1. Register
                RegisterRequest registerRequest = RegisterRequest.builder()
                                .username("testuser")
                                .fullName("Test User")
                                .email("test@example.com")
                                .password("password123")
                                .role(User.Role.PHARMACIST)
                                .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value("testuser"))
                                .andExpect(jsonPath("$.role").value("PHARMACIST"));

                // 2. Login
                AuthRequest authRequest = new AuthRequest("testuser", "password123");
                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString();
                AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);

                assertThat(authResponse.getToken()).isNotNull();
                assertThat(authResponse.getUsername()).isEqualTo("testuser");
        }

        @Test
        void shouldFailLoginWithWrongPassword() throws Exception {
                // Register first
                RegisterRequest registerRequest = RegisterRequest.builder()
                                .username("faultyuser")
                                .fullName("Faulty User")
                                .email("faulty@example.com")
                                .password("correct_password")
                                .role(User.Role.PHARMACIST)
                                .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                // Login with wrong password
                AuthRequest authRequest = new AuthRequest("faultyuser", "wrong_password");
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldFailRegisterWithDuplicateUsername() throws Exception {
                RegisterRequest registerRequest = RegisterRequest.builder()
                                .username("dupuser")
                                .fullName("Dup User")
                                .email("dup@example.com")
                                .password("password123")
                                .role(User.Role.PHARMACIST)
                                .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isConflict());
        }
}
