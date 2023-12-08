package dev.magadiflo.inventory_service.services.impl;

import dev.magadiflo.inventory_service.model.dtos.BaseResponse;
import dev.magadiflo.inventory_service.model.dtos.OrderItemRequest;
import dev.magadiflo.inventory_service.model.entities.Inventory;
import dev.magadiflo.inventory_service.repositories.IInventoryRepository;
import dev.magadiflo.inventory_service.services.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class InventoryServiceImpl implements IInventoryService {

    private final IInventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Boolean isInStock(String sku) {
        return this.inventoryRepository.findBySku(sku)
                .filter(inventoryDB -> inventoryDB.getQuantity() > 0)
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse areInStock(List<OrderItemRequest> items) {
        List<String> skus = items.stream().map(OrderItemRequest::sku).toList();
        List<Inventory> inventoriesDB = this.inventoryRepository.findBySkuIn(skus);
        List<String> errorList = new ArrayList<>();

        items.forEach(orderItemRequest -> {
            Optional<Inventory> inventoryOptional = inventoriesDB.stream()
                    .filter(inventory -> inventory.getSku().equals(orderItemRequest.sku()))
                    .findFirst();
            if (inventoryOptional.isEmpty()) {
                errorList.add("Product with sku %s does not exist!".formatted(orderItemRequest.sku()));
            } else if (inventoryOptional.get().getQuantity() < orderItemRequest.quantity()) {
                errorList.add("Product with sku %s has insufficient quantity".formatted(orderItemRequest.sku()));
            }
        });

        return !errorList.isEmpty() ? new BaseResponse(errorList.toArray(String[]::new)) : new BaseResponse(null);
    }
}
