package com.pharmacy;

import com.pharmacy.dto.AuthRequest;
import com.pharmacy.dto.AuthResponse;
import com.pharmacy.dto.CustomerOrderResponse;
import com.pharmacy.dto.MedicineRequest;
import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.dto.OrderItemRequest;
import com.pharmacy.dto.RegisterRequest;
import com.pharmacy.dto.StockAdjustmentRequest;
import com.pharmacy.dto.StockAdjustmentResponse;
import com.pharmacy.model.AdjustmentType;
import com.pharmacy.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
public class StockAdjustmentIntegrationTest extends BaseIntegrationTest {

    private String pharmacistToken;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("audit_user")
                .fullName("Audit User")
                .email("audit@example.com")
                .password("password123")
                .role(User.Role.PHARMACIST)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        AuthRequest authRequest = new AuthRequest("audit_user", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        pharmacistToken = "Bearer " + authResponse.getToken();
    }

    @Test
    void shouldRecordSaleAdjustmentWhenOrderIsCreated() throws Exception {
        MedicineResponse medicine = createMedicine("Audit Sale Medicine", "AUDIT-SALE-001", 20);

        List<OrderItemRequest> orderItems = List.of(new OrderItemRequest(medicine.getId(), 3));

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItems)))
                .andExpect(status().isOk())
                .andReturn();

        CustomerOrderResponse orderResponse = objectMapper.readValue(
                orderResult.getResponse().getContentAsString(), CustomerOrderResponse.class);

        MvcResult auditResult = mockMvc.perform(get("/api/stock-adjustments")
                        .header("Authorization", pharmacistToken))
                .andExpect(status().isOk())
                .andReturn();

        StockAdjustmentResponse[] adjustments = objectMapper.readValue(
                auditResult.getResponse().getContentAsString(), StockAdjustmentResponse[].class);

        assertThat(adjustments).hasSize(1);
        assertThat(adjustments[0].getAdjustmentType()).isEqualTo(AdjustmentType.SALE);
        assertThat(adjustments[0].getQuantityChange()).isEqualTo(-3);
        assertThat(adjustments[0].getMedicineId()).isEqualTo(medicine.getId());
        assertThat(adjustments[0].getReferenceNumber()).isEqualTo(String.valueOf(orderResponse.getId()));

        MvcResult medicineResult = mockMvc.perform(get("/api/medicines/" + medicine.getId())
                        .header("Authorization", pharmacistToken))
                .andExpect(status().isOk())
                .andReturn();

        MedicineResponse updatedMedicine = objectMapper.readValue(
                medicineResult.getResponse().getContentAsString(), MedicineResponse.class);
        assertThat(updatedMedicine.getStockQuantity()).isEqualTo(17);
    }

    @Test
    void shouldCreateManualStockAdjustment() throws Exception {
        MedicineResponse medicine = createMedicine("Audit Manual Medicine", "AUDIT-MANUAL-001", 10);

        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                .medicineId(medicine.getId())
                .adjustmentType(AdjustmentType.PURCHASE)
                .quantityChange(5)
                .reason("Restock from supplier")
                .referenceNumber("GRN-001")
                .build();

        mockMvc.perform(post("/api/stock-adjustments")
                        .header("Authorization", pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        MvcResult medicineResult = mockMvc.perform(get("/api/medicines/" + medicine.getId())
                        .header("Authorization", pharmacistToken))
                .andExpect(status().isOk())
                .andReturn();

        MedicineResponse updatedMedicine = objectMapper.readValue(
                medicineResult.getResponse().getContentAsString(), MedicineResponse.class);
        assertThat(updatedMedicine.getStockQuantity()).isEqualTo(15);

        MvcResult auditResult = mockMvc.perform(get("/api/stock-adjustments/medicine/" + medicine.getId())
                        .header("Authorization", pharmacistToken))
                .andExpect(status().isOk())
                .andReturn();

        StockAdjustmentResponse[] adjustments = objectMapper.readValue(
                auditResult.getResponse().getContentAsString(), StockAdjustmentResponse[].class);

        assertThat(adjustments).hasSize(1);
        assertThat(adjustments[0].getAdjustmentType()).isEqualTo(AdjustmentType.PURCHASE);
        assertThat(adjustments[0].getQuantityChange()).isEqualTo(5);
        assertThat(adjustments[0].getNewStockQuantity()).isEqualTo(15);
    }

    private MedicineResponse createMedicine(String name, String barcode, int stockQuantity) throws Exception {
        MedicineRequest request = MedicineRequest.builder()
                .name(name)
                .price(10.0)
                .stockQuantity(stockQuantity)
                .expiryDate(LocalDate.now().plusMonths(6))
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