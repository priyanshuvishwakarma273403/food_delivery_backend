package com.delivery.foodDelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;  // CARD, UPI, COD

    private String specialInstructions;

    // For dummy/Razorpay payment integration
    private String paymentToken;

    private boolean useCoins; // Use Tomato Coins for this order
}
