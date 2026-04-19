package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.request.CartItemRequest;
import com.delivery.foodDelivery.dto.response.CartItemResponse;
import com.delivery.foodDelivery.dto.response.CartResponse;
import com.delivery.foodDelivery.entity.Cart;
import com.delivery.foodDelivery.entity.CartItem;
import com.delivery.foodDelivery.entity.MenuItem;
import com.delivery.foodDelivery.entity.Restaurant;
import com.delivery.foodDelivery.exception.BusinessException;
import com.delivery.foodDelivery.exception.ResourceNotFoundException;
import com.delivery.foodDelivery.repository.mongo.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final MenuItemService menuItemService;
    private final RestaurantService restaurantService;

    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cart);
    }

    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        MenuItem menuItem = menuItemService.findById(request.getMenuItemId());

        if (!menuItem.isAvailable()) {
            throw new BusinessException("Menu item is currently unavailable: " + menuItem.getName());
        }

        if (cart.getRestaurantId() != null &&
                !cart.getRestaurantId().equals(menuItem.getRestaurantId())) {
            throw new BusinessException(
                    "Cart already has items from another restaurant. Clear cart first.");
        }

        if (cart.getRestaurantId() == null) {
            cart.setRestaurantId(menuItem.getRestaurantId());
        }

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getMenuItemId().equals(menuItem.getId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .menuItemId(menuItem.getId())
                    .name(menuItem.getName())
                    .imageUrl(menuItem.getImageUrl())
                    .price(menuItem.getPrice())
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        return toResponse(cartRepository.save(cart));
    }

    public CartResponse updateItemQuantity(Long userId, String menuItemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(i -> i.getMenuItemId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        if (quantity <= 0) {
            cart.getItems().remove(cartItem);
            if (cart.getItems().isEmpty()) cart.setRestaurantId(null);
        } else {
            cartItem.setQuantity(quantity);
        }

        return toResponse(cartRepository.save(cart));
    }

    public CartResponse removeItem(Long userId, String menuItemId) {
        return updateItemQuantity(userId, menuItemId, 0);
    }

    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clear();
        cartRepository.save(cart);
    }

    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = Cart.builder().userId(userId).build();
            return cartRepository.save(cart);
        });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(i -> CartItemResponse.builder()
                        .menuItemId(i.getMenuItemId())
                        .menuItemName(i.getName())
                        .menuItemImage(i.getImageUrl())
                        .unitPrice(i.getPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        String restaurantName = null;
        if (cart.getRestaurantId() != null) {
            try {
                Restaurant r = restaurantService.findById(cart.getRestaurantId());
                restaurantName = r.getName();
            } catch(Exception e) {
                log.warn("Restaurant not found for cart: " + cart.getId());
            }
        }

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .restaurantId(cart.getRestaurantId())
                .restaurantName(restaurantName)
                .items(itemResponses)
                .totalPrice(cart.getTotalAmount())
                .totalItems(cart.getItems().size())
                .build();
    }
}
