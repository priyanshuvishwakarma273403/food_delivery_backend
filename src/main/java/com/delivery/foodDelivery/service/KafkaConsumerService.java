package com.delivery.foodDelivery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    /**
     * Listen for Order Status updates to trigger real-time notifications
     */
    @KafkaListener(topics = "order-updates", groupId = "food-delivery-group")
    public void consumeOrderUpdate(String message) {
        log.info("[NOTIFICATION SERVICE] Processing order update: {}", message);
        // Step 1: Parse JSON
        // Step 2: Find User's active Socket/FCM token
        // Step 3: PUSH notification: "Order Status changed!"
    }

    /**
     * Listen for delivery location streams to feed into an Analytics Engine
     */
    @KafkaListener(topics = "delivery-locations", groupId = "food-delivery-group")
    public void consumeLocationUpdate(String message) {
        log.debug("[ANALYTICS] Tracking delivery movement: {}", message);
        // Step 1: Store in Time-series DB (InfluxDB/Prometheus)
        // Step 2: Recalculate ETA (Estimated Time of Arrival)
    }

    /**
     * Listen for User Analytics (Login/Clicks) to build user profiles
     */
    @KafkaListener(topics = "user-analytics", groupId = "food-delivery-group")
    public void consumeUserAnalytics(String message) {
        log.info("[MARKETING ENGINE] Analyzing user behavior: {}", message);
        // Logic: If user logged in after 7 days, send "We missed you!" coupon
    }
}
