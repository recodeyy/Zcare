package com.pharmacy.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {

    private Long id;
    private String name;
    private String category;
    private Double price;
    private Double sellingPrice;
    private Integer stockQuantity;
    private LocalDate expiryDate;
    private String manufacturer;
    private String barcode;
    private String sku;
    private String batchNumber;
    private LocalDate manufactureDate;
    private String dosageForm;
    private String strength;
    private String genericName;
    private String brandName;
    private Boolean prescriptionRequired;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private String unitOfMeasure;
    private String rackLocation;
    private Double costPrice;
    private Double taxRate;
    private String imageUrl;
    private String activeIngredient;
    private String storageConditions;
    private Boolean isActive;
    private Boolean lowStock;
    private Boolean expiringSoon;
    private Long daysUntilExpiry;
}
