package com.inventory.backend.controller;

import com.inventory.backend.dto.response.InventoryResponse;
import com.inventory.backend.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getInventory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId
    ) {
        return ResponseEntity.ok(inventoryService.getInventory(productId, warehouseId));
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductAndWarehouse(
            @PathVariable Long productId,
            @PathVariable Long warehouseId
    ) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductAndWarehouse(productId, warehouseId));
    }
}
