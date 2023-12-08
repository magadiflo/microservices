package dev.magadiflo.orders_service.services;

import dev.magadiflo.orders_service.model.dtos.OrderRequest;
import dev.magadiflo.orders_service.model.dtos.OrderResponse;

import java.util.List;

public interface IOrderService {
    List<OrderResponse> getAllOrders();

    void placeOrder(OrderRequest orderRequest);
}
