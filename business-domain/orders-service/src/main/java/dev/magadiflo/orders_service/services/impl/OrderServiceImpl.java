package dev.magadiflo.orders_service.services.impl;

import dev.magadiflo.orders_service.events.OrderEvent;
import dev.magadiflo.orders_service.mapper.JsonMapper;
import dev.magadiflo.orders_service.mapper.OrderMapper;
import dev.magadiflo.orders_service.model.dtos.BaseResponse;
import dev.magadiflo.orders_service.model.dtos.OrderRequest;
import dev.magadiflo.orders_service.model.dtos.OrderResponse;
import dev.magadiflo.orders_service.model.entities.Order;
import dev.magadiflo.orders_service.model.enums.OrderStatus;
import dev.magadiflo.orders_service.repositories.IOrderRepository;
import dev.magadiflo.orders_service.services.IOrderService;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final RestClient restClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderServiceImpl(IOrderRepository orderRepository, RestClient.Builder restClientBuilder,
                            KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.restClient = restClientBuilder.baseUrl("lb://inventory-service/api/v1/inventories").build();
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return this.orderRepository.findAll().stream().map(OrderMapper::mapToOrderResponse).toList();
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {
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
        Order orderDB = this.orderRepository.save(order);

        //TODO: Send message to order topic
        OrderEvent orderEvent = new OrderEvent(orderDB.getOrderNumber(), orderDB.getItems().size(), OrderStatus.PLACED);
        this.kafkaTemplate.send("orders-topic", JsonMapper.toJson(orderEvent));

        return OrderMapper.mapToOrderResponse(orderDB);
    }
}
