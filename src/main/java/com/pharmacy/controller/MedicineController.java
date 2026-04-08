package com.pharmacy.controller;

import com.pharmacy.dto.MedicineRequest;
import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.List;

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

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID")
    public ResponseEntity<MedicineResponse> getMedicine(
            @Parameter(description = "Medicine ID") @PathVariable @NonNull Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
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
    @Operation(summary = "Delete medicine from inventory")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<Void> deleteMedicine(@PathVariable @NonNull Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search medicines by name")
    public ResponseEntity<List<MedicineResponse>> searchMedicines(
            @RequestParam @NonNull String name) {
        return ResponseEntity.ok(medicineService.searchMedicines(name));
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
}
