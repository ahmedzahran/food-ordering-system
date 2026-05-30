package com.food.ordering.system.order.service.dataacess.order.repository;


import com.food.ordering.system.order.service.dataacess.order.entity.OrderEntity;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

@Registered
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByTrackingId(UUID trackingId);
}
