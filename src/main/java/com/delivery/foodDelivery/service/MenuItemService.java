package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.request.MenuItemRequest;
import com.delivery.foodDelivery.dto.response.MenuItemResponse;
import com.delivery.foodDelivery.entity.MenuItem;
import com.delivery.foodDelivery.entity.Restaurant;
import com.delivery.foodDelivery.exception.ResourceNotFoundException;
import com.delivery.foodDelivery.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantService restaurantService;

    public MenuItemResponse addMenuItem(String restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantService.findById(restaurantId);

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .restaurantId(restaurantId)
                .build();

        item = menuItemRepository.save(item);
        log.info("Menu item added: {} to restaurant {}", item.getName(), restaurantId);
        return toResponse(item, restaurant.getName());
    }

    public MenuItemResponse updateMenuItem(String itemId, MenuItemRequest request) {
        MenuItem item = findById(itemId);

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setImageUrl(request.getImageUrl());

        return toResponse(menuItemRepository.save(item), null);
    }

    public void deleteMenuItem(String itemId) {
        MenuItem item = findById(itemId);
        menuItemRepository.delete(item);
    }

    public MenuItemResponse toggleAvailability(String itemId) {
        MenuItem item = findById(itemId);
        item.setAvailable(!item.isAvailable());
        return toResponse(menuItemRepository.save(item), null);
    }

    public List<MenuItemResponse> getMenuByRestaurant(String restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId)
                .stream().map(item -> toResponse(item, null)).collect(Collectors.toList());
    }

    public MenuItem findById(String id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }

    public MenuItemResponse toResponse(MenuItem item, String restaurantName) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .imageUrl(item.getImageUrl())
                .available(item.isAvailable())
                .restaurantId(item.getRestaurantId())
                .restaurantName(restaurantName)
                .build();
    }
}
