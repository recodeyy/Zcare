package com.pharmacy.controller;

import com.pharmacy.model.Medicine;
import com.pharmacy.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicine Inventory", description = "Manage medicine stock and inventory")
@SecurityRequirement(name = "bearerAuth")
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @Operation(summary = "Get all medicines", description = "Returns full inventory list")
    public ResponseEntity<List<Medicine>> getAllMedicines() {
        return ResponseEntity.ok(medicineService.getAllMedicines());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID")
    public ResponseEntity<Medicine> getMedicine(
            @Parameter(description = "Medicine ID") @PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    @PostMapping
    @Operation(summary = "Add new medicine to inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Medicine> addMedicine(@RequestBody Medicine medicine) {
        return ResponseEntity.ok(medicineService.addMedicine(medicine));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update medicine details")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Medicine> updateMedicine(@PathVariable Long id,
                                                    @RequestBody Medicine medicine) {
        return ResponseEntity.ok(medicineService.updateMedicine(id, medicine));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medicine from inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(Map.of("message", "Medicine deleted successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search medicines by name")
    public ResponseEntity<List<Medicine>> searchMedicines(
            @Parameter(description = "Search keyword") @RequestParam String name) {
        return ResponseEntity.ok(medicineService.searchByName(name));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get medicines by category")
    public ResponseEntity<List<Medicine>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(medicineService.getByCategory(category));
    }

    @GetMapping("/expiring-soon")
    @Operation(summary = "Get medicines expiring soon",
               description = "Default: medicines expiring within 30 days")
    public ResponseEntity<List<Medicine>> getExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(medicineService.getExpiringSoon(days));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get medicines with low stock",
               description = "Default threshold: 10 units")
    public ResponseEntity<List<Medicine>> getLowStock(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(medicineService.getLowStock(threshold));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Adjust medicine stock quantity",
               description = "Use positive number to add stock, negative to reduce")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Medicine> adjustStock(@PathVariable Long id,
                                                 @RequestParam int quantity) {
        return ResponseEntity.ok(medicineService.adjustStock(id, quantity));
    }
}
