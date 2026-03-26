package com.pharmacy.controller;

import com.pharmacy.model.Medicine;
import com.pharmacy.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.Map;




@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicine Inventory", description = "Manage medicine stock and inventory")
@SecurityRequirement(name = "bearerAuth")
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @Operation(summary = "Get medicines (Paginated)")
    public ResponseEntity<Page<Medicine>> getAllMedicines(@PageableDefault(size = 10, sort = "id") @NonNull Pageable pageable) {
        return ResponseEntity.ok(medicineService.getAllMedicines(pageable));
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID")
    public ResponseEntity<Medicine> getMedicine(
            @Parameter(description = "Medicine ID") @PathVariable @NonNull Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }


    @PostMapping
    @Operation(summary = "Add new medicine to inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Medicine> addMedicine(@Valid @RequestBody @NonNull Medicine medicine) {
        return ResponseEntity.ok(medicineService.addMedicine(medicine));
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update medicine details")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Medicine> updateMedicine(@PathVariable @NonNull Long id,
                                                    @Valid @RequestBody @NonNull Medicine medicine) {
        return ResponseEntity.ok(medicineService.updateMedicine(id, medicine));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medicine from inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMedicine(@PathVariable @NonNull Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(Map.of("message", "Medicine deleted successfully"));
    }


    @GetMapping("/search")
    @Operation(summary = "Search medicines by name (Paginated)")
    public ResponseEntity<Page<Medicine>> searchMedicines(
            @RequestParam @NonNull String name,
            @PageableDefault(size = 10) @NonNull Pageable pageable) {
        return ResponseEntity.ok(medicineService.searchByName(name, pageable));
    }


    @GetMapping("/category/{category}")
    @Operation(summary = "Get medicines by category (Paginated)")
    public ResponseEntity<Page<Medicine>> getByCategory(
            @PathVariable @NonNull String category,
            @PageableDefault(size = 10) @NonNull Pageable pageable) {
        return ResponseEntity.ok(medicineService.getByCategory(category, pageable));
    }


    @GetMapping("/expiring-soon")
    @Operation(summary = "Get medicines expiring soon (Paginated)")
    public ResponseEntity<Page<Medicine>> getExpiringSoon(
            @RequestParam(defaultValue = "30") int days,
            @PageableDefault(size = 10, sort = "expiryDate") @NonNull Pageable pageable) {
        return ResponseEntity.ok(medicineService.getExpiringSoon(days, pageable));
    }


    @GetMapping("/low-stock")
    @Operation(summary = "Get medicines with low stock (Paginated)")
    public ResponseEntity<Page<Medicine>> getLowStock(
            @RequestParam(defaultValue = "10") int threshold,
            @PageableDefault(size = 10, sort = "stockQuantity") @NonNull Pageable pageable) {
        return ResponseEntity.ok(medicineService.getLowStock(threshold, pageable));
    }


    @PatchMapping("/{id}/stock")
    @Operation(summary = "Adjust medicine stock quantity",
               description = "Use positive number to add stock, negative to reduce")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Medicine> adjustStock(@PathVariable @NonNull Long id,
                                                 @RequestParam int quantity) {
        return ResponseEntity.ok(medicineService.adjustStock(id, quantity));
    }

}
