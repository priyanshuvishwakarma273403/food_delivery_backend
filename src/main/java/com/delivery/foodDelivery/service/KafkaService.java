package com.delivery.foodDelivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.delivery.foodDelivery.dto.SaleEventDTO;
import com.delivery.foodDelivery.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.listener.auto-startup:false}")
    private boolean kafkaEnabled;

    /**
     * Send a message to a specific topic
     * @param topic Topic name
     * @param message Message content (usually JSON string)
     */
    @Async
    public void sendMessage(String topic, String message) {
        if (!kafkaEnabled) {
            log.warn("Kafka is DISABLED. Skipping message to topic: {}", topic);
            return;
        }
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

    public void sendSaleNotification(SaleEventDTO saleEvent) {
        try {
            String json = objectMapper.writeValueAsString(saleEvent);
            sendMessage(KafkaConfig.SALE_TOPIC, json);
        } catch (Exception e) {
            log.error("Failed to serialize sale event: {}", e.getMessage());
        }
    }
}
