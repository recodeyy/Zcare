package com.pharmacy.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.dto.StockAdjustmentRequest;
import com.pharmacy.dto.StockAdjustmentResponse;
import com.pharmacy.service.StockAdjustmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stock-adjustments")
@RequiredArgsConstructor
@Tag(name = "Stock Adjustments", description = "Track stock movement history and manual stock corrections")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('PHARMACIST')")
public class StockAdjustmentController {

    private final StockAdjustmentService stockAdjustmentService;

    @PostMapping
    @Operation(summary = "Create a manual stock adjustment")
    public ResponseEntity<StockAdjustmentResponse> createAdjustment(@Valid @RequestBody StockAdjustmentRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(stockAdjustmentService.createAdjustment(request, authentication.getName()));
    }

    @GetMapping
    @Operation(summary = "Get all stock adjustments")
    public ResponseEntity<Page<StockAdjustmentResponse>> getAllAdjustments(Pageable pageable) {
        return ResponseEntity.ok(stockAdjustmentService.getAllAdjustments(pageable));
    }

    @GetMapping("/medicine/{medicineId}")
    @Operation(summary = "Get stock adjustments for a medicine")
    public ResponseEntity<Page<StockAdjustmentResponse>> getAdjustmentsByMedicine(
            @PathVariable Long medicineId,
            Pageable pageable) {
        return ResponseEntity.ok(stockAdjustmentService.getAdjustmentsForMedicine(medicineId, pageable));
    }
}