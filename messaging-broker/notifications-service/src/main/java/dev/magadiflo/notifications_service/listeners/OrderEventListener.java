package dev.magadiflo.notifications_service.listeners;

import dev.magadiflo.notifications_service.events.OrderEvent;
import dev.magadiflo.notifications_service.mapper.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {
    @KafkaListener(topics = "orders-topic")
    public void handlerOrdersNotifications(String message) {
        OrderEvent orderEvent = JsonMapper.fromJson(message, OrderEvent.class);

        // Send email to customer, send sms to customer, etc.
        // Notify another service...

        log.info("Order {} event received for order: {} with {} items",
                orderEvent.orderStatus(), orderEvent.orderNumber(), orderEvent.itemsCount());
    }

}
