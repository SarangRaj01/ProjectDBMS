package com.inventory.backend.dto.response;

import com.inventory.backend.entity.Inventory;
import java.time.LocalDateTime;

public class InventoryResponse {

    private Long inventoryId;
    private Long productId;
    private String productSku;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private Integer onHandQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Long version;
    private LocalDateTime updatedAt;

    public InventoryResponse() {
    }

    public static InventoryResponse fromEntity(Inventory inventory) {
        InventoryResponse dto = new InventoryResponse();
        dto.inventoryId = inventory.getInventoryId();
        dto.productId = inventory.getProductId();
        if (inventory.getProduct() != null) {
            dto.productSku = inventory.getProduct().getSku();
            dto.productName = inventory.getProduct().getProductName();
        }
        dto.warehouseId = inventory.getWarehouseId();
        if (inventory.getWarehouse() != null) {
            dto.warehouseName = inventory.getWarehouse().getWarehouseName();
        }
        dto.onHandQuantity = inventory.getOnHandQuantity();
        dto.reservedQuantity = inventory.getReservedQuantity();
        dto.availableQuantity = inventory.getAvailableQuantity();
        dto.version = inventory.getVersion();
        dto.updatedAt = inventory.getUpdatedAt();
        return dto;
    }

    public Long getInventoryId() {
        return inventoryId;
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

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public Integer getOnHandQuantity() {
        return onHandQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
