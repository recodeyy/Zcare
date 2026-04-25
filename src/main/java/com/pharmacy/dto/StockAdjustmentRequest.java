package com.pharmacy.dto;

import com.pharmacy.model.AdjustmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "Medicine id is required")
    private Long medicineId;

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType adjustmentType;

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange;

    @Size(max = 500, message = "Reason must be 500 characters or fewer")
    private String reason;

    @Size(max = 100, message = "Reference number must be 100 characters or fewer")
    private String referenceNumber;

    @Size(max = 100, message = "Batch number must be 100 characters or fewer")
    private String batchNumber;
}