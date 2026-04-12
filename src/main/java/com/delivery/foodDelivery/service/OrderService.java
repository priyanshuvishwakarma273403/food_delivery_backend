package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.request.OrderRequest;
import com.delivery.foodDelivery.dto.response.DeliveryResponse;
import com.delivery.foodDelivery.dto.response.OrderItemResponse;
import com.delivery.foodDelivery.dto.response.OrderResponse;
import com.delivery.foodDelivery.dto.response.PaymentResponse;
import com.delivery.foodDelivery.entity.*;
import com.delivery.foodDelivery.enums.OrderStatus;
import com.delivery.foodDelivery.enums.PaymentStatus;
import com.delivery.foodDelivery.exception.BusinessException;
import com.delivery.foodDelivery.exception.ResourceNotFoundException;
import com.delivery.foodDelivery.exception.UnauthorizedException;
import com.delivery.foodDelivery.repository.OrderRepository;
import com.delivery.foodDelivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Core order lifecycle management:
 *  - Place order from cart
 *  - Update order status (admin / restaurant)
 *  - Cancel order with refund
 *  - Order history for customer and admin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService         cartService;
    private final PaymentService      paymentService;

    // ──────────────────────────────────────────────
    // Place Order
    // ──────────────────────────────────────────────

    @Transactional
    public OrderResponse placeOrder(Long customerId, OrderRequest request) {

        // 1. Load and validate cart
        Cart cart = cartService.getOrCreateCart(customerId);
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot place order: cart is empty.");
        }

        Restaurant restaurant = cart.getRestaurant();
        if (!restaurant.isOpen()) {
            throw new BusinessException("Restaurant is currently closed.");
        }

        double total = cart.getTotalPrice();
        if (total < restaurant.getMinOrderAmount()) {
            throw new BusinessException(
                    "Minimum order amount is ₹" + restaurant.getMinOrderAmount());
        }

        // 2. Process payment
        PaymentResponse payment = paymentService.processPayment(
                null, total, request.getPaymentMethod(), request.getPaymentToken());

        if (PaymentStatus.FAILED.name().equals(payment.getStatus())) {
            throw new BusinessException("Payment failed: " + payment.getMessage());
        }

        // 3. Build Order entity
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", customerId));

        Order order = Order.builder()
                .customer(customer)
                .restaurant(restaurant)
                .deliveryAddress(request.getDeliveryAddress())
                .totalAmount(total)
                .paymentStatus(PaymentStatus.valueOf(payment.getStatus()))
                .paymentMethod(request.getPaymentMethod())
                .paymentId(payment.getPaymentId())
                .specialInstructions(request.getSpecialInstructions())
                .status(OrderStatus.PLACED)
                .build();

        // 4. Convert cart items → order items (price snapshot)
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(ci -> OrderItem.builder()
                        .order(order)
                        .menuItem(ci.getMenuItem())
                        .quantity(ci.getQuantity())
                        .priceAtOrderTime(ci.getMenuItem().getPrice())
                        .build())
                .collect(Collectors.toList());
        order.setOrderItems(orderItems);

        Order saved = orderRepository.save(order);

        // 5. Clear cart
        cartService.clearCart(customerId);

        log.info("Order placed: id={} customer={} amount={}", saved.getId(), customerId, total);
        return toResponse(saved);
    }

    // ──────────────────────────────────────────────
    // Status Updates (Admin / Restaurant)
    // ──────────────────────────────────────────────

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String newStatusStr) {
        Order order = findById(orderId);
        OrderStatus newStatus = parseStatus(newStatusStr);
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        log.info("Order {} status → {}", orderId, newStatus);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long requestingUserId) {
        Order order = findById(orderId);

        // Only the customer who placed it or admin can cancel
        if (!order.getCustomer().getId().equals(requestingUserId)) {
            throw new UnauthorizedException("You are not allowed to cancel this order.");
        }

        if (order.getStatus() == OrderStatus.OUT_FOR_DELIVERY ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel order that is already " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Issue refund if already paid
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            paymentService.processRefund(order.getPaymentId(), order.getTotalAmount());
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        log.info("Order {} cancelled by user {}", orderId, requestingUserId);
        return toResponse(orderRepository.save(order));
    }

    // ──────────────────────────────────────────────
    // Queries
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long requestingUserId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Customers can only view their own orders
        if (!order.getCustomer().getId().equals(requestingUserId)) {
            throw new UnauthorizedException("Access denied to order: " + orderId);
        }
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    private OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid order status: " + status);
        }
    }

    /**
     * Enforces a forward-only state machine for order status.
     * PLACED → CONFIRMED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot change status of a " + current + " order.");
        }
        if (next == OrderStatus.CANCELLED) return;  // cancellation allowed from most states

        int currentOrdinal = current.ordinal();
        int nextOrdinal    = next.ordinal();
        if (nextOrdinal != currentOrdinal + 1) {
            throw new BusinessException(
                    "Invalid status transition: " + current + " → " + next);
        }
    }

    public OrderResponse toResponse(Order o) {
        List<OrderItemResponse> itemResponses = o.getOrderItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .menuItemId(i.getMenuItem().getId())
                        .menuItemName(i.getMenuItem().getName())
                        .menuItemImage(i.getMenuItem().getImageUrl())
                        .quantity(i.getQuantity())
                        .priceAtOrderTime(i.getPriceAtOrderTime())
                        .subtotal(i.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        // Map delivery if present
        DeliveryResponse deliveryResponse = null;
        if (o.getDelivery() != null) {
            Delivery d = o.getDelivery();
            deliveryResponse = DeliveryResponse.builder()
                    .id(d.getId())
                    .orderId(o.getId())
                    .deliveryPartnerId(d.getDeliveryPartner() != null
                            ? d.getDeliveryPartner().getId() : null)
                    .deliveryPartnerName(d.getDeliveryPartner() != null
                            ? d.getDeliveryPartner().getUser().getName() : null)
                    .deliveryPartnerPhone(d.getDeliveryPartner() != null
                            ? d.getDeliveryPartner().getUser().getPhone() : null)
                    .status(d.getStatus().name())
                    .currentLatitude(d.getCurrentLatitude())
                    .currentLongitude(d.getCurrentLongitude())
                    .assignedAt(d.getAssignedAt())
                    .acceptedAt(d.getAcceptedAt())
                    .pickedUpAt(d.getPickedUpAt())
                    .deliveredAt(d.getDeliveredAt())
                    .build();
        }

        return OrderResponse.builder()
                .id(o.getId())
                .customerId(o.getCustomer().getId())
                .customerName(o.getCustomer().getName())
                .restaurantId(o.getRestaurant().getId())
                .restaurantName(o.getRestaurant().getName())
                .orderItems(itemResponses)
                .status(o.getStatus().name())
                .totalAmount(o.getTotalAmount())
                .deliveryAddress(o.getDeliveryAddress())
                .paymentStatus(o.getPaymentStatus().name())
                .paymentMethod(o.getPaymentMethod())
                .paymentId(o.getPaymentId())
                .specialInstructions(o.getSpecialInstructions())
                .delivery(deliveryResponse)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
