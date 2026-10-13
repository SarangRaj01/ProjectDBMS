package com.inventory.backend.repository;

import com.inventory.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.customer c " +
           "WHERE (:status IS NULL OR o.status = :status) AND " +
           "(:customerId IS NULL OR o.customerId = :customerId) " +
           "ORDER BY o.createdAt DESC")
    List<Order> findOrdersFiltered(
            @Param("status") String status,
            @Param("customerId") Long customerId
    );

    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product p " +
           "WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);
}
