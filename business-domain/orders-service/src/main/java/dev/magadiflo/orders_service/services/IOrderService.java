package dev.magadiflo.orders_service.services;

import dev.magadiflo.orders_service.model.dtos.OrderRequest;

public interface IOrderService {
    void placeOrder(OrderRequest orderRequest);
}
