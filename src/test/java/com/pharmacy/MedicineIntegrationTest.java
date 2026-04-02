package com.pharmacy;

import com.pharmacy.dto.AuthRequest;
import com.pharmacy.dto.AuthResponse;
import com.pharmacy.dto.MedicineRequest;
import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.dto.RegisterRequest;
import com.pharmacy.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
public class MedicineIntegrationTest extends BaseIntegrationTest {

        private String pharmacistToken;

        @BeforeEach
        void setUp() throws Exception {
                // Register a user (defaults to PHARMACIST in AuthService)
                RegisterRequest registerRequest = RegisterRequest.builder()
                                .username("pharma_user")
                                .fullName("Pharma Cist")
                                .email("pharma@example.com")
                                .password("password123")
                                .role(User.Role.PHARMACIST)
                                .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                AuthRequest authRequest = new AuthRequest("pharma_user", "password123");
                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                AuthResponse authResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                                AuthResponse.class);
                pharmacistToken = "Bearer " + authResponse.getToken();
        }

        @Test
        void shouldCreateAndRetrieveMedicine() throws Exception {
                MedicineRequest request = MedicineRequest.builder()
                                .name("Paracetamol")
                                .category("Analgesic")
                                .price(10.5)
                                .stockQuantity(100)
                                .expiryDate(LocalDate.now().plusYears(1))
                                .manufacturer("GSK")
                                .build();

                MvcResult createResult = mockMvc.perform(post("/api/medicines")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();

                MedicineResponse response = objectMapper.readValue(createResult.getResponse().getContentAsString(),
                                MedicineResponse.class);
                assertThat(response.getId()).isNotNull();
                assertThat(response.getName()).isEqualTo("Paracetamol");

                mockMvc.perform(get("/api/medicines/" + response.getId())
                                .header("Authorization", pharmacistToken))
                                .andExpect(status().isOk());
        }

        @Test
        void shouldFailWithNegativePrice() throws Exception {
                MedicineRequest request = MedicineRequest.builder()
                                .name("Invalid")
                                .price(-1.0)
                                .stockQuantity(10)
                                .build();

                mockMvc.perform(post("/api/medicines")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldFailWhenUnauthorized() throws Exception {
                mockMvc.perform(post("/api/medicines")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isUnauthorized());
        }
}
