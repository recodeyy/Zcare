package com.pharmacy.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.dto.StockAdjustmentRequest;
import com.pharmacy.dto.StockAdjustmentResponse;
import com.pharmacy.exception.InsufficientStockException;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.model.AdjustmentType;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.StockAdjustment;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.StockAdjustmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockAdjustmentService {

    private final MedicineRepository medicineRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;

    @Transactional
    public StockAdjustmentResponse createAdjustment(StockAdjustmentRequest request, String createdBy) {
        Medicine medicine = medicineRepository.findActiveForUpdate(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medicine not found with id: " + request.getMedicineId()));

        return applyAdjustment(medicine,
                request.getAdjustmentType(),
                request.getQuantityChange(),
                request.getReason(),
                request.getReferenceNumber(),
                request.getBatchNumber(),
                createdBy);
    }

    @Transactional
    public StockAdjustmentResponse recordSaleAdjustment(Medicine medicine, int quantitySold, String createdBy,
            String referenceNumber, String batchNumber) {
        if (medicine == null || medicine.getId() == null) {
            throw new IllegalArgumentException("Medicine is required for stock adjustment");
        }
        if (quantitySold <= 0) {
            throw new IllegalArgumentException("Quantity sold must be greater than 0");
        }

        int newStockQuantity = medicine.getStockQuantity();
        int previousStockQuantity = newStockQuantity + quantitySold;

        StockAdjustment adjustment = StockAdjustment.builder()
                .medicine(medicine)
                .adjustmentType(AdjustmentType.SALE)
                .quantityChange(-quantitySold)
                .previousStockQuantity(previousStockQuantity)
                .newStockQuantity(newStockQuantity)
                .reason("Order sale")
                .referenceNumber(referenceNumber)
                .batchNumber(batchNumber != null ? batchNumber : medicine.getBatchNumber())
                .createdBy(normalizeAdjustedBy(createdBy))
                .build();

        return toResponse(stockAdjustmentRepository.save(adjustment));
    }

    @Transactional(readOnly = true)
    public Page<StockAdjustmentResponse> getAllAdjustments(Pageable pageable) {
        return stockAdjustmentRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<StockAdjustmentResponse> getAdjustmentsForMedicine(Long medicineId, Pageable pageable) {
        medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + medicineId));

        return stockAdjustmentRepository.findByMedicine_IdOrderByCreatedAtDesc(medicineId, pageable)
                .map(this::toResponse);
    }

    private StockAdjustmentResponse applyAdjustment(Medicine medicine,
            AdjustmentType adjustmentType,
            Integer quantityChange,
            String reason,
            String referenceNumber,
            String batchNumber,
            String createdBy) {
        if (adjustmentType == null) {
            throw new IllegalArgumentException("Adjustment type is required");
        }
        if (quantityChange == null || quantityChange == 0) {
            throw new IllegalArgumentException("Quantity change must not be zero");
        }

        int delta = resolveDelta(adjustmentType, quantityChange);
        int previousStockQuantity = medicine.getStockQuantity();
        int newStockQuantity = previousStockQuantity + delta;

        if (newStockQuantity < 0) {
            throw new InsufficientStockException("Insufficient stock for medicine: " + medicine.getName());
        }

        medicine.setStockQuantity(newStockQuantity);

        StockAdjustment adjustment = StockAdjustment.builder()
                .medicine(medicine)
                .adjustmentType(adjustmentType)
                .quantityChange(delta)
                .previousStockQuantity(previousStockQuantity)
                .newStockQuantity(newStockQuantity)
                .reason(reason != null && !reason.isBlank() ? reason : adjustmentType.name())
                .referenceNumber(referenceNumber)
                .batchNumber(batchNumber)
                .createdBy(normalizeAdjustedBy(createdBy))
                .build();

        return toResponse(stockAdjustmentRepository.save(adjustment));
    }

    private int resolveDelta(AdjustmentType adjustmentType, Integer quantityChange) {
        int absoluteQuantity = Math.abs(quantityChange);
        return switch (adjustmentType) {
            case SALE, DAMAGE, WRITE_OFF, EXPIRY -> -absoluteQuantity;
            case PURCHASE, RETURN -> absoluteQuantity;
            case MANUAL_ADJUSTMENT -> quantityChange;
        };
    }

    private String normalizeAdjustedBy(String adjustedBy) {
        return adjustedBy == null || adjustedBy.isBlank() ? "system" : adjustedBy;
    }

    private StockAdjustmentResponse toResponse(StockAdjustment adjustment) {
        return StockAdjustmentResponse.builder()
                .id(adjustment.getId())
                .medicineId(adjustment.getMedicine().getId())
                .medicineName(adjustment.getMedicine().getName())
                .adjustmentType(adjustment.getAdjustmentType())
                .quantityChange(adjustment.getQuantityChange())
                .previousStockQuantity(adjustment.getPreviousStockQuantity())
                .newStockQuantity(adjustment.getNewStockQuantity())
                .reason(adjustment.getReason())
                .referenceNumber(adjustment.getReferenceNumber())
                .batchNumber(adjustment.getBatchNumber())
                .createdBy(adjustment.getCreatedBy())
                .createdAt(adjustment.getCreatedAt())
                .build();
    }
}