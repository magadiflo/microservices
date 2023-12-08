package dev.magadiflo.inventory_service.model.dtos;

public record OrderItemRequest(Long id, String sku, Double price, Long quantity) {
}