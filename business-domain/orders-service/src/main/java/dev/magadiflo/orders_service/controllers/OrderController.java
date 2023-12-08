package dev.magadiflo.orders_service.controllers;

import dev.magadiflo.orders_service.model.dtos.OrderRequest;
import dev.magadiflo.orders_service.model.dtos.OrderResponse;
import dev.magadiflo.orders_service.services.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/orders")
public class OrderController {

    private final IOrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrder() {
        return ResponseEntity.ok(this.orderService.getAllOrders());
    }


    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        this.orderService.placeOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Order placed successfully");
    }

}
