package dev.magadiflo.orders_service.controllers;

import dev.magadiflo.orders_service.model.dtos.OrderRequest;
import dev.magadiflo.orders_service.model.dtos.OrderResponse;
import dev.magadiflo.orders_service.services.IOrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/orders")
public class OrderController {

    private final IOrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrder() {
        return ResponseEntity.ok(this.orderService.getAllOrders());
    }

    @CircuitBreaker(name = "orders-service", fallbackMethod = "placeOrderFallback")
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = this.orderService.placeOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    private ResponseEntity<OrderResponse> placeOrderFallback(OrderRequest orderRequest, Throwable throwable) {
        log.info("Llamando al fallbackMethod placeOrderFallback(...)");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

}
