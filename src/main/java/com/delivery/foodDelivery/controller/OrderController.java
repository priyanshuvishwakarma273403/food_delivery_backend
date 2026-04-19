package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.request.OrderRequest;
import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.dto.response.OrderResponse;
import com.delivery.foodDelivery.repository.jpa.UserRepository;
import com.delivery.foodDelivery.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Order lifecycle endpoints.
 *
 * Customer:
 *   POST   /orders                   → Place order (from cart)
 *   GET    /orders/my                → Order history
 *   GET    /orders/{id}              → Order details
 *   DELETE /orders/{id}/cancel       → Cancel order
 *
 * Admin:
 *   GET    /orders                               → All orders
 *   GET    /orders/restaurant/{restaurantId}     → Orders by restaurant
 *   PATCH  /orders/{id}/status?status=PREPARING  → Update order status
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    // ── Customer ──────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        Long userId = resolveUserId(userDetails);
        OrderResponse response = orderService.placeOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderHistory(userId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id, userId)));
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
                orderService.cancelOrder(id, userId)));
    }

    // ── Admin ─────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders()));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByRestaurant(
            @PathVariable String restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrdersByRestaurant(restaurantId)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.updateOrderStatus(id, status)));
    }

    // ── Helpers ───────────────────────────────────

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getId();
    }
}

