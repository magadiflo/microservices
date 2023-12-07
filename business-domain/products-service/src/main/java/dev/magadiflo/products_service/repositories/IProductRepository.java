package dev.magadiflo.products_service.repositories;

import dev.magadiflo.products_service.model.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IProductRepository extends JpaRepository<Product, Long> {
}
