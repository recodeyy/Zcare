package com.pharmacy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustments", indexes = {
        @Index(name = "idx_stock_adjustment_medicine", columnList = "medicine_id"),
        @Index(name = "idx_stock_adjustment_type", columnList = "adjustment_type"),
        @Index(name = "idx_stock_adjustment_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 50)
    private AdjustmentType adjustmentType;

    @Column(nullable = false)
    private Integer quantityChange;

    @Column(nullable = false)
    private Integer previousStockQuantity;

    @Column(nullable = false)
    private Integer newStockQuantity;

    @Column(length = 500)
    private String reason;

    @Column(length = 100)
    private String referenceNumber;

    @Column(length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 100)
    private String batchNumber;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}