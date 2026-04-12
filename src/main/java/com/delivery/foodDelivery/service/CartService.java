package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.request.CartItemRequest;
import com.delivery.foodDelivery.dto.response.CartItemResponse;
import com.delivery.foodDelivery.dto.response.CartResponse;
import com.delivery.foodDelivery.entity.Cart;
import com.delivery.foodDelivery.entity.CartItem;
import com.delivery.foodDelivery.entity.MenuItem;
import com.delivery.foodDelivery.entity.User;
import com.delivery.foodDelivery.exception.BusinessException;
import com.delivery.foodDelivery.exception.ResourceNotFoundException;
import com.delivery.foodDelivery.repository.CartRepository;
import com.delivery.foodDelivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final MenuItemService menuItemService;

    // ──────────────────────────────────────────────
    // Cart Operations
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        MenuItem menuItem = menuItemService.findById(request.getMenuItemId());

        if (!menuItem.isAvailable()) {
            throw new BusinessException("Menu item is currently unavailable: " + menuItem.getName());
        }

        // Enforce single-restaurant constraint
        if (cart.getRestaurant() != null &&
                !cart.getRestaurant().getId().equals(menuItem.getRestaurant().getId())) {
            throw new BusinessException(
                    "Cart already has items from another restaurant. Clear cart first.");
        }

        // Set restaurant if cart is empty
        if (cart.getRestaurant() == null) {
            cart.setRestaurant(menuItem.getRestaurant());
        }

        // Update quantity if item exists, else add new
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItemQuantity(Long userId, Long menuItemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(i -> i.getMenuItem().getId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        if (quantity <= 0) {
            cart.getItems().remove(cartItem);
            if (cart.getItems().isEmpty()) cart.setRestaurant(null);
        } else {
            cartItem.setQuantity(quantity);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long menuItemId) {
        return updateItemQuantity(userId, menuItemId, 0);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clear();
        cartRepository.save(cart);
    }

    // ──────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────

    Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            Cart cart = Cart.builder().user(user).build();
            return cartRepository.save(cart);
        });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(i -> CartItemResponse.builder()
                        .id(i.getId())
                        .menuItemId(i.getMenuItem().getId())
                        .menuItemName(i.getMenuItem().getName())
                        .menuItemImage(i.getMenuItem().getImageUrl())
                        .unitPrice(i.getMenuItem().getPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .restaurantId(cart.getRestaurant() != null ? cart.getRestaurant().getId() : null)
                .restaurantName(cart.getRestaurant() != null ? cart.getRestaurant().getName() : null)
                .items(itemResponses)
                .totalPrice(cart.getTotalPrice())
                .totalItems(cart.getItems().size())
                .build();
    }

}
