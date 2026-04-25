package com.pharmacy.service;

import com.pharmacy.dto.MedicineRequest;
import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.exception.ResourceNotFoundException;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineService {

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    private static final int DEFAULT_EXPIRING_SOON_DAYS = 30;

    private final MedicineRepository medicineRepository;

    @Transactional(readOnly = true)
    public List<MedicineResponse> getAllMedicines() {
        log.debug("Fetching all medicines");
        return medicineRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MedicineResponse> getMedicines(Pageable pageable) {
        return medicineRepository.findByIsActiveTrue(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MedicineResponse getMedicineById(@NonNull Long id) {
        Medicine medicine = medicineRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        return toResponse(medicine);
    }

    @Transactional(readOnly = true)
    public MedicineResponse getMedicineByBarcode(@NonNull String barcode) {
        Medicine medicine = medicineRepository.findByBarcodeAndIsActiveTrue(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with barcode: " + barcode));
        return toResponse(medicine);
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getMedicinesByCategory(@NonNull String category) {
        return medicineRepository.findByCategoryIgnoreCaseAndIsActiveTrue(category).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getMedicinesByBatchNumber(@NonNull String batchNumber) {
        return medicineRepository.findByBatchNumberIgnoreCaseAndIsActiveTrue(batchNumber).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicineResponse addMedicine(@NonNull MedicineRequest request) {
        log.info("Adding new medicine: {}", request.getName());
        Medicine medicine = toEntity(request);
        validateMedicine(medicine);
        return toResponse(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineResponse updateMedicine(@NonNull Long id, @NonNull MedicineRequest request) {
        log.info("Updating medicine ID: {}", id);
        Medicine existing = medicineRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));

        applyRequest(existing, request);
        validateMedicine(existing);

        return toResponse(medicineRepository.save(existing));
    }

    public void deleteMedicine(@NonNull Long id) {
        log.warn("Soft deleting medicine ID: {}", id);
        Medicine existing = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot delete. Medicine not found with id: " + id));
        existing.setIsActive(false);
        medicineRepository.save(existing);
    }

    @Transactional
    public MedicineResponse restoreMedicine(@NonNull Long id) {
        Medicine existing = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        existing.setIsActive(true);
        return toResponse(medicineRepository.save(existing));
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> searchMedicines(String name) {
        log.debug("Searching medicines by name: {}", name);
        return medicineRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getLowStockMedicines(Integer threshold) {
        int resolvedThreshold = threshold == null ? DEFAULT_LOW_STOCK_THRESHOLD : threshold;
        log.debug("Fetching medicines with stock below {}", resolvedThreshold);
        return medicineRepository.findByStockQuantityLessThanAndIsActiveTrue(resolvedThreshold).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getExpiredMedicines() {
        LocalDate today = LocalDate.now();
        log.debug("Fetching medicines expired before {}", today);
        return medicineRepository.findByExpiryDateBeforeAndIsActiveTrue(today).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getExpiringSoonMedicines(Integer days) {
        int resolvedDays = days == null ? DEFAULT_EXPIRING_SOON_DAYS : days;
        if (resolvedDays < 0) {
            throw new IllegalArgumentException("Days must be greater than or equal to 0");
        }

        LocalDate today = LocalDate.now();
        LocalDate cutoffDate = today.plusDays(resolvedDays);
        log.debug("Fetching medicines expiring between {} and {}", today, cutoffDate);
        return medicineRepository.findByExpiryDateBetweenAndIsActiveTrue(today, cutoffDate).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getInactiveMedicines() {
        return medicineRepository.findByIsActiveFalse().stream()
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
        if (medicine.getMinStockLevel() != null && medicine.getMinStockLevel() < 0) {
            throw new IllegalArgumentException("Minimum stock level must be greater than or equal to 0");
        }
        if (medicine.getMaxStockLevel() != null && medicine.getMaxStockLevel() < 0) {
            throw new IllegalArgumentException("Maximum stock level must be greater than or equal to 0");
        }
        if (medicine.getMinStockLevel() != null && medicine.getMaxStockLevel() != null
                && medicine.getMinStockLevel() > medicine.getMaxStockLevel()) {
            throw new IllegalArgumentException("Minimum stock level cannot exceed maximum stock level");
        }
        if (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now())) {
            log.warn("Warning: Medicine '{}' has an expiry date in the past: {}", medicine.getName(), medicine.getExpiryDate());
        }
    }

    private Medicine toEntity(MedicineRequest request) {
        Medicine medicine = Medicine.builder().build();
        applyRequest(medicine, request);
        if (medicine.getIsActive() == null) {
            medicine.setIsActive(true);
        }
        return medicine;
    }

    private void applyRequest(Medicine medicine, MedicineRequest request) {
        if (request.getName() != null) {
            medicine.setName(request.getName());
        }
        if (request.getCategory() != null) {
            medicine.setCategory(request.getCategory());
        }
        if (request.getManufacturer() != null) {
            medicine.setManufacturer(request.getManufacturer());
        }
        if (request.getBarcode() != null) {
            medicine.setBarcode(request.getBarcode());
        }
        if (request.getSku() != null) {
            medicine.setSku(request.getSku());
        }
        if (request.getBatchNumber() != null) {
            medicine.setBatchNumber(request.getBatchNumber());
        }
        if (request.getManufactureDate() != null) {
            medicine.setManufactureDate(request.getManufactureDate());
        }
        if (request.getDosageForm() != null) {
            medicine.setDosageForm(request.getDosageForm());
        }
        if (request.getStrength() != null) {
            medicine.setStrength(request.getStrength());
        }
        if (request.getGenericName() != null) {
            medicine.setGenericName(request.getGenericName());
        }
        if (request.getBrandName() != null) {
            medicine.setBrandName(request.getBrandName());
        }
        if (request.getPrescriptionRequired() != null) {
            medicine.setPrescriptionRequired(request.getPrescriptionRequired());
        }
        if (request.getMinStockLevel() != null) {
            medicine.setMinStockLevel(request.getMinStockLevel());
        }
        if (request.getMaxStockLevel() != null) {
            medicine.setMaxStockLevel(request.getMaxStockLevel());
        }
        if (request.getUnitOfMeasure() != null) {
            medicine.setUnitOfMeasure(request.getUnitOfMeasure());
        }
        if (request.getRackLocation() != null) {
            medicine.setRackLocation(request.getRackLocation());
        }
        if (request.getCostPrice() != null) {
            medicine.setCostPrice(request.getCostPrice());
        }
        if (request.getTaxRate() != null) {
            medicine.setTaxRate(request.getTaxRate());
        }
        if (request.getImageUrl() != null) {
            medicine.setImageUrl(request.getImageUrl());
        }
        if (request.getActiveIngredient() != null) {
            medicine.setActiveIngredient(request.getActiveIngredient());
        }
        if (request.getStorageConditions() != null) {
            medicine.setStorageConditions(request.getStorageConditions());
        }
        if (request.getIsActive() != null) {
            medicine.setIsActive(request.getIsActive());
        }

        if (request.getPrice() != null || request.getSellingPrice() != null) {
            Double resolvedPrice = request.getSellingPrice() != null ? request.getSellingPrice() : request.getPrice();
            medicine.setPrice(resolvedPrice);
            medicine.setSellingPrice(resolvedPrice);
        }

        if (request.getStockQuantity() != null) {
            medicine.setStockQuantity(request.getStockQuantity());
        }
        if (request.getExpiryDate() != null) {
            medicine.setExpiryDate(request.getExpiryDate());
        }
    }

    private MedicineResponse toResponse(Medicine medicine) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = medicine.getExpiryDate();
        Long daysUntilExpiry = expiryDate == null ? null : ChronoUnit.DAYS.between(today, expiryDate);
        Double resolvedPrice = medicine.getSellingPrice() != null ? medicine.getSellingPrice() : medicine.getPrice();
        Integer minStockLevel = medicine.getMinStockLevel();
        Integer stockQuantity = medicine.getStockQuantity();

        return MedicineResponse.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .category(medicine.getCategory())
                .price(resolvedPrice)
                .sellingPrice(resolvedPrice)
                .stockQuantity(stockQuantity)
                .expiryDate(expiryDate)
                .manufacturer(medicine.getManufacturer())
                .barcode(medicine.getBarcode())
                .sku(medicine.getSku())
                .batchNumber(medicine.getBatchNumber())
                .manufactureDate(medicine.getManufactureDate())
                .dosageForm(medicine.getDosageForm())
                .strength(medicine.getStrength())
                .genericName(medicine.getGenericName())
                .brandName(medicine.getBrandName())
                .prescriptionRequired(medicine.getPrescriptionRequired())
                .minStockLevel(minStockLevel)
                .maxStockLevel(medicine.getMaxStockLevel())
                .unitOfMeasure(medicine.getUnitOfMeasure())
                .rackLocation(medicine.getRackLocation())
                .costPrice(medicine.getCostPrice())
                .taxRate(medicine.getTaxRate())
                .imageUrl(medicine.getImageUrl())
                .activeIngredient(medicine.getActiveIngredient())
                .storageConditions(medicine.getStorageConditions())
                .isActive(Boolean.TRUE.equals(medicine.getIsActive()))
                .lowStock(stockQuantity != null && minStockLevel != null && stockQuantity <= minStockLevel)
                .expiringSoon(daysUntilExpiry != null && daysUntilExpiry >= 0 && daysUntilExpiry <= DEFAULT_EXPIRING_SOON_DAYS)
                .daysUntilExpiry(daysUntilExpiry)
                .build();
    }
}
