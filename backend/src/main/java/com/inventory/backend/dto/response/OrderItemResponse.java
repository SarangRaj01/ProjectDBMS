package com.inventory.backend.dto.response;

import com.inventory.backend.entity.OrderItem;
import java.math.BigDecimal;

public class OrderItemResponse {

    private Long orderItemId;
    private Long productId;
    private String productSku;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public OrderItemResponse() {
    }

    public static OrderItemResponse fromEntity(OrderItem item) {
        OrderItemResponse dto = new OrderItemResponse();
        dto.orderItemId = item.getOrderItemId();
        dto.productId = item.getProductId();
        if (item.getProduct() != null) {
            dto.productSku = item.getProduct().getSku();
            dto.productName = item.getProduct().getProductName();
        }
        dto.quantity = item.getQuantity();
        dto.unitPrice = item.getUnitPrice();
        // lineTotal is calculated or from DB generated column
        if (item.getLineTotal() != null) {
            dto.lineTotal = item.getLineTotal();
        } else if (item.getUnitPrice() != null && item.getQuantity() != null) {
            dto.lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        }
        return dto;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
