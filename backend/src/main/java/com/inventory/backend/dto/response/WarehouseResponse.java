package com.inventory.backend.dto.response;

import com.inventory.backend.entity.Warehouse;
import java.time.LocalDateTime;

public class WarehouseResponse {

    private Long warehouseId;
    private String warehouseName;
    private String location;
    private String status;
    private LocalDateTime createdAt;

    public WarehouseResponse() {
    }

    public static WarehouseResponse fromEntity(Warehouse warehouse) {
        WarehouseResponse dto = new WarehouseResponse();
        dto.warehouseId = warehouse.getWarehouseId();
        dto.warehouseName = warehouse.getWarehouseName();
        dto.location = warehouse.getLocation();
        dto.status = warehouse.getStatus();
        dto.createdAt = warehouse.getCreatedAt();
        return dto;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
