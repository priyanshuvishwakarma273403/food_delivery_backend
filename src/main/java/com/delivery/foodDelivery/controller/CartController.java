package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.request.CartItemRequest;
import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.dto.response.CartResponse;
import com.delivery.foodDelivery.entity.User;
import com.delivery.foodDelivery.repository.UserRepository;
import com.delivery.foodDelivery.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Cart management for authenticated CUSTOMER users.
 *
 * GET    /cart                             → Get current cart
 * POST   /cart/items                       → Add item to cart
 * PUT    /cart/items/{menuItemId}?qty=N    → Update quantity
 * DELETE /cart/items/{menuItemId}          → Remove item
 * DELETE /cart                             → Clear entire cart
 */

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(userId)));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart",
                cartService.addItem(userId, request)));
    }

    @PutMapping("/items/{menuItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long menuItemId,
            @RequestParam Integer quantity) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                cartService.updateItemQuantity(userId, menuItemId, quantity)));
    }

    @DeleteMapping("/items/{menuItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long menuItemId) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Item removed",
                cartService.removeItem(userId, menuItemId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(resolveUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    // ── Helpers ───────────────────────────────────

    private Long resolveUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return user.getId();
    }
}
