package com.delivery.foodDelivery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "order-updates", groupId = "food-delivery-group")
    public void consumeOrderUpdate(String message) {
        log.info("Received Order Update from Kafka: {}", message);
        // Here you can use WebSocket to push this update to the user frontend
        // Or update a real-time dashboard
    }

    @KafkaListener(topics = "delivery-locations", groupId = "food-delivery-group")
    public void consumeLocationUpdate(String message) {
        log.info("Received Location Update from Kafka: {}", message);
        // Push location to the user's LiveMap via WebSockets
    }
}
