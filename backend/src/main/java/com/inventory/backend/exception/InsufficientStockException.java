package com.inventory.backend.exception;

/**
 * Thrown when an order attempts to reserve more stock than is currently available.
 */
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final Long warehouseId;
    private final int requested;
    private final int available;

    public InsufficientStockException(Long productId, Long warehouseId, int requested, int available) {
        super(String.format("Insufficient stock for product ID %d in warehouse ID %d: requested %d, available %d",
                productId, warehouseId, requested, available));
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.requested = requested;
        this.available = available;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
