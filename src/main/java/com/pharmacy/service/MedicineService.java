package com.pharmacy.service;

import com.pharmacy.model.Medicine;
import com.pharmacy.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.util.Objects;





@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public Page<Medicine> getAllMedicines(@NonNull Pageable pageable) {

        log.debug("Fetching medicines: {}", pageable);
        return medicineRepository.findAll(pageable);
    }

    public Medicine getMedicineById(@NonNull Long id) {

        return medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found with id: " + id));
    }

    public Medicine addMedicine(@NonNull Medicine medicine) {
        log.info("Adding new medicine: {} (Batch: {})", medicine.getName(), medicine.getBatchNumber());
        if (medicineRepository.existsByBatchNumber(medicine.getBatchNumber())) {
            throw new IllegalArgumentException("Batch number already exists: " + medicine.getBatchNumber());
        }
        return medicineRepository.save(Objects.requireNonNull(medicine));
    }

    @Transactional
    public Medicine updateMedicine(@NonNull Long id, @NonNull Medicine updated) {
        log.info("Updating medicine ID: {}", id);
        Medicine existing = getMedicineById(id);


        // Check if batch number is changing and if it conflicts with another record
        if (updated.getBatchNumber() != null && !updated.getBatchNumber().equals(existing.getBatchNumber())) {
            medicineRepository.findByBatchNumber(updated.getBatchNumber()).ifPresent(m -> {
                if (!m.getId().equals(id)) {
                    throw new IllegalArgumentException("Batch number " + updated.getBatchNumber() + " is already used by another medicine.");
                }
            });
            existing.setBatchNumber(updated.getBatchNumber());
        }

        existing.setName(updated.getName());
        existing.setGenericName(updated.getGenericName());
        existing.setCategory(updated.getCategory());
        existing.setManufacturer(updated.getManufacturer());
        existing.setPrice(updated.getPrice());
        existing.setStockQuantity(updated.getStockQuantity());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setDescription(updated.getDescription());
        existing.setUnit(updated.getUnit());

        return medicineRepository.save(existing);
    }

    public void deleteMedicine(@NonNull Long id) {
        log.warn("Deleting medicine ID: {}", id);
        getMedicineById(id); // Validate exists
        medicineRepository.deleteById(id);
    }


    public Page<Medicine> searchByName(String name, Pageable pageable) {
        log.debug("Searching medicines by name: {} with {}", name, pageable);
        return medicineRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Page<Medicine> getByCategory(String category, Pageable pageable) {
        log.debug("Filtering medicines by category: {} with {}", category, pageable);
        return medicineRepository.findByCategoryIgnoreCase(category, pageable);
    }

    public Page<Medicine> getExpiringSoon(int days, Pageable pageable) {
        LocalDate cutoffDate = LocalDate.now().plusDays(days);
        log.debug("Fetching expiring medicines (before {}) with {}", cutoffDate, pageable);
        return medicineRepository.findExpiringSoon(cutoffDate, pageable);
    }

    public Page<Medicine> getLowStock(int threshold, Pageable pageable) {
        log.debug("Fetching low-stock medicines (below {}) with {}", threshold, pageable);
        return medicineRepository.findLowStock(threshold, pageable);
    }

    @Transactional
    public Medicine adjustStock(@NonNull Long id, int quantity) {
        log.info("Adjusting stock for ID {}: quantity flux {}", id, quantity);
        Medicine medicine = getMedicineById(id);
        int newQty = medicine.getStockQuantity() + quantity;
        if (newQty < 0) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + medicine.getStockQuantity());
        }
        medicine.setStockQuantity(newQty);
        return medicineRepository.save(Objects.requireNonNull(medicine));
    }

}
