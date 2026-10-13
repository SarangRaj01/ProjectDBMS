package com.inventory.backend.service;

import com.inventory.backend.dto.response.WarehouseResponse;
import com.inventory.backend.entity.Warehouse;
import com.inventory.backend.exception.ResourceNotFoundException;
import com.inventory.backend.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(WarehouseResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "warehouse_id", id));
        return WarehouseResponse.fromEntity(warehouse);
    }
}
