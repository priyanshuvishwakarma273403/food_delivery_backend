package com.delivery.foodDelivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Send a message to a specific topic
     * @param topic Topic name
     * @param message Message content (usually JSON string)
     */
    public void sendMessage(String topic, String message) {
        try {
            log.info("Sending message to Kafka topic {}: {}", topic, message);
            kafkaTemplate.send(topic, message);
        } catch (Exception e) {
            log.error("Kafka ERROR: Could not send message to topic {}. Is Kafka connected? Error: {}", 
                topic, e.getMessage());
            // We catch exception so that main flow (like Login/OTP) is not blocked
        }
    }

    /**
     * Send order status update event
     * @param orderId ID of the order
     * @param status New status
     */
    public void sendOrderStatusUpdate(Long orderId, String status) {
        String message = String.format("{\"orderId\": %d, \"status\": \"%s\", \"timestamp\": %d}", 
                orderId, status, System.currentTimeMillis());
        sendMessage("order-updates", message);
    }

    /**
     * Send delivery agent location update
     * @param deliveryId Delivery ID
     * @param lat Latitude
     * @param lng Longitude
     */
    public void sendLocationUpdate(Long deliveryId, Double lat, Double lng) {
        String message = String.format("{\"deliveryId\": %d, \"lat\": %f, \"lng\": %f, \"timestamp\": %d}", 
                deliveryId, lat, lng, System.currentTimeMillis());
        sendMessage("delivery-locations", message);
    }
}
