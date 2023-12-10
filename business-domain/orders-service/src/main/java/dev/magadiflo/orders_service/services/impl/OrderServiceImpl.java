package dev.magadiflo.orders_service.services.impl;

import dev.magadiflo.orders_service.mapper.OrderMapper;
import dev.magadiflo.orders_service.model.dtos.BaseResponse;
import dev.magadiflo.orders_service.model.dtos.OrderRequest;
import dev.magadiflo.orders_service.model.dtos.OrderResponse;
import dev.magadiflo.orders_service.model.entities.Order;
import dev.magadiflo.orders_service.repositories.IOrderRepository;
import dev.magadiflo.orders_service.services.IOrderService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final RestClient restClient;

    public OrderServiceImpl(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8080/api/v1/inventories") //Colocando ruta de api-gateway
                .build();
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return this.orderRepository.findAll().stream().map(OrderMapper::mapToOrderResponse).toList();
    }

    @Override
    @Transactional
    public void placeOrder(OrderRequest orderRequest) {
        // Check for inventory
        BaseResponse response = this.restClient.post()
                .uri("/in-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .body(orderRequest.items())
                .retrieve()
                .body(BaseResponse.class);

        if (response == null || response.hasErrors()) {
            throw new IllegalArgumentException("Some of products are not in stock");
        }

        Order order = OrderMapper.mapToOrder(orderRequest);
        this.orderRepository.save(order);
    }
}
