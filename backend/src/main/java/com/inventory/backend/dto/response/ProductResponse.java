package com.inventory.backend.dto.response;

import com.inventory.backend.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {

    private Long productId;
    private Long categoryId;
    private String categoryName;
    private String sku;
    private String productName;
    private String description;
    private BigDecimal unitPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse() {
    }

    public static ProductResponse fromEntity(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.productId = product.getProductId();
        dto.categoryId = product.getCategoryId();
        if (product.getCategory() != null) {
            dto.categoryName = product.getCategory().getCategoryName();
        }
        dto.sku = product.getSku();
        dto.productName = product.getProductName();
        dto.description = product.getDescription();
        dto.unitPrice = product.getUnitPrice();
        dto.status = product.getStatus();
        dto.createdAt = product.getCreatedAt();
        dto.updatedAt = product.getUpdatedAt();
        return dto;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getSku() {
        return sku;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
