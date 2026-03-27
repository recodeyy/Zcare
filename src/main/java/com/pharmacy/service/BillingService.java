package com.pharmacy.service;

import com.pharmacy.dto.CustomerOrderResponse;
import com.pharmacy.dto.OrderItemRequest;
import com.pharmacy.dto.OrderItemResponse;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.model.CustomerOrder;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.OrderItem;
import com.pharmacy.repository.CustomerOrderRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MedicineRepository medicineRepository;

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

            Medicine medicine = medicineRepository.findById(request.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Medicine not found with id: " + request.getMedicineId()));

            if (request.getQuantity() > medicine.getStockQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for medicine: " + medicine.getName());
            }

            double itemTotal = medicine.getPrice() * request.getQuantity();
            medicine.setStockQuantity(medicine.getStockQuantity() - request.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .medicine(medicine)
                    .quantity(request.getQuantity())
                    .price(itemTotal)
                    .build();

            order.getItems().add(orderItem);
            totalAmount += itemTotal;
        }

        order.setTotalAmount(totalAmount);
        CustomerOrder savedOrder = customerOrderRepository.save(order);

        return toResponse(savedOrder);
    }

    private CustomerOrderResponse toResponse(CustomerOrder order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .medicineId(item.getMedicine().getId())
                        .medicineName(item.getMedicine().getName())
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
