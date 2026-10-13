package com.inventory.backend.controller;

import com.inventory.backend.dto.request.OrderCreateRequest;
import com.inventory.backend.dto.request.OrderItemRequest;
import com.inventory.backend.dto.response.OrderResponse;
import com.inventory.backend.exception.GlobalExceptionHandler;
import com.inventory.backend.exception.InsufficientStockException;
import com.inventory.backend.exception.ResourceNotFoundException;
import com.inventory.backend.service.OrderService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTests {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/orders returns 201 Created on valid order placement")
    void testCreateOrderSuccess() throws Exception {
        OrderResponse response = new OrderResponse();
        when(orderService.createOrder(any())).thenReturn(response);

        String json = """
                {
                    "customerId": 1,
                    "idempotencyKey": "KEY-OK-1",
                    "items": [
                        { "productId": 1, "warehouseId": 1, "quantity": 2 }
                    ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/orders returns 409 Conflict when stock is insufficient")
    void testCreateOrderInsufficientStock() throws Exception {
        when(orderService.createOrder(any()))
                .thenThrow(new InsufficientStockException(2L, 1L, 7, 3));

        String json = """
                {
                    "customerId": 1,
                    "idempotencyKey": "KEY-FAIL-1",
                    "items": [
                        { "productId": 2, "warehouseId": 1, "quantity": 7 }
                    ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Insufficient Stock"));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/cancel returns 200 OK on successful cancellation")
    void testCancelOrderSuccess() throws Exception {
        OrderResponse response = new OrderResponse();
        when(orderService.cancelOrder(eq(10L), any())).thenReturn(response);

        mockMvc.perform(post("/api/orders/10/cancel?reason=ChangedMind"))
                .andExpect(status().isOk());
    }
}
