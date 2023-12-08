package dev.magadiflo.inventory_service.controllers;

import dev.magadiflo.inventory_service.model.dtos.BaseResponse;
import dev.magadiflo.inventory_service.model.dtos.OrderItemRequest;
import dev.magadiflo.inventory_service.services.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/inventories")
public class InventoryController {

    private final IInventoryService iInventoryService;

    @GetMapping(path = "/{sku}")
    public ResponseEntity<Boolean> isInStock(@PathVariable String sku) {
        return ResponseEntity.ok(this.iInventoryService.isInStock(sku));
    }

    @PostMapping(path = "/in-stock")
    public ResponseEntity<BaseResponse> areInStock(@RequestBody List<OrderItemRequest> items) {
        return ResponseEntity.ok(this.iInventoryService.areInStock(items));
    }
}
