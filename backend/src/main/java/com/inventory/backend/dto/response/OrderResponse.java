package com.inventory.backend.dto.response;

import com.inventory.backend.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long orderId;
    private Long customerId;
    private String customerName;
    private String idempotencyKey;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;

    public OrderResponse() {
    }

    public static OrderResponse fromEntity(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.orderId = order.getOrderId();
        dto.customerId = order.getCustomerId();
        if (order.getCustomer() != null) {
            dto.customerName = order.getCustomer().getFullName();
        }
        dto.idempotencyKey = order.getIdempotencyKey();
        dto.status = order.getStatus();
        dto.totalAmount = order.getTotalAmount();
        dto.createdAt = order.getCreatedAt();
        dto.updatedAt = order.getUpdatedAt();
        if (order.getItems() != null) {
            dto.items = order.getItems().stream()
                    .map(OrderItemResponse::fromEntity)
                    .toList();
        }
        return dto;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}
