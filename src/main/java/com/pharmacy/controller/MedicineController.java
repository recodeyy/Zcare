package com.pharmacy.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.dto.MedicineRequest;
import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.service.MedicineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicine Inventory", description = "Manage medicine stock and inventory")
@SecurityRequirement(name = "bearerAuth")
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @Operation(summary = "Get all medicines")
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {
        return ResponseEntity.ok(medicineService.getAllMedicines());
    }

    @GetMapping("/page")
    @Operation(summary = "Get paginated medicines")
    public ResponseEntity<Page<MedicineResponse>> getMedicinesPage(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(medicineService.getMedicines(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID")
    public ResponseEntity<MedicineResponse> getMedicine(
            @Parameter(description = "Medicine ID") @PathVariable @NonNull Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get medicine by barcode")
    public ResponseEntity<MedicineResponse> getMedicineByBarcode(
            @Parameter(description = "Medicine barcode") @PathVariable @NonNull String barcode) {
        return ResponseEntity.ok(medicineService.getMedicineByBarcode(barcode));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get medicines by category")
    public ResponseEntity<List<MedicineResponse>> getMedicinesByCategory(
            @Parameter(description = "Medicine category") @PathVariable @NonNull String category) {
        return ResponseEntity.ok(medicineService.getMedicinesByCategory(category));
    }

    @GetMapping("/batch/{batchNumber}")
    @Operation(summary = "Get medicines by batch number")
    public ResponseEntity<List<MedicineResponse>> getMedicinesByBatchNumber(
            @Parameter(description = "Batch number") @PathVariable @NonNull String batchNumber) {
        return ResponseEntity.ok(medicineService.getMedicinesByBatchNumber(batchNumber));
    }

    @PostMapping
    @Operation(summary = "Add new medicine to inventory")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<MedicineResponse> addMedicine(@Valid @RequestBody @NonNull MedicineRequest medicineRequest) {
        return ResponseEntity.ok(medicineService.addMedicine(medicineRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update medicine details")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<MedicineResponse> updateMedicine(@PathVariable @NonNull Long id,
                                                            @Valid @RequestBody @NonNull MedicineRequest medicineRequest) {
        return ResponseEntity.ok(medicineService.updateMedicine(id, medicineRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete medicine from inventory")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<Void> deleteMedicine(@PathVariable @NonNull Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore a soft-deleted medicine")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<MedicineResponse> restoreMedicine(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(medicineService.restoreMedicine(id));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a medicine")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<Void> deactivateMedicine(@PathVariable @NonNull Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search medicines by name")
    public ResponseEntity<List<MedicineResponse>> searchMedicines(
            @RequestParam @NonNull String name) {
        return ResponseEntity.ok(medicineService.searchMedicines(name));
    }

    @GetMapping("/expiring-soon")
    @Operation(summary = "Get medicines expiring within a configurable number of days")
    public ResponseEntity<List<MedicineResponse>> getExpiringSoon(@RequestParam(defaultValue = "30") Integer days) {
        return ResponseEntity.ok(medicineService.getExpiringSoonMedicines(days));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock medicines")
    public ResponseEntity<List<MedicineResponse>> getLowStock(@RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(medicineService.getLowStockMedicines(threshold));
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired medicines")
    public ResponseEntity<List<MedicineResponse>> getExpired() {
        return ResponseEntity.ok(medicineService.getExpiredMedicines());
    }

    @GetMapping("/inactive")
    @Operation(summary = "Get soft-deleted medicines")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<List<MedicineResponse>> getInactiveMedicines() {
        return ResponseEntity.ok(medicineService.getInactiveMedicines());
    }
}
