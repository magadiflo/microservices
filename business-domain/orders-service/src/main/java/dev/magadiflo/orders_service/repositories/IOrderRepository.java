package dev.magadiflo.orders_service.repositories;

import dev.magadiflo.orders_service.model.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrderRepository extends JpaRepository<Order, Long> {
}
