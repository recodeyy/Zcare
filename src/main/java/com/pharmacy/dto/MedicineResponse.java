package com.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {

    private Long id;
    private String name;
    private String category;
    private Double price;
    private Integer stockQuantity;
    private LocalDate expiryDate;
    private String manufacturer;
}
