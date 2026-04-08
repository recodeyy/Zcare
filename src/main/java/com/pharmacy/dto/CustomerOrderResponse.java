package com.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderResponse {

    private Long id;
    private LocalDateTime orderDate;
    private String createdBy;
    private Double totalAmount;
    private List<OrderItemResponse> items;
}
