package dev.magadiflo.orders_service.mapper;

import dev.magadiflo.orders_service.model.dtos.OrderItemRequest;
import dev.magadiflo.orders_service.model.dtos.OrderItemResponse;
import dev.magadiflo.orders_service.model.dtos.OrderRequest;
import dev.magadiflo.orders_service.model.dtos.OrderResponse;
import dev.magadiflo.orders_service.model.entities.Order;
import dev.magadiflo.orders_service.model.entities.OrderItem;

import java.util.List;
import java.util.UUID;

public class OrderMapper {
    public static Order mapToOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        // Como es una relación bidireccional debemos enlazar ambas entidades order <-> OrderItem
        order.setItems((mapToOrderItemList(orderRequest.items(), order)));
        return order;
    }

    public static OrderItem mapToOrderItem(OrderItemRequest orderItemRequest, Order order) {
        return OrderItem.builder()
                .id(orderItemRequest.id())
                .sku(orderItemRequest.sku())
                .price(orderItemRequest.price())
                .quantity(orderItemRequest.quantity())
                .order(order)
                .build();
    }

    public static List<OrderItem> mapToOrderItemList(List<OrderItemRequest> items, Order order) {
        return items.stream().map(orderItemRequest -> mapToOrderItem(orderItemRequest, order)).toList();
    }

    public static OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(order.getId(), order.getOrderNumber(), mapToOrderItemResponseList(order.getItems()));
    }

    public static OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return new OrderItemResponse(orderItem.getId(), orderItem.getSku(), orderItem.getPrice(), orderItem.getQuantity());
    }

    public static List<OrderItemResponse> mapToOrderItemResponseList(List<OrderItem> items) {
        return items.stream().map(OrderMapper::mapToOrderItemResponse).toList();
    }
}
