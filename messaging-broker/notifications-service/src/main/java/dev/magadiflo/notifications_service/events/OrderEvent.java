package dev.magadiflo.notifications_service.events;


import dev.magadiflo.notifications_service.model.enums.OrderStatus;

public record OrderEvent(String orderNumber, int itemsCount, OrderStatus orderStatus) {
}
