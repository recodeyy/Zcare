package com.pharmacy;

import java.time.LocalDate;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmacy.dto.AuthRequest;
import com.pharmacy.dto.AuthResponse;
import com.pharmacy.dto.MedicineRequest;
import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.dto.RegisterRequest;
import com.pharmacy.model.User;

@SuppressWarnings("null")
public class MedicineAdvancedIntegrationTest extends BaseIntegrationTest {

    private String pharmacistToken;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("medicine_admin")
                .fullName("Medicine Admin")
                .email("medicine.admin@example.com")
                .password("password123")
                .role(User.Role.PHARMACIST)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        AuthRequest authRequest = new AuthRequest("medicine_admin", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        pharmacistToken = "Bearer " + authResponse.getToken();
    }

    @Test
    void shouldLookupMedicineByBarcode() throws Exception {
        MedicineResponse created = createMedicine("Barcode Medicine", "BARCODE-001", 25, LocalDate.now().plusMonths(6));

        mockMvc.perform(get("/api/medicines/barcode/" + created.getBarcode())
                        .header("Authorization", pharmacistToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Barcode Medicine"))
                .andExpect(jsonPath("$.barcode").value("BARCODE-001"));
    }

    @Test
    void shouldReturnPaginatedMedicines() throws Exception {
        createMedicine("Paged A", "PAGED-A", 10, LocalDate.now().plusMonths(6));
        createMedicine("Paged B", "PAGED-B", 12, LocalDate.now().plusMonths(7));
        createMedicine("Paged C", "PAGED-C", 14, LocalDate.now().plusMonths(8));

        mockMvc.perform(get("/api/medicines/page")
                        .header("Authorization", pharmacistToken)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(3)));
    }

    @Test
    void shouldReturnExpiringSoonMedicines() throws Exception {
        createMedicine("Soon Medicine", "SOON-001", 20, LocalDate.now().plusDays(10));
        createMedicine("Late Medicine", "LATE-001", 20, LocalDate.now().plusDays(60));

        mockMvc.perform(get("/api/medicines/expiring-soon")
                        .header("Authorization", pharmacistToken)
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Soon Medicine"));
    }

    @Test
    void shouldSoftDeleteAndRestoreMedicine() throws Exception {
        MedicineResponse created = createMedicine("Soft Delete Medicine", "SOFT-001", 18, LocalDate.now().plusMonths(6));

        mockMvc.perform(delete("/api/medicines/" + created.getId())
                        .header("Authorization", pharmacistToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/medicines/" + created.getId())
                        .header("Authorization", pharmacistToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/medicines/inactive")
                        .header("Authorization", pharmacistToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Soft Delete Medicine"));

        mockMvc.perform(patch("/api/medicines/" + created.getId() + "/restore")
                        .header("Authorization", pharmacistToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Soft Delete Medicine"));

        mockMvc.perform(get("/api/medicines/" + created.getId())
                        .header("Authorization", pharmacistToken))
                .andExpect(status().isOk());
    }

    private MedicineResponse createMedicine(String name, String barcode, int stockQuantity, LocalDate expiryDate)
            throws Exception {
        MedicineRequest request = MedicineRequest.builder()
                .name(name)
                .price(10.0)
                .stockQuantity(stockQuantity)
                .expiryDate(expiryDate)
                .barcode(barcode)
                .build();

        MvcResult result = mockMvc.perform(post("/api/medicines")
                        .header("Authorization", pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), MedicineResponse.class);
    }
}