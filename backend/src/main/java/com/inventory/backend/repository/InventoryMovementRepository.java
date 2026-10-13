package com.inventory.backend.repository;

import com.inventory.backend.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    @Query("SELECT m FROM InventoryMovement m " +
           "JOIN FETCH m.product p " +
           "JOIN FETCH m.warehouse w " +
           "WHERE (:productId IS NULL OR m.productId = :productId) AND " +
           "(:warehouseId IS NULL OR m.warehouseId = :warehouseId) " +
           "ORDER BY m.createdAt DESC")
    List<InventoryMovement> findMovementsFiltered(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );
}
