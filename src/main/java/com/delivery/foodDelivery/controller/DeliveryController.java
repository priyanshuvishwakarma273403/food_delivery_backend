package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.request.DeliveryActionRequest;
import com.delivery.foodDelivery.dto.request.LocationUpdateRequest;
import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.dto.response.DeliveryResponse;
import com.delivery.foodDelivery.entity.DeliveryPartner;
import com.delivery.foodDelivery.repository.DeliveryPartnerRepository;
import com.delivery.foodDelivery.repository.UserRepository;
import com.delivery.foodDelivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Delivery tracking and management endpoints.
 *
 * Admin:
 *   POST  /delivery/assign/{orderId}/partner/{partnerId}    → Assign partner
 *   GET   /delivery/order/{orderId}                         → Get delivery info
 *
 * Delivery Partner:
 *   POST  /delivery/partner/order/{orderId}/action          → Accept / Reject
 *   POST  /delivery/partner/order/{orderId}/pickup          → Mark picked up
 *   POST  /delivery/partner/order/{orderId}/deliver         → Mark delivered
 *   POST  /delivery/partner/order/{orderId}/location        → Update live location (REST fallback)
 *   GET   /delivery/partner/my                              → My deliveries
 */
@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryPartnerRepository partnerRepository;
    private final UserRepository userRepository;

    // ── Admin ─────────────────────────────────────

    @PostMapping("/assign/{orderId}/partner/{partnerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryResponse>> assignPartner(
            @PathVariable Long orderId,
            @PathVariable Long partnerId) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryService.assignDeliveryPartner(orderId, partnerId)));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<DeliveryResponse>> getDelivery(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryService.getDeliveryByOrder(orderId)));
    }

    // ── Delivery Partner ──────────────────────────

    @PostMapping("/partner/order/{orderId}/action")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<DeliveryResponse>> handleAction(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DeliveryActionRequest request) {
        Long partnerId = resolvePartnerId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                deliveryService.handleDeliveryAction(orderId, partnerId, request)));
    }

    @PostMapping("/partner/order/{orderId}/pickup")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<DeliveryResponse>> markPickedUp(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long partnerId = resolvePartnerId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Order picked up",
                deliveryService.markPickedUp(orderId, partnerId)));
    }

    @PostMapping("/partner/order/{orderId}/deliver")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<DeliveryResponse>> markDelivered(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long partnerId = resolvePartnerId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Order delivered",
                deliveryService.markDelivered(orderId, partnerId)));
    }

    /**
     * REST fallback for location update.
     * Primary mechanism is WebSocket — see LocationWebSocketController.
     */
    @PostMapping("/partner/order/{orderId}/location")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LocationUpdateRequest request) {
        Long partnerId = resolvePartnerId(userDetails);
        deliveryService.updateLocation(orderId, partnerId, request);
        return ResponseEntity.ok(ApiResponse.success("Location updated", null));
    }

    @GetMapping("/partner/my")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> myDeliveries(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long partnerId = resolvePartnerId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                deliveryService.getDeliveriesByPartner(partnerId)));
    }

    // ── Helpers ───────────────────────────────────

    private Long resolvePartnerId(UserDetails userDetails) {
        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        DeliveryPartner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(
                        "Delivery partner profile not found for user: " + userId));
        return partner.getId();
    }
}
