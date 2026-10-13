package com.inventory.backend.service;

import com.inventory.backend.dto.request.OrderCreateRequest;
import com.inventory.backend.dto.request.OrderItemRequest;
import com.inventory.backend.dto.response.OrderResponse;
import com.inventory.backend.entity.Customer;
import com.inventory.backend.entity.Inventory;
import com.inventory.backend.entity.Order;
import com.inventory.backend.entity.OrderItem;
import com.inventory.backend.entity.Product;
import com.inventory.backend.entity.StockReservation;
import com.inventory.backend.exception.InsufficientStockException;
import com.inventory.backend.exception.InvalidOperationException;
import com.inventory.backend.repository.CustomerRepository;
import com.inventory.backend.repository.InventoryMovementRepository;
import com.inventory.backend.repository.InventoryRepository;
import com.inventory.backend.repository.OrderRepository;
import com.inventory.backend.repository.OrderStatusHistoryRepository;
import com.inventory.backend.repository.ProductRepository;
import com.inventory.backend.repository.StockReservationRepository;
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
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private StockReservationRepository stockReservationRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Create order reserves stock and calculates totals correctly")
    void testCreateOrder_Success() {
        OrderCreateRequest req = new OrderCreateRequest(
                1L, "KEY-100", List.of(new OrderItemRequest(2L, 1L, 3))
        );

        Customer customer = new Customer("Aarav Nair", "aarav@example.com", "9876500001");
        customer.setCustomerId(1L);

        Product product = new Product(1L, "ELEC-002", "Mechanical Keyboard", "Desc", BigDecimal.valueOf(2499.00), "ACTIVE");
        product.setProductId(2L);

        Inventory inv = new Inventory(2L, 1L, 10, 0); // 10 on hand, 0 reserved => 10 available

        when(orderRepository.findByIdempotencyKey("KEY-100")).thenReturn(Optional.empty());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseIdForUpdate(2L, 1L)).thenReturn(Optional.of(inv));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setOrderId(101L);
            o.getItems().get(0).setOrderItemId(501L);
            return o;
        });

        OrderResponse res = orderService.createOrder(req);

        assertNotNull(res);
        assertEquals(101L, res.getOrderId());
        assertEquals("CONFIRMED", res.getStatus());
        assertEquals(BigDecimal.valueOf(7497.00), res.getTotalAmount(), "Total should be 3 * 2499 = 7497");
        assertEquals(3, inv.getReservedQuantity(), "Reserved quantity in inventory must increase by 3");

        verify(inventoryRepository).save(inv);
        verify(stockReservationRepository).save(any());
        verify(inventoryMovementRepository).save(any());
        verify(orderStatusHistoryRepository).save(any());
    }

    @Test
    @DisplayName("Create order fails and rolls back when requested stock exceeds available stock")
    void testCreateOrder_InsufficientStock() {
        // Customer requests 7, but only 5 are available (10 on hand, 5 already reserved)
        OrderCreateRequest req = new OrderCreateRequest(
                1L, "KEY-200", List.of(new OrderItemRequest(2L, 1L, 7))
        );

        Customer customer = new Customer("Aarav Nair", "aarav@example.com", "9876500001");
        customer.setCustomerId(1L);

        Product product = new Product(1L, "ELEC-002", "Mechanical Keyboard", "Desc", BigDecimal.valueOf(2499.00), "ACTIVE");
        product.setProductId(2L);

        Inventory inv = new Inventory(2L, 1L, 10, 5); // 10 on hand, 5 reserved => 5 available

        when(orderRepository.findByIdempotencyKey("KEY-200")).thenReturn(Optional.empty());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseIdForUpdate(2L, 1L)).thenReturn(Optional.of(inv));

        InsufficientStockException ex = assertThrows(
                InsufficientStockException.class, () -> orderService.createOrder(req)
        );

        assertEquals(2L, ex.getProductId());
        assertEquals(1L, ex.getWarehouseId());
        assertEquals(7, ex.getRequested());
        assertEquals(5, ex.getAvailable());

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Idempotency: Re-submitting identical idempotency key returns existing order without re-reserving")
    void testCreateOrder_Idempotency() {
        OrderCreateRequest req = new OrderCreateRequest(
                1L, "KEY-EXISTING", List.of(new OrderItemRequest(2L, 1L, 2))
        );

        Order existing = new Order(1L, "KEY-EXISTING", "CONFIRMED", BigDecimal.valueOf(4998.00));
        existing.setOrderId(88L);

        when(orderRepository.findByIdempotencyKey("KEY-EXISTING")).thenReturn(Optional.of(existing));

        OrderResponse res = orderService.createOrder(req);

        assertEquals(88L, res.getOrderId());
        verify(customerRepository, never()).findById(any());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Canceling an order releases reservation and updates status")
    void testCancelOrder_Success() {
        Order order = new Order(1L, "KEY-CANCEL", "CONFIRMED", BigDecimal.valueOf(2499.00));
        order.setOrderId(50L);

        OrderItem item = new OrderItem(2L, 1, BigDecimal.valueOf(2499.00));
        item.setOrderItemId(300L);
        order.addItem(item);

        StockReservation res = new StockReservation(300L, 1L, 1, "RESERVED");
        res.setReservationId(900L);

        Inventory inv = new Inventory(2L, 1L, 10, 1); // 1 reserved

        when(orderRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(order));
        when(stockReservationRepository.findByOrderItemIdIn(List.of(300L))).thenReturn(List.of(res));
        when(inventoryRepository.findByProductIdAndWarehouseIdForUpdate(2L, 1L)).thenReturn(Optional.of(inv));
        when(orderRepository.save(any())).thenReturn(order);

        OrderResponse cancelledResponse = orderService.cancelOrder(50L, "Customer requested cancellation");

        assertEquals("CANCELLED", cancelledResponse.getStatus());
        assertEquals("RELEASED", res.getStatus());
        assertEquals(0, inv.getReservedQuantity(), "Reserved quantity should drop back to 0");

        verify(stockReservationRepository).save(res);
        verify(inventoryRepository).save(inv);
        verify(inventoryMovementRepository).save(any());
    }

    @Test
    @DisplayName("Cannot cancel an order that is already CANCELLED (prevents double-release)")
    void testCancelOrder_AlreadyCancelled() {
        Order order = new Order(1L, "KEY-CANCEL", "CANCELLED", BigDecimal.valueOf(2499.00));
        order.setOrderId(50L);

        when(orderRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOperationException.class, () -> orderService.cancelOrder(50L, "Repeated cancel"));
        verify(stockReservationRepository, never()).save(any());
    }
}
