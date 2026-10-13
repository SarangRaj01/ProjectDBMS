package com.inventory.backend.service;

import com.inventory.backend.dto.request.ProductCreateRequest;
import com.inventory.backend.dto.response.InventoryResponse;
import com.inventory.backend.dto.response.ProductResponse;
import com.inventory.backend.entity.Category;
import com.inventory.backend.entity.Inventory;
import com.inventory.backend.entity.Product;
import com.inventory.backend.entity.Warehouse;
import com.inventory.backend.exception.DuplicateResourceException;
import com.inventory.backend.exception.ResourceNotFoundException;
import com.inventory.backend.repository.CategoryRepository;
import com.inventory.backend.repository.InventoryRepository;
import com.inventory.backend.repository.ProductRepository;
import com.inventory.backend.repository.WarehouseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceLayerTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductService productService;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("Product creation rejects duplicate SKU with DuplicateResourceException")
    void testCreateProduct_DuplicateSku() {
        ProductCreateRequest request = new ProductCreateRequest(
                1L, "ELEC-001", "Duplicate Mouse", "Desc", BigDecimal.valueOf(100), "ACTIVE"
        );
        when(productRepository.existsBySku("ELEC-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Product creation succeeds when SKU is unique and category exists")
    void testCreateProduct_Success() {
        ProductCreateRequest request = new ProductCreateRequest(
                1L, "TEST-999", "New Item", "Desc", BigDecimal.valueOf(499.99), "ACTIVE"
        );
        when(productRepository.existsBySku("TEST-999")).thenReturn(false);
        when(categoryRepository.existsById(1L)).thenReturn(true);

        Product savedProduct = new Product(1L, "TEST-999", "New Item", "Desc", BigDecimal.valueOf(499.99), "ACTIVE");
        savedProduct.setProductId(10L);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("TEST-999", response.getSku());
        assertEquals("New Item", response.getProductName());
    }

    @Test
    @DisplayName("Available quantity calculation = onHand - reserved")
    void testInventoryAvailableQuantityCalculation() {
        Product product = new Product(1L, "ELEC-001", "Wireless Mouse", "Desc", BigDecimal.valueOf(799), "ACTIVE");
        product.setProductId(1L);

        Warehouse warehouse = new Warehouse("Chennai Warehouse", "Chennai", "ACTIVE");
        warehouse.setWarehouseId(1L);

        Inventory inv = new Inventory(1L, 1L, 20, 7);
        inv.setInventoryId(100L);
        inv.setProduct(product);
        inv.setWarehouse(warehouse);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductAndWarehouse(1L, 1L)).thenReturn(List.of(inv));

        List<InventoryResponse> results = inventoryService.getInventory(1L, 1L);

        assertEquals(1, results.size());
        InventoryResponse item = results.get(0);
        assertEquals(20, item.getOnHandQuantity());
        assertEquals(7, item.getReservedQuantity());
        assertEquals(13, item.getAvailableQuantity(), "Available quantity must equal 20 - 7 = 13");
    }
}
