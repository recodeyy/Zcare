package com.pharmacy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.dto.CustomerOrderResponse;
import com.pharmacy.dto.OrderItemRequest;
import com.pharmacy.service.BillingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<CustomerOrderResponse> createOrder(
            @RequestBody List<@Valid OrderItemRequest> items,
            Authentication authentication) {
        return ResponseEntity.ok(billingService.createOrder(items, authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<List<CustomerOrderResponse>> getAllOrders() {
        return ResponseEntity.ok(billingService.getAllOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<CustomerOrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getOrderById(id));
    }
}
