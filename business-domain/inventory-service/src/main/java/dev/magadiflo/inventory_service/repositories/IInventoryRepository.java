package dev.magadiflo.inventory_service.repositories;

import dev.magadiflo.inventory_service.model.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IInventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySku(String sku);
    List<Inventory> findBySkuIn(List<String> skus);
}
