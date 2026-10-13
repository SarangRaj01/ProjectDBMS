package com.inventory.backend.service;

import com.inventory.backend.dto.response.InventoryResponse;
import com.inventory.backend.exception.ResourceNotFoundException;
import com.inventory.backend.repository.InventoryRepository;
import com.inventory.backend.repository.ProductRepository;
import com.inventory.backend.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventory(Long productId, Long warehouseId) {
        if (productId != null && !productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "product_id", productId);
        }
        if (warehouseId != null && !warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "warehouse_id", warehouseId);
        }

        return inventoryRepository.findByProductAndWarehouse(productId, warehouseId).stream()
                .map(InventoryResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductAndWarehouse(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .map(InventoryResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No inventory record found for product ID %d in warehouse ID %d", productId, warehouseId)
                ));
    }
}
