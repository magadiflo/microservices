package dev.magadiflo.orders_service.events;

import dev.magadiflo.orders_service.model.enums.OrderStatus;

public record OrderEvent(String orderNumber, int itemsCount, OrderStatus orderStatus) {
}
