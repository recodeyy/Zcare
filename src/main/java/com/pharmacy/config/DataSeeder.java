package com.pharmacy.config;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.pharmacy.model.Medicine;
import com.pharmacy.model.User;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



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
        userRepository.save(Objects.requireNonNull(admin));

        User pharmacist = User.builder()
                .username("pharmacist")
                .password(passwordEncoder.encode("pharma123"))
                .fullName("Default Pharmacist")
                .email("pharmacist@pharmacy.com")
                .role(User.Role.PHARMACIST)
                .active(true)
                .build();
        userRepository.save(Objects.requireNonNull(pharmacist));


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

        medicineRepository.save(java.util.Objects.requireNonNull(Medicine.builder()
                .name("Paracetamol 500mg")
            .category("Analgesic")
            .manufacturer("Sun Pharma")
                .price(5.50)
            .sellingPrice(5.50)
                .stockQuantity(200).expiryDate(LocalDate.now().plusMonths(18))
            .barcode("PARA-500-001")
            .batchNumber("BATCH-PARA-001")
            .imageUrl("https://example.com/medicine/paracetamol.png")
                .build()));

        medicineRepository.save(java.util.Objects.requireNonNull(Medicine.builder()
                .name("Amoxicillin 250mg")
            .category("Antibiotic")
            .manufacturer("Cipla")
                .price(12.00)
            .sellingPrice(12.00)
                .stockQuantity(150).expiryDate(LocalDate.now().plusMonths(12))
            .barcode("AMOX-250-001")
            .batchNumber("BATCH-AMOX-001")
                .build()));

        medicineRepository.save(java.util.Objects.requireNonNull(Medicine.builder()
                .name("Metformin 500mg")
            .category("Antidiabetic")
            .manufacturer("Dr. Reddy's")
                .price(8.00)
            .sellingPrice(8.00)
                .stockQuantity(8).expiryDate(LocalDate.now().plusMonths(24))
            .barcode("METF-500-001")
            .batchNumber("BATCH-METF-001")
                .build()));

        medicineRepository.save(java.util.Objects.requireNonNull(Medicine.builder()
                .name("Cetirizine 10mg")
            .category("Antihistamine")
            .manufacturer("Mankind")
                .price(3.50)
            .sellingPrice(3.50)
                .stockQuantity(300).expiryDate(LocalDate.now().plusDays(20))
            .barcode("CETI-10-001")
            .batchNumber("BATCH-CETI-001")
                .build()));


        log.info("Sample medicines seeded successfully.");
    }
}
