package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.request.MenuItemRequest;
import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.dto.response.MenuItemResponse;
import com.delivery.foodDelivery.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Menu item management endpoints.
 *
 * Public (GET):
 *   GET  /menu/restaurant/{restaurantId}                    → Full menu
 *   GET  /menu/restaurant/{restaurantId}/category/{cat}     → Filtered by VEG/NON_VEG etc.
 *
 * Admin only:
 *   POST   /menu/restaurant/{restaurantId}    → Add item
 *   PUT    /menu/{itemId}                     → Update item
 *   DELETE /menu/{itemId}                     → Mark unavailable
 *   PATCH  /menu/{itemId}/toggle              → Toggle availability
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    // ── Public ────────────────────────────────────

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenu(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(
                menuItemService.getMenuByRestaurant(restaurantId)));
    }

    @GetMapping("/restaurant/{restaurantId}/category/{category}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuByCategory(
            @PathVariable Long restaurantId,
            @PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(
                menuItemService.getMenuByCategory(restaurantId, category)));
    }

    // ── Admin ─────────────────────────────────────

    @PostMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> addMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse response = menuItemService.addMenuItem(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu item added", response));
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(menuItemService.updateMenuItem(itemId, request)));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long itemId) {
        menuItemService.deleteMenuItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Menu item removed", null));
    }

    @PatchMapping("/{itemId}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> toggleAvailability(
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(menuItemService.toggleAvailability(itemId)));
    }
}
