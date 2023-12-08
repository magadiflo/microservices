package dev.magadiflo.products_service.model.dtos;

public record ProductResponse(Long id, String sku, String name, String description, Double price, Boolean status) {
}
