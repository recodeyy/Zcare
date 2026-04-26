package com.pharmacy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.dto.CustomerOrderResponse;
import com.pharmacy.dto.OrderItemRequest;
import com.pharmacy.dto.OrderItemResponse;
import com.pharmacy.exception.InsufficientStockException;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.model.CustomerOrder;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.OrderItem;
import com.pharmacy.repository.CustomerOrderRepository;
import com.pharmacy.repository.MedicineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final CustomerOrderRepository customerOrderRepository;
    private final MedicineRepository medicineRepository;
    private final StockAdjustmentService stockAdjustmentService;

    @Transactional
    public CustomerOrderResponse createOrder(List<OrderItemRequest> items, String createdBy) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        CustomerOrder order = CustomerOrder.builder()
                .createdBy(createdBy)
                .build();

        double totalAmount = 0.0;

        for (OrderItemRequest request : items) {
            if (request.getMedicineId() == null) {
                throw new IllegalArgumentException("Medicine id is required");
            }
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

                Medicine medicine = medicineRepository.findActiveForUpdate(request.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Medicine not found with id: " + request.getMedicineId()));

            if (request.getQuantity() > medicine.getStockQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for medicine: " + medicine.getName());
            }

                double unitPrice = medicine.getSellingPrice() != null ? medicine.getSellingPrice() : medicine.getPrice();
                double itemTotal = unitPrice * request.getQuantity();
            medicine.setStockQuantity(medicine.getStockQuantity() - request.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .medicine(medicine)
                    .quantity(request.getQuantity())
                    .price(itemTotal)
                    .batchNumber(request.getBatchNumber() != null ? request.getBatchNumber() : medicine.getBatchNumber())
                    .build();

            order.getItems().add(orderItem);
            totalAmount += itemTotal;
        }

        order.setTotalAmount(totalAmount);
        CustomerOrder savedOrder = customerOrderRepository.save(order);

        savedOrder.getItems().forEach(item -> stockAdjustmentService.recordSaleAdjustment(
            item.getMedicine(),
            item.getQuantity(),
            createdBy,
            String.valueOf(savedOrder.getId()),
            item.getBatchNumber()
        ));

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderResponse> getAllOrders() {
        return customerOrderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerOrderResponse getOrderById(Long id) {
        CustomerOrder order = customerOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return toResponse(order);
    }

    private CustomerOrderResponse toResponse(CustomerOrder order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .medicineId(item.getMedicine().getId())
                        .medicineName(item.getMedicine().getName())
                        .batchNumber(item.getBatchNumber())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getPrice())
                        .build())
                .toList();

        return CustomerOrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .createdBy(order.getCreatedBy())
                .totalAmount(order.getTotalAmount())
                .items(itemResponses)
                .build();
    }
}
