package com.pharmacy;

import com.pharmacy.dto.*;
import com.pharmacy.model.User;
import com.pharmacy.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
public class BillingIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private MedicineRepository medicineRepository;

        private String pharmacistToken;
        private Long med1Id;
        private Long med2Id;

        @BeforeEach
        void setUp() throws Exception {
                // 1. Setup Pharmacist
                RegisterRequest registerRequest = RegisterRequest.builder()
                                .username("billing_user")
                                .fullName("Billing Specialist")
                                .email("billing@example.com")
                                .password("password123")
                                .role(User.Role.PHARMACIST)
                                .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                AuthRequest authRequest = new AuthRequest("billing_user", "password123");
                MvcResult authResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                AuthResponse authResponse = objectMapper.readValue(authResult.getResponse().getContentAsString(),
                                AuthResponse.class);
                pharmacistToken = "Bearer " + authResponse.getToken();

                // 2. Setup Medicines
                MedicineRequest m1 = MedicineRequest.builder()
                                .name("Med A")
                                .price(10.0)
                                .stockQuantity(50)
                                .build();
                MedicineRequest m2 = MedicineRequest.builder()
                                .name("Med B")
                                .price(20.0)
                                .stockQuantity(10)
                                .build();

                MvcResult r1 = mockMvc.perform(post("/api/medicines")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(m1)))
                                .andExpect(status().isOk())
                                .andReturn();
                MvcResult r2 = mockMvc.perform(post("/api/medicines")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(m2)))
                                .andExpect(status().isOk())
                                .andReturn();

                med1Id = objectMapper.readValue(r1.getResponse().getContentAsString(), MedicineResponse.class).getId();
                med2Id = objectMapper.readValue(r2.getResponse().getContentAsString(), MedicineResponse.class).getId();
        }

        @Test
        void shouldCreateOrderAndReduceStock() throws Exception {
                OrderItemRequest item1 = new OrderItemRequest(med1Id, 2);
                OrderItemRequest item2 = new OrderItemRequest(med2Id, 1);
                List<OrderItemRequest> orderItems = List.of(item1, item2);

                MvcResult result = mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isOk())
                                .andReturn();

                CustomerOrderResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                                CustomerOrderResponse.class);

                // Verify total: (10.0 * 2) + (20.0 * 1) = 40.0
                assertThat(response.getTotalAmount()).isEqualTo(40.0);
                assertThat(response.getItems()).hasSize(2);

                // Verify stock reduction
                assertThat(medicineRepository.findById(med1Id).get().getStockQuantity()).isEqualTo(48);
                assertThat(medicineRepository.findById(med2Id).get().getStockQuantity()).isEqualTo(9);
        }

        @Test
        void shouldFailWhenInsufficientStock() throws Exception {
                OrderItemRequest item = new OrderItemRequest(med2Id, 11); // med2 only has 10
                List<OrderItemRequest> orderItems = List.of(item);

                mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isConflict());
        }

        @Test
        void shouldFailWithNegativeQuantity() throws Exception {
                OrderItemRequest item = new OrderItemRequest(med1Id, -5);
                List<OrderItemRequest> orderItems = List.of(item);

                mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldFailWithZeroQuantity() throws Exception {
                OrderItemRequest item = new OrderItemRequest(med1Id, 0);
                List<OrderItemRequest> orderItems = List.of(item);

                mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldFailWithNonExistentMedicineId() throws Exception {
                OrderItemRequest item = new OrderItemRequest(99999L, 1); // Non-existent ID
                List<OrderItemRequest> orderItems = List.of(item);

                mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldFailWithEmptyOrderList() throws Exception {
                List<OrderItemRequest> orderItems = List.of(); // Empty list

                mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldFailWithNullMedicineId() throws Exception {
                OrderItemRequest item = new OrderItemRequest(null, 1);
                List<OrderItemRequest> orderItems = List.of(item);

                mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldFailWithNullQuantity() throws Exception {
                OrderItemRequest item = new OrderItemRequest(med1Id, null);
                List<OrderItemRequest> orderItems = List.of(item);

                mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldHandleConcurrentOrdersWithConflict() throws Exception {
                // med2 has 10 items. Try to place two orders of 6 items each concurrently.
                // One should succeed, one should fail with 409 Conflict
                OrderItemRequest item = new OrderItemRequest(med2Id, 6);
                List<OrderItemRequest> orderItems = List.of(item);

                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger conflictCount = new AtomicInteger(0);
                CountDownLatch latch = new CountDownLatch(2);

                Runnable task = () -> {
                        try {
                                MvcResult result = mockMvc.perform(post("/api/orders")
                                                .header("Authorization", pharmacistToken)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(orderItems)))
                                                .andReturn();

                                int status = result.getResponse().getStatus();
                                if (status == 200) {
                                        successCount.incrementAndGet();
                                } else if (status == 409) {
                                        conflictCount.incrementAndGet();
                                }
                        } catch (Exception e) {
                                e.printStackTrace();
                        } finally {
                                latch.countDown();
                        }
                };

                Thread t1 = new Thread(task);
                Thread t2 = new Thread(task);

                t1.start();
                t2.start();
                latch.await();

                // Verify: one should succeed, one should fail with 409
                assertThat(successCount.get()).isEqualTo(1);
                assertThat(conflictCount.get()).isEqualTo(1);

                // Verify stock is consistent (should be 4, not negative)
                int remainingStock = medicineRepository.findById(med2Id).get().getStockQuantity();
                assertThat(remainingStock).isEqualTo(4);
        }

        @Test
        void shouldRollbackTransactionOnPartialFailure() throws Exception {
                // Create a scenario where one item is valid and one is invalid
                // The entire transaction should rollback
                OrderItemRequest validItem = new OrderItemRequest(med1Id, 2);
                OrderItemRequest invalidItem = new OrderItemRequest(99999L, 1); // Non-existent
                List<OrderItemRequest> orderItems = List.of(validItem, invalidItem);

                mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isNotFound());

                // Verify stock was NOT reduced (transaction rolled back)
                // Need to check in a new transaction
                assertThat(medicineRepository.findById(med1Id).get().getStockQuantity()).isEqualTo(50);
        }

        @Test
        void shouldFailWithoutJwtToken() throws Exception {
                OrderItemRequest item = new OrderItemRequest(med1Id, 1);
                List<OrderItemRequest> orderItems = List.of(item);

                mockMvc.perform(post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldFailWithInvalidJwtToken() throws Exception {
                OrderItemRequest item = new OrderItemRequest(med1Id, 1);
                List<OrderItemRequest> orderItems = List.of(item);

                mockMvc.perform(post("/api/orders")
                                .header("Authorization", "Bearer invalid.token.here")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldFailWithExpiredJwtToken() throws Exception {
                // Create an expired token (this is a mock - in real scenario you'd use a truly expired token)
                OrderItemRequest item = new OrderItemRequest(med1Id, 1);
                List<OrderItemRequest> orderItems = List.of(item);

                // Using a malformed token that will fail validation
                mockMvc.perform(post("/api/orders")
                                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldFailWithWrongRole() throws Exception {
                // Register a customer user
                RegisterRequest registerRequest = RegisterRequest.builder()
                                .username("customer_user")
                                .fullName("Customer User")
                                .email("customer@example.com")
                                .password("password123")
                                .role(User.Role.CUSTOMER)
                                .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                AuthRequest authRequest = new AuthRequest("customer_user", "password123");
                MvcResult authResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                AuthResponse authResponse = objectMapper.readValue(authResult.getResponse().getContentAsString(),
                                AuthResponse.class);
                String customerToken = "Bearer " + authResponse.getToken();

                OrderItemRequest item = new OrderItemRequest(med1Id, 1);
                List<OrderItemRequest> orderItems = List.of(item);

                // Customer should not be able to create orders (requires PHARMACIST role)
                mockMvc.perform(post("/api/orders")
                                .header("Authorization", customerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void shouldCompleteFullIntegrationFlow() throws Exception {
                // Step 1: Register and Login (already done in setUp)
                // Step 2: Create medicine (already done in setUp)
                // Step 3: Create order
                OrderItemRequest item1 = new OrderItemRequest(med1Id, 3);
                OrderItemRequest item2 = new OrderItemRequest(med2Id, 2);
                List<OrderItemRequest> orderItems = List.of(item1, item2);

                MvcResult result = mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isOk())
                                .andReturn();

                CustomerOrderResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                                CustomerOrderResponse.class);

                // Verify order created
                assertThat(response.getId()).isNotNull();
                assertThat(response.getCreatedBy()).isEqualTo("billing_user");
                assertThat(response.getItems()).hasSize(2);

                // Verify total: (10.0 * 3) + (20.0 * 2) = 70.0
                assertThat(response.getTotalAmount()).isEqualTo(70.0);

                // Verify stock reduced
                assertThat(medicineRepository.findById(med1Id).get().getStockQuantity()).isEqualTo(47);
                assertThat(medicineRepository.findById(med2Id).get().getStockQuantity()).isEqualTo(8);
        }

        @Test
        void shouldHandleMultipleItemsInSingleOrder() throws Exception {
                // Test with multiple items to ensure proper calculation
                OrderItemRequest item1 = new OrderItemRequest(med1Id, 5);
                OrderItemRequest item2 = new OrderItemRequest(med2Id, 3);
                List<OrderItemRequest> orderItems = List.of(item1, item2);

                MvcResult result = mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isOk())
                                .andReturn();

                CustomerOrderResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                                CustomerOrderResponse.class);

                // Verify total: (10.0 * 5) + (20.0 * 3) = 110.0
                assertThat(response.getTotalAmount()).isEqualTo(110.0);
                assertThat(response.getItems()).hasSize(2);

                // Verify stock reduction
                assertThat(medicineRepository.findById(med1Id).get().getStockQuantity()).isEqualTo(45);
                assertThat(medicineRepository.findById(med2Id).get().getStockQuantity()).isEqualTo(7);
        }

        @Test
        void shouldFetchAllOrdersAndById() throws Exception {
                OrderItemRequest item = new OrderItemRequest(med1Id, 2);
                List<OrderItemRequest> orderItems = List.of(item);

                MvcResult create = mockMvc.perform(post("/api/orders")
                                .header("Authorization", pharmacistToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItems)))
                                .andExpect(status().isOk())
                                .andReturn();

                CustomerOrderResponse created = objectMapper.readValue(create.getResponse().getContentAsString(),
                                CustomerOrderResponse.class);

                mockMvc.perform(get("/api/orders")
                                .header("Authorization", pharmacistToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));

                mockMvc.perform(get("/api/orders/" + created.getId())
                                .header("Authorization", pharmacistToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(created.getId()));
        }

        @Test
        void shouldReturn404WhenOrderNotFound() throws Exception {
                mockMvc.perform(get("/api/orders/999999999")
                                .header("Authorization", pharmacistToken))
                                .andExpect(status().isNotFound());
        }
}
