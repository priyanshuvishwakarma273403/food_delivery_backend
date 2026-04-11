package com.delivery.foodDelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String cuisineType;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String imageUrl;

    private Integer avgDeliveryTime;

    @NotNull(message = "Minimum order amount is required")
    @Positive(message = "Minimum order amount must be positive")
    private Double minOrderAmount;
}
