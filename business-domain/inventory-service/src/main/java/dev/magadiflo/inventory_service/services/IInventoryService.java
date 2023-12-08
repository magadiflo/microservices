package dev.magadiflo.inventory_service.services;

import dev.magadiflo.inventory_service.model.dtos.BaseResponse;
import dev.magadiflo.inventory_service.model.dtos.OrderItemRequest;

import java.util.List;

public interface IInventoryService {
    Boolean isInStock(String sku);

    BaseResponse areInStock(List<OrderItemRequest> items);
}
