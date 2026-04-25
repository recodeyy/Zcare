package com.pharmacy.dto;

import com.pharmacy.model.AdjustmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private AdjustmentType adjustmentType;
    private Integer quantityChange;
    private Integer previousStockQuantity;
    private Integer newStockQuantity;
    private String reason;
    private String referenceNumber;
    private String createdBy;
    private LocalDateTime createdAt;
    private String batchNumber;
}