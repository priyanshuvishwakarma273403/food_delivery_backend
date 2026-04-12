package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.request.DeliveryActionRequest;
import com.delivery.foodDelivery.dto.request.LocationUpdateRequest;
import com.delivery.foodDelivery.dto.response.DeliveryResponse;
import com.delivery.foodDelivery.dto.response.LocationMessage;
import com.delivery.foodDelivery.entity.Delivery;
import com.delivery.foodDelivery.entity.DeliveryPartner;
import com.delivery.foodDelivery.entity.Order;
import com.delivery.foodDelivery.enums.DeliveryStatus;
import com.delivery.foodDelivery.enums.OrderStatus;
import com.delivery.foodDelivery.exception.BusinessException;
import com.delivery.foodDelivery.exception.ResourceNotFoundException;
import com.delivery.foodDelivery.exception.UnauthorizedException;
import com.delivery.foodDelivery.repository.DeliveryPartnerRepository;
import com.delivery.foodDelivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages delivery partner assignment and real-time order tracking.
 * Broadcasts location updates via STOMP WebSocket.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final OrderService               orderService;
    private final SimpMessagingTemplate messagingTemplate;

    // ──────────────────────────────────────────────
    // Admin: Assign a delivery partner to an order
    // ──────────────────────────────────────────────

    @Transactional
    public DeliveryResponse assignDeliveryPartner(Long orderId, Long partnerId) {
        Order order = orderService.findById(orderId);

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP &&
                order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException(
                    "Cannot assign delivery partner to order in status: " + order.getStatus());
        }
        if (deliveryRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessException("Delivery partner already assigned to order: " + orderId);
        }

        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", partnerId));

        if (!partner.isAvailable()) {
            throw new BusinessException("Delivery partner is not available: " + partnerId);
        }

        Delivery delivery = Delivery.builder()
                .order(order)
                .deliveryPartner(partner)
                .status(DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();

        partner.setAvailable(false);
        partnerRepository.save(partner);

        Delivery saved = deliveryRepository.save(delivery);
        log.info("Delivery assigned: orderId={} partnerId={}", orderId, partnerId);
        return toResponse(saved);
    }

    // ──────────────────────────────────────────────
    // Delivery Partner: Accept or Reject assignment
    // ──────────────────────────────────────────────

    @Transactional
    public DeliveryResponse handleDeliveryAction(Long orderId, Long partnerId,
                                                 DeliveryActionRequest request) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery not found for order: " + orderId));

        if (!delivery.getDeliveryPartner().getId().equals(partnerId)) {
            throw new UnauthorizedException("This delivery is not assigned to you.");
        }

        String action = request.getAction().toUpperCase();

        switch (action) {
            case "ACCEPT" -> {
                delivery.setStatus(DeliveryStatus.ACCEPTED);
                delivery.setAcceptedAt(LocalDateTime.now());
                // Move order status forward
                orderService.updateOrderStatus(orderId, OrderStatus.OUT_FOR_DELIVERY.name());
                log.info("Delivery ACCEPTED: orderId={} partnerId={}", orderId, partnerId);
            }
            case "REJECT" -> {
                delivery.setStatus(DeliveryStatus.REJECTED);
                delivery.setRejectionReason(request.getRejectionReason());
                // Free the partner for re-assignment
                delivery.getDeliveryPartner().setAvailable(true);
                partnerRepository.save(delivery.getDeliveryPartner());
                log.info("Delivery REJECTED: orderId={} reason={}", orderId,
                        request.getRejectionReason());
            }
            default -> throw new BusinessException("Invalid action: " + action +
                    ". Must be ACCEPT or REJECT.");
        }

        return toResponse(deliveryRepository.save(delivery));
    }

    // ──────────────────────────────────────────────
    // Delivery Partner: Mark as Picked Up / Delivered
    // ──────────────────────────────────────────────

    @Transactional
    public DeliveryResponse markPickedUp(Long orderId, Long partnerId) {
        Delivery delivery = getValidatedDelivery(orderId, partnerId);
        if (delivery.getStatus() != DeliveryStatus.ACCEPTED) {
            throw new BusinessException("Order must be ACCEPTED before marking picked up.");
        }
        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickedUpAt(LocalDateTime.now());
        log.info("Order {} picked up by partner {}", orderId, partnerId);
        return toResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse markDelivered(Long orderId, Long partnerId) {
        Delivery delivery = getValidatedDelivery(orderId, partnerId);
        if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new BusinessException("Order must be PICKED_UP before marking delivered.");
        }
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());

        // Free partner for next order
        delivery.getDeliveryPartner().setAvailable(true);
        DeliveryPartner partner = delivery.getDeliveryPartner();
        partner.setTotalDeliveries(
                partner.getTotalDeliveries() == null ? 1 : partner.getTotalDeliveries() + 1);
        partnerRepository.save(partner);

        // Mark order delivered
        orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED.name());
        log.info("Order {} delivered by partner {}", orderId, partnerId);
        return toResponse(deliveryRepository.save(delivery));
    }

    // ──────────────────────────────────────────────
    // Real-time Location Update (WebSocket broadcast)
    // ──────────────────────────────────────────────

    @Transactional
    public void updateLocation(Long orderId, Long partnerId, LocationUpdateRequest request) {
        Delivery delivery = getValidatedDelivery(orderId, partnerId);

        // Persist latest coordinates
        delivery.setCurrentLatitude(request.getLatitude());
        delivery.setCurrentLongitude(request.getLongitude());
        deliveryRepository.save(delivery);

        // Also update partner's live coordinates
        delivery.getDeliveryPartner().setCurrentLatitude(request.getLatitude());
        delivery.getDeliveryPartner().setCurrentLongitude(request.getLongitude());
        partnerRepository.save(delivery.getDeliveryPartner());

        // Broadcast over WebSocket: /topic/location/{orderId}
        LocationMessage message = LocationMessage.builder()
                .orderId(orderId)
                .deliveryPartnerId(partnerId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(delivery.getStatus().name())
                .build();

        messagingTemplate.convertAndSend("/topic/location/" + orderId, message);
        log.debug("Location broadcast: orderId={} [{}, {}]", orderId,
                request.getLatitude(), request.getLongitude());
    }

    // ──────────────────────────────────────────────
    // Queries
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrder(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery not found for order: " + orderId));
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByPartner(Long partnerId) {
        return deliveryRepository.findByDeliveryPartnerId(partnerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getActiveDeliveriesByPartner(Long partnerId) {
        return deliveryRepository
                .findByDeliveryPartnerIdAndStatus(partnerId, DeliveryStatus.ACCEPTED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────

    private Delivery getValidatedDelivery(Long orderId, Long partnerId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery not found for order: " + orderId));
        if (!delivery.getDeliveryPartner().getId().equals(partnerId)) {
            throw new UnauthorizedException("This delivery is not assigned to you.");
        }
        return delivery;
    }

    private DeliveryResponse toResponse(Delivery d) {
        DeliveryPartner partner = d.getDeliveryPartner();
        return DeliveryResponse.builder()
                .id(d.getId())
                .orderId(d.getOrder().getId())
                .deliveryPartnerId(partner != null ? partner.getId() : null)
                .deliveryPartnerName(partner != null ? partner.getUser().getName() : null)
                .deliveryPartnerPhone(partner != null ? partner.getUser().getPhone() : null)
                .status(d.getStatus().name())
                .currentLatitude(d.getCurrentLatitude())
                .currentLongitude(d.getCurrentLongitude())
                .assignedAt(d.getAssignedAt())
                .acceptedAt(d.getAcceptedAt())
                .pickedUpAt(d.getPickedUpAt())
                .deliveredAt(d.getDeliveredAt())
                .build();
    }
}
