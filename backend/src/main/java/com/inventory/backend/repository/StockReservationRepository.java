package com.inventory.backend.repository;

import com.inventory.backend.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findByOrderItemId(Long orderItemId);

    List<StockReservation> findByOrderItemIdIn(List<Long> orderItemIds);
}
