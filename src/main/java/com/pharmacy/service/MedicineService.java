package com.pharmacy.service;

import com.pharmacy.model.Medicine;
import com.pharmacy.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found with id: " + id));
    }

    public Medicine addMedicine(Medicine medicine) {
        if (medicineRepository.existsByBatchNumber(medicine.getBatchNumber())) {
            throw new IllegalArgumentException("Batch number already exists: " + medicine.getBatchNumber());
        }
        return medicineRepository.save(medicine);
    }

    public Medicine updateMedicine(Long id, Medicine updated) {
        Medicine existing = getMedicineById(id);
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

    public void deleteMedicine(Long id) {
        getMedicineById(id); // Validate exists
        medicineRepository.deleteById(id);
    }

    public List<Medicine> searchByName(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Medicine> getByCategory(String category) {
        return medicineRepository.findByCategoryIgnoreCase(category);
    }

    public List<Medicine> getExpiringSoon(int days) {
        LocalDate cutoffDate = LocalDate.now().plusDays(days);
        return medicineRepository.findExpiringSoon(cutoffDate);
    }

    public List<Medicine> getLowStock(int threshold) {
        return medicineRepository.findLowStock(threshold);
    }

    public Medicine adjustStock(Long id, int quantity) {
        Medicine medicine = getMedicineById(id);
        int newQty = medicine.getStockQuantity() + quantity;
        if (newQty < 0) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + medicine.getStockQuantity());
        }
        medicine.setStockQuantity(newQty);
        return medicineRepository.save(medicine);
    }
}
