package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.request.RestaurantRequest;
import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.dto.response.RestaurantResponse;
import com.delivery.foodDelivery.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Restaurant management endpoints.
 *
 * Public (GET):
 *   GET  /restaurants                    → All open restaurants
 *   GET  /restaurants/{id}               → Restaurant by ID
 *   GET  /restaurants/search?q=keyword   → Search by name/cuisine
 *   GET  /restaurants/city/{city}        → Filter by city
 *
 * Admin only (POST/PUT/DELETE):
 *   POST   /restaurants                  → Add restaurant
 *   PUT    /restaurants/{id}             → Update restaurant
 *   DELETE /restaurants/{id}             → Soft-delete restaurant
 *   PATCH  /restaurants/{id}/toggle      → Toggle open/closed
 */
@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // ── Public ────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAllRestaurants() {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getAllRestaurants()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getRestaurantById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> searchRestaurants(
            @RequestParam("q") String keyword) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.searchRestaurants(keyword)));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getByCity(
            @PathVariable String city) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getRestaurantsByCity(city)));
    }

    // ── Admin ─────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> addRestaurant(
            @Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse response = restaurantService.addRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Restaurant added successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
            @PathVariable String id,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.updateRestaurant(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(@PathVariable String id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant deleted successfully", null));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> toggleOpenStatus(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.toggleOpenStatus(id)));
    }
}
