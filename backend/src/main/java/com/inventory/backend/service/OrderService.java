package com.inventory.backend.service;

import com.inventory.backend.dto.request.OrderCreateRequest;
import com.inventory.backend.dto.request.OrderItemRequest;
import com.inventory.backend.dto.response.OrderResponse;
import com.inventory.backend.entity.Customer;
import com.inventory.backend.entity.Inventory;
import com.inventory.backend.entity.InventoryMovement;
import com.inventory.backend.entity.Order;
import com.inventory.backend.entity.OrderItem;
import com.inventory.backend.entity.OrderStatusHistory;
import com.inventory.backend.entity.Product;
import com.inventory.backend.entity.StockReservation;
import com.inventory.backend.entity.Warehouse;
import com.inventory.backend.exception.InsufficientStockException;
import com.inventory.backend.exception.InvalidOperationException;
import com.inventory.backend.exception.ResourceNotFoundException;
import com.inventory.backend.repository.CustomerRepository;
import com.inventory.backend.repository.InventoryMovementRepository;
import com.inventory.backend.repository.InventoryRepository;
import com.inventory.backend.repository.OrderRepository;
import com.inventory.backend.repository.OrderStatusHistoryRepository;
import com.inventory.backend.repository.ProductRepository;
import com.inventory.backend.repository.StockReservationRepository;
import com.inventory.backend.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository stockReservationRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            InventoryRepository inventoryRepository,
            StockReservationRepository stockReservationRepository,
            InventoryMovementRepository inventoryMovementRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockReservationRepository = stockReservationRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String status, Long customerId) {
        return orderRepository.findOrdersFiltered(status, customerId).stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "order_id", id));
        return OrderResponse.fromEntity(order);
    }

    /**
     * Atomically creates an order and reserves inventory across warehouses.
     * Uses pessimistic row locking to serialize concurrent reservation requests
     * on the same product and warehouse.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 1. Idempotency Check: if request was already processed, return existing order
        var existingOrder = orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingOrder.isPresent()) {
            return OrderResponse.fromEntity(existingOrder.get());
        }

        // 2. Customer Validation
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "customer_id", request.getCustomerId()));

        // 3. Construct Order entity
        Order order = new Order();
        order.setCustomerId(customer.getCustomerId());
        order.setCustomer(customer);
        order.setIdempotencyKey(request.getIdempotencyKey());
        order.setStatus("CONFIRMED");

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<StockReservation> reservations = new ArrayList<>();

        // 4. Validate items and lock stock per line item
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", itemReq.getProductId()));

            if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                throw new InvalidOperationException("Product " + product.getSku() + " is inactive and cannot be ordered");
            }

            if (!warehouseRepository.existsById(itemReq.getWarehouseId())) {
                throw new ResourceNotFoundException("Warehouse", "warehouse_id", itemReq.getWarehouseId());
            }

            // Acquire pessimistic row lock on Inventory (SELECT ... FOR UPDATE)
            Inventory inventory = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(
                    itemReq.getProductId(), itemReq.getWarehouseId()
            ).orElseThrow(() -> new ResourceNotFoundException(
                    String.format("No inventory found for product %d in warehouse %d", itemReq.getProductId(), itemReq.getWarehouseId())
            ));

            int available = inventory.getAvailableQuantity();
            if (available < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                        itemReq.getProductId(), itemReq.getWarehouseId(), itemReq.getQuantity(), available
                );
            }

            // Reserve inventory
            inventory.setReservedQuantity(inventory.getReservedQuantity() + itemReq.getQuantity());
            inventoryRepository.save(inventory);

            // Calculate line total
            BigDecimal lineTotal = product.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            OrderItem orderItem = new OrderItem(product.getProductId(), itemReq.getQuantity(), product.getUnitPrice());
            orderItem.setProduct(product);
            order.addItem(orderItem);
            orderItems.add(orderItem);

            StockReservation reservation = new StockReservation(
                    null, // set after saving item
                    itemReq.getWarehouseId(),
                    itemReq.getQuantity(),
                    "RESERVED"
            );
            reservations.add(reservation);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // 5. Link and save stock reservations and audit records
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem savedItem = savedOrder.getItems().get(i);
            StockReservation res = reservations.get(i);
            res.setOrderItemId(savedItem.getOrderItemId());
            stockReservationRepository.save(res);

            // Record inventory movement audit
            InventoryMovement movement = new InventoryMovement(
                    savedItem.getProductId(),
                    res.getWarehouseId(),
                    null,
                    "SHIPMENT",
                    -savedItem.getQuantity(),
                    "ORDER",
                    savedOrder.getOrderId(),
                    "Stock reserved for Order #" + savedOrder.getOrderId()
            );
            inventoryMovementRepository.save(movement);
        }

        // 6. Record Order Status History audit
        OrderStatusHistory history = new OrderStatusHistory(
                savedOrder.getOrderId(),
                null,
                null,
                "CONFIRMED",
                "Order created and stock reserved successfully"
        );
        orderStatusHistoryRepository.save(history);

        return OrderResponse.fromEntity(savedOrder);
    }

    /**
     * Atomically cancels an order and safely releases reserved stock exactly once.
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "order_id", orderId));

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new InvalidOperationException("Order #" + orderId + " has already been cancelled");
        }

        if ("SHIPPED".equalsIgnoreCase(order.getStatus()) || "DELIVERED".equalsIgnoreCase(order.getStatus())) {
            throw new InvalidOperationException("Cannot cancel an order in " + order.getStatus() + " status");
        }

        String oldStatus = order.getStatus();
        order.setStatus("CANCELLED");

        // Release stock for each item in this order
        List<Long> itemIds = order.getItems().stream().map(OrderItem::getOrderItemId).toList();
        List<StockReservation> activeReservations = stockReservationRepository.findByOrderItemIdIn(itemIds);

        for (StockReservation res : activeReservations) {
            if ("RESERVED".equalsIgnoreCase(res.getStatus())) {
                res.setStatus("RELEASED");
                stockReservationRepository.save(res);

                // Fetch inventory with row lock and decrement reserved_quantity
                OrderItem item = order.getItems().stream()
                        .filter(it -> it.getOrderItemId().equals(res.getOrderItemId()))
                        .findFirst()
                        .orElse(null);

                if (item != null) {
                    Inventory inv = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(
                            item.getProductId(), res.getWarehouseId()
                    ).orElse(null);

                    if (inv != null) {
                        int newReserved = Math.max(0, inv.getReservedQuantity() - res.getQuantity());
                        inv.setReservedQuantity(newReserved);
                        inventoryRepository.save(inv);

                        // Audit release movement
                        InventoryMovement movement = new InventoryMovement(
                                item.getProductId(),
                                res.getWarehouseId(),
                                null,
                                "RETURN",
                                res.getQuantity(),
                                "ORDER_CANCELLATION",
                                order.getOrderId(),
                                "Stock released due to cancellation of Order #" + order.getOrderId()
                        );
                        inventoryMovementRepository.save(movement);
                    }
                }
            }
        }

        // Record Order Status History
        OrderStatusHistory history = new OrderStatusHistory(
                order.getOrderId(),
                null,
                oldStatus,
                "CANCELLED",
                reason != null ? reason : "Order cancelled by customer or manager"
        );
        orderStatusHistoryRepository.save(history);

        Order updated = orderRepository.save(order);
        return OrderResponse.fromEntity(updated);
    }
}
