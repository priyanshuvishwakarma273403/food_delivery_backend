package com.delivery.foodDelivery.websocket;

import com.delivery.foodDelivery.dto.request.LocationUpdateRequest;
import com.delivery.foodDelivery.dto.response.LocationMessage;
import com.delivery.foodDelivery.entity.DeliveryPartner;
import com.delivery.foodDelivery.repository.jpa.DeliveryPartnerRepository;
import com.delivery.foodDelivery.repository.jpa.UserRepository;
import com.delivery.foodDelivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket STOMP controller for real-time delivery tracking.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  Client (delivery partner app) connects to:                     │
 * │    ws://host/api/ws  (with SockJS fallback)                     │
 * │                                                                  │
 * │  Sends location to:                                             │
 * │    /app/location/{orderId}   ← @MessageMapping here             │
 * │                                                                  │
 * │  Customers subscribe to:                                        │
 * │    /topic/location/{orderId} ← broadcast destination            │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * Message format (JSON):
 * {
 *   "latitude": 28.6139,
 *   "longitude": 77.2090
 * }
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LocationWebSocketController {

    private final DeliveryService deliveryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final DeliveryPartnerRepository partnerRepository;

    /**
     * Delivery partner sends their GPS coordinates.
     * The service persists and broadcasts to /topic/location/{orderId}.
     *
     * @param orderId   order being tracked
     * @param payload   { latitude, longitude }
     * @param headerAccessor  STOMP headers (contains authenticated principal)
     */
    @MessageMapping("/location/{orderId}")
    public void updateLocation(
            @DestinationVariable Long orderId,
            @Payload LocationUpdateRequest payload,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            // Resolve delivery partner from session principal
            String email = headerAccessor.getUser() != null
                    ? headerAccessor.getUser().getName()
                    : null;

            if (email == null) {
                log.warn("Unauthenticated WebSocket location update for orderId={}", orderId);
                return;
            }

            Long userId = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email))
                    .getId();

            DeliveryPartner partner = partnerRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException(
                            "Partner profile not found for user: " + userId));

            // Delegate to service — persists and broadcasts
            deliveryService.updateLocation(orderId, partner.getId(), payload);

        } catch (Exception e) {
            log.error("Error processing WebSocket location update for orderId={}: {}",
                    orderId, e.getMessage());

            // Send error back to the sender only
            LocationMessage error = LocationMessage.builder()
                    .orderId(orderId)
                    .status("ERROR: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            messagingTemplate.convertAndSend("/topic/location/" + orderId + "/error", error);
        }
    }
}
