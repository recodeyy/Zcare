package com.pharmacy.service;

import com.pharmacy.dto.MedicineRequest;
import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.model.Medicine;
import com.pharmacy.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public List<MedicineResponse> getAllMedicines() {
        log.debug("Fetching all medicines");
        return medicineRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MedicineResponse getMedicineById(@NonNull Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        return toResponse(medicine);
    }

    public MedicineResponse addMedicine(@NonNull MedicineRequest request) {
        log.info("Adding new medicine: {}", request.getName());
        Medicine medicine = toEntity(request);
        validateMedicine(medicine);
        return toResponse(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineResponse updateMedicine(@NonNull Long id, @NonNull MedicineRequest request) {
        log.info("Updating medicine ID: {}", id);
        Medicine existing = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));

        Medicine updated = toEntity(request);
        validateMedicine(updated);

        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setManufacturer(updated.getManufacturer());
        existing.setPrice(updated.getPrice());
        existing.setStockQuantity(updated.getStockQuantity());
        existing.setExpiryDate(updated.getExpiryDate());

        return toResponse(medicineRepository.save(existing));
    }

    public void deleteMedicine(@NonNull Long id) {
        log.warn("Deleting medicine ID: {}", id);
        if (!medicineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. Medicine not found with id: " + id);
        }
        medicineRepository.deleteById(id);
    }

    public List<MedicineResponse> searchMedicines(String name) {
        log.debug("Searching medicines by name: {}", name);
        return medicineRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MedicineResponse> getLowStockMedicines(Integer threshold) {
        log.debug("Fetching medicines with stock below {}", threshold);
        return medicineRepository.findByStockQuantityLessThan(threshold).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MedicineResponse> getExpiredMedicines() {
        LocalDate today = LocalDate.now();
        log.debug("Fetching medicines expired before {}", today);
        return medicineRepository.findByExpiryDateBefore(today).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void validateMedicine(Medicine medicine) {
        if (medicine.getPrice() == null || medicine.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (medicine.getStockQuantity() == null || medicine.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity must be greater than or equal to 0");
        }
        if (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now())) {
            log.warn("Warning: Medicine '{}' has an expiry date in the past: {}", medicine.getName(), medicine.getExpiryDate());
        }
    }

    private Medicine toEntity(MedicineRequest request) {
        return Medicine.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .expiryDate(request.getExpiryDate())
                .manufacturer(request.getManufacturer())
                .build();
    }

    private MedicineResponse toResponse(Medicine medicine) {
        return MedicineResponse.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .category(medicine.getCategory())
                .price(medicine.getPrice())
                .stockQuantity(medicine.getStockQuantity())
                .expiryDate(medicine.getExpiryDate())
                .manufacturer(medicine.getManufacturer())
                .build();
    }
}
