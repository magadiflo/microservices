package dev.magadiflo.orders_service.mapper;

import dev.magadiflo.orders_service.model.dtos.OrderItemRequest;
import dev.magadiflo.orders_service.model.dtos.OrderRequest;
import dev.magadiflo.orders_service.model.entities.Order;
import dev.magadiflo.orders_service.model.entities.OrderItem;

import java.util.List;
import java.util.UUID;

public class OrderMapper {
    public static Order mapToOrder(OrderRequest orderRequest) {
        return Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .items(mapToOrderItemList(orderRequest.items()))
                .build();
    }

    public static OrderItem mapToOrderItem(OrderItemRequest orderItemRequest) {
        return OrderItem.builder()
                .id(orderItemRequest.id())
                .sku(orderItemRequest.sku())
                .price(orderItemRequest.price())
                .quantity(orderItemRequest.quantity())
                .build();
    }

    public static List<OrderItem> mapToOrderItemList(List<OrderItemRequest> items) {
        return items.stream().map(OrderMapper::mapToOrderItem).toList();
    }
}
