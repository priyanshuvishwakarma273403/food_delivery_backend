package com.delivery.foodDelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long restaurantId;
    private String restaurantName;
    private List<OrderItemResponse> orderItems;
    private String status;
    private Double totalAmount;
    private String deliveryAddress;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentId;
    private String specialInstructions;
    private DeliveryResponse delivery;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
