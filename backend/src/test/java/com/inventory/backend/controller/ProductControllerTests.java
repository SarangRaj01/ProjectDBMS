package com.inventory.backend.controller;

import com.inventory.backend.dto.request.ProductCreateRequest;
import com.inventory.backend.dto.response.ProductResponse;
import com.inventory.backend.exception.DuplicateResourceException;
import com.inventory.backend.exception.GlobalExceptionHandler;
import com.inventory.backend.exception.ResourceNotFoundException;
import com.inventory.backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTests {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/products returns 200 OK with product list")
    void testGetAllProducts() throws Exception {
        ProductResponse p = new ProductResponse();
        when(productService.getAllProducts()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 404 when product is missing")
    void testGetProductNotFound() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Product", "product_id", 999L));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("POST /api/products returns 400 Bad Request when validation fails")
    void testCreateProductValidationFailure() throws Exception {
        // Missing SKU and negative price in raw JSON payload
        String invalidJson = """
                {
                    "categoryId": 1,
                    "sku": "",
                    "productName": "",
                    "unitPrice": -50.00,
                    "status": "INVALID"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.sku").exists())
                .andExpect(jsonPath("$.validationErrors.unitPrice").exists());
    }

    @Test
    @DisplayName("POST /api/products returns 409 Conflict when SKU already exists")
    void testCreateProductDuplicateSku() throws Exception {
        String validJson = """
                {
                    "categoryId": 1,
                    "sku": "ELEC-001",
                    "productName": "Keyboard",
                    "unitPrice": 1999.00,
                    "status": "ACTIVE"
                }
                """;

        when(productService.createProduct(any()))
                .thenThrow(new DuplicateResourceException("Product with SKU 'ELEC-001' already exists"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}
