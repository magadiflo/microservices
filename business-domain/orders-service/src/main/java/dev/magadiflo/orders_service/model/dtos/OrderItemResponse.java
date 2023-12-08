package dev.magadiflo.orders_service.model.dtos;

public record OrderItemResponse(Long id, String sku, Double price, Long quantity) {
}
