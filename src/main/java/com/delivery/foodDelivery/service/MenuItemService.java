package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.request.MenuItemRequest;
import com.delivery.foodDelivery.dto.response.MenuItemResponse;
import com.delivery.foodDelivery.entity.MenuItem;
import com.delivery.foodDelivery.entity.Restaurant;
import com.delivery.foodDelivery.enums.FoodCategory;
import com.delivery.foodDelivery.exception.ResourceNotFoundException;
import com.delivery.foodDelivery.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantService restaurantService;

    // ──────────────────────────────────────────────
    // Admin Operations
    // ──────────────────────────────────────────────

    @Transactional
    public MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantService.findById(restaurantId);

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .menuCategory(request.getMenuCategory())
                .restaurant(restaurant)
                .build();

        item = menuItemRepository.save(item);
        log.info("Menu item added: {} to restaurant {}", item.getName(), restaurantId);
        return toResponse(item);
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long itemId, MenuItemRequest request) {
        MenuItem item = findById(itemId);

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setImageUrl(request.getImageUrl());
        item.setMenuCategory(request.getMenuCategory());

        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public void deleteMenuItem(Long itemId) {
        MenuItem item = findById(itemId);
        item.setAvailable(false);   // Soft-delete / mark unavailable
        menuItemRepository.save(item);
    }

    @Transactional
    public MenuItemResponse toggleAvailability(Long itemId) {
        MenuItem item = findById(itemId);
        item.setAvailable(!item.isAvailable());
        return toResponse(menuItemRepository.save(item));
    }

    // ──────────────────────────────────────────────
    // Customer Operations
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuByCategory(Long restaurantId, String category) {
        FoodCategory foodCategory = FoodCategory.valueOf(category.toUpperCase());
        return menuItemRepository.findByRestaurantIdAndCategory(restaurantId, foodCategory)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────

    public MenuItem findById(Long id) {
        return menuItemRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }

    public MenuItemResponse toResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory().name())
                .menuCategory(item.getMenuCategory())
                .imageUrl(item.getImageUrl())
                .available(item.isAvailable())
                .restaurantId(item.getRestaurant().getId())
                .restaurantName(item.getRestaurant().getName())
                .build();
    }
}
