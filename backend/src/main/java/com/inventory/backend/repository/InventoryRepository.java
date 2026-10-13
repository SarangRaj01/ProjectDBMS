package com.inventory.backend.repository;

import com.inventory.backend.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT i FROM Inventory i " +
           "JOIN FETCH i.product p " +
           "JOIN FETCH i.warehouse w " +
           "WHERE (:productId IS NULL OR i.productId = :productId) AND " +
           "(:warehouseId IS NULL OR i.warehouseId = :warehouseId)")
    List<Inventory> findByProductAndWarehouse(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    /**
     * Pessimistic row-level lock (translates to SELECT ... FOR UPDATE in MySQL).
     * Serializes concurrent reservation attempts on the exact same inventory row.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.warehouseId = :warehouseId")
    Optional<Inventory> findByProductIdAndWarehouseIdForUpdate(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );

    /**
     * Conditional atomic reservation update.
     * Decrements available stock only if (on_hand_quantity - reserved_quantity >= :quantity).
     * Returns 1 if successful, 0 if insufficient stock.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity + :quantity " +
           "WHERE i.productId = :productId AND i.warehouseId = :warehouseId " +
           "AND (i.onHandQuantity - i.reservedQuantity) >= :quantity")
    int reserveStockConditionally(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("quantity") Integer quantity
    );

    /**
     * Atomic release of reserved stock (on cancellation).
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :quantity " +
           "WHERE i.productId = :productId AND i.warehouseId = :warehouseId " +
           "AND i.reservedQuantity >= :quantity")
    int releaseReservedStock(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("quantity") Integer quantity
    );
}
