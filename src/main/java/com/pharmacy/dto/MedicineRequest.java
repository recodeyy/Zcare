package com.pharmacy.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @FutureOrPresent(message = "Expiry date cannot be in the past")
    private LocalDate expiryDate;

    private String manufacturer;
}
