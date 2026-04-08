package com.pharmacy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected com.pharmacy.repository.UserRepository userRepository;

    @Autowired
    protected com.pharmacy.repository.MedicineRepository medicineRepository;

    @Autowired
    protected com.pharmacy.repository.CustomerOrderRepository orderRepository;

    @org.junit.jupiter.api.AfterEach
    void cleanup() {
        orderRepository.deleteAll();
        medicineRepository.deleteAll();
        userRepository.deleteAll();
    }
}
