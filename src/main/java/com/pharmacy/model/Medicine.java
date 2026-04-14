package com.pharmacy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medicines", indexes = {
    @Index(name = "idx_medicine_barcode", columnList = "barcode"),
    @Index(name = "idx_medicine_name", columnList = "name"),
    @Index(name = "idx_medicine_category", columnList = "category"),
    @Index(name = "idx_medicine_expiry_date", columnList = "expiry_date"),
    @Index(name = "idx_medicine_batch_number", columnList = "batch_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stockQuantity;

    private LocalDate expiryDate;

    private String manufacturer;

    @Column(unique = true, length = 50)
    private String barcode;

    @Column(length = 50)
    private String sku;

    @Column(length = 100)
    private String batchNumber;

    private LocalDate manufactureDate;

    @Column(length = 50)
    private String dosageForm;

    @Column(length = 50)
    private String strength;

    @Column(length = 200)
    private String genericName;

    @Column(length = 200)
    private String brandName;

    @Builder.Default
    private Boolean prescriptionRequired = false;

    @Builder.Default
    private Integer minStockLevel = 10;

    @Builder.Default
    private Integer maxStockLevel = 1000;

    @Column(length = 50)
    private String unitOfMeasure;

    @Column(length = 200)
    private String rackLocation;

    private Double costPrice;

    private Double sellingPrice;

    @Builder.Default
    private Double taxRate = 0.0;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String activeIngredient;

    @Column(length = 500)
    private String storageConditions;

    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
