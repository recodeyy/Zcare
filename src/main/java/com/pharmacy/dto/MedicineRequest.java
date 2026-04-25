package com.pharmacy.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRequest {

    @NotBlank(message = "Medicine name is required")
    private String name;

    private String category;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @Positive(message = "Selling price must be positive")
    private Double sellingPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @FutureOrPresent(message = "Expiry date cannot be in the past")
    private LocalDate expiryDate;

    private String manufacturer;

    @Size(max = 50, message = "Barcode must be 50 characters or fewer")
    private String barcode;

    @Size(max = 50, message = "SKU must be 50 characters or fewer")
    private String sku;

    @Size(max = 100, message = "Batch number must be 100 characters or fewer")
    private String batchNumber;

    @PastOrPresent(message = "Manufacture date cannot be in the future")
    private LocalDate manufactureDate;

    @Size(max = 50, message = "Dosage form must be 50 characters or fewer")
    private String dosageForm;

    @Size(max = 50, message = "Strength must be 50 characters or fewer")
    private String strength;

    @Size(max = 200, message = "Generic name must be 200 characters or fewer")
    private String genericName;

    @Size(max = 200, message = "Brand name must be 200 characters or fewer")
    private String brandName;

    private Boolean prescriptionRequired;

    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minStockLevel;

    @Min(value = 0, message = "Maximum stock level cannot be negative")
    private Integer maxStockLevel;

    @Size(max = 50, message = "Unit of measure must be 50 characters or fewer")
    private String unitOfMeasure;

    @Size(max = 200, message = "Rack location must be 200 characters or fewer")
    private String rackLocation;

    @PositiveOrZero(message = "Cost price cannot be negative")
    private Double costPrice;

    @PositiveOrZero(message = "Tax rate cannot be negative")
    private Double taxRate;

    @Size(max = 500, message = "Image URL must be 500 characters or fewer")
    private String imageUrl;

    @Size(max = 500, message = "Active ingredient must be 500 characters or fewer")
    private String activeIngredient;

    @Size(max = 500, message = "Storage conditions must be 500 characters or fewer")
    private String storageConditions;

    private Boolean isActive;
}
