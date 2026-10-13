package com.inventory.backend.service;

import com.inventory.backend.dto.request.OrderCreateRequest;
import com.inventory.backend.dto.request.OrderItemRequest;
import com.inventory.backend.dto.response.OrderResponse;
import com.inventory.backend.entity.Customer;
import com.inventory.backend.entity.Inventory;
import com.inventory.backend.entity.Order;
import com.inventory.backend.entity.Product;
import com.inventory.backend.exception.InsufficientStockException;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcurrentOrderReservationTests {

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
    @DisplayName("Two simultaneous orders for 7 units from 10 available stock: exactly 1 succeeds, 1 rejected")
    void testConcurrentOrdersExceedingStock() throws InterruptedException, ExecutionException {
        // Shared inventory state: 10 on hand, 0 reserved => 10 available
        final Inventory sharedInventory = new Inventory(2L, 1L, 10, 0);

        Customer customer = new Customer("Aarav", "aarav@example.com", "1234567890");
        customer.setCustomerId(1L);

        Product keyboard = new Product(1L, "ELEC-002", "Mechanical Keyboard", "Desc", BigDecimal.valueOf(2499.00), "ACTIVE");
        keyboard.setProductId(2L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.of(keyboard));
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        // Simulate database mutex row lock: only one thread accesses findByProductIdAndWarehouseIdForUpdate at a time
        when(inventoryRepository.findByProductIdAndWarehouseIdForUpdate(2L, 1L)).thenAnswer(inv -> {
            // Emulate DB read lock on shared row
            synchronized (sharedInventory) {
                return Optional.of(sharedInventory);
            }
        });

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(System.nanoTime());
            if (!o.getItems().isEmpty()) {
                o.getItems().get(0).setOrderItemId(System.nanoTime() + 1);
            }
            return o;
        });

        // 2 threads competing at the exact same millisecond
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        Callable<OrderResponse> task1 = () -> {
            barrier.await(); // Synchronize thread start
            OrderCreateRequest req = new OrderCreateRequest(
                    1L, "CONCURRENT-ORDER-A", List.of(new OrderItemRequest(2L, 1L, 7))
            );
            return orderService.createOrder(req);
        };

        Callable<OrderResponse> task2 = () -> {
            barrier.await(); // Synchronize thread start
            OrderCreateRequest req = new OrderCreateRequest(
                    1L, "CONCURRENT-ORDER-B", List.of(new OrderItemRequest(2L, 1L, 7))
            );
            return orderService.createOrder(req);
        };

        Future<OrderResponse> f1 = executor.submit(task1);
        Future<OrderResponse> f2 = executor.submit(task2);

        // Process Future 1
        try {
            f1.get();
            successCount.incrementAndGet();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InsufficientStockException) {
                rejectedCount.incrementAndGet();
            } else {
                throw e;
            }
        }

        // Process Future 2
        try {
            f2.get();
            successCount.incrementAndGet();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InsufficientStockException) {
                rejectedCount.incrementAndGet();
            } else {
                throw e;
            }
        }

        executor.shutdown();

        // INVARIANT VERIFICATIONS:
        assertEquals(1, successCount.get(), "Exactly ONE order must succeed");
        assertEquals(1, rejectedCount.get(), "The competing order must be rejected with InsufficientStockException");
        assertEquals(7, sharedInventory.getReservedQuantity(), "Total reserved units must be 7 (never 14)");
        assertEquals(3, sharedInventory.getAvailableQuantity(), "Available units must be 10 - 7 = 3");
        assertTrue(sharedInventory.getReservedQuantity() <= sharedInventory.getOnHandQuantity(),
                "Invariant violated: reserved > on_hand");
    }
}
