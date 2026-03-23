package com.pharmacy.config;

import com.pharmacy.model.Medicine;
import com.pharmacy.model.User;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedSampleMedicines();
    }

    private void seedAdminUser() {
        if (userRepository.existsByUsername("admin")) {
            log.info("Admin user already exists — skipping seed.");
            return;
        }

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .email("admin@pharmacy.com")
                .role(User.Role.ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);

        User pharmacist = User.builder()
                .username("pharmacist")
                .password(passwordEncoder.encode("pharma123"))
                .fullName("Default Pharmacist")
                .email("pharmacist@pharmacy.com")
                .role(User.Role.PHARMACIST)
                .active(true)
                .build();
        userRepository.save(pharmacist);

        log.info("========================================");
        log.info("  DEFAULT USERS CREATED:");
        log.info("  Admin     -> admin / admin123");
        log.info("  Pharmacist-> pharmacist / pharma123");
        log.info("  CHANGE PASSWORDS IN PRODUCTION!");
        log.info("========================================");
    }

    private void seedSampleMedicines() {
        if (medicineRepository.count() > 0) {
            log.info("Medicines already seeded — skipping.");
            return;
        }

        medicineRepository.save(Medicine.builder()
                .name("Paracetamol 500mg").genericName("Acetaminophen")
                .category("Analgesic").manufacturer("Sun Pharma")
                .batchNumber("BATCH-001").price(new BigDecimal("5.50"))
                .stockQuantity(200).expiryDate(LocalDate.now().plusMonths(18))
                .unit("tablet").description("Pain reliever and fever reducer")
                .build());

        medicineRepository.save(Medicine.builder()
                .name("Amoxicillin 250mg").genericName("Amoxicillin")
                .category("Antibiotic").manufacturer("Cipla")
                .batchNumber("BATCH-002").price(new BigDecimal("12.00"))
                .stockQuantity(150).expiryDate(LocalDate.now().plusMonths(12))
                .unit("capsule").description("Broad-spectrum antibiotic")
                .build());

        medicineRepository.save(Medicine.builder()
                .name("Metformin 500mg").genericName("Metformin HCl")
                .category("Antidiabetic").manufacturer("Dr. Reddy's")
                .batchNumber("BATCH-003").price(new BigDecimal("8.00"))
                .stockQuantity(8).expiryDate(LocalDate.now().plusMonths(24))
                .unit("tablet").description("Type 2 diabetes management")
                .build());

        medicineRepository.save(Medicine.builder()
                .name("Cetirizine 10mg").genericName("Cetirizine HCl")
                .category("Antihistamine").manufacturer("Mankind")
                .batchNumber("BATCH-004").price(new BigDecimal("3.50"))
                .stockQuantity(300).expiryDate(LocalDate.now().plusDays(20))
                .unit("tablet").description("Allergy relief")
                .build());

        log.info("Sample medicines seeded successfully.");
    }
}
