package com.delivery.foodDelivery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {
    private String id;
    private String name;
    private String address;
    private String city;
    private String cuisineType;
    private String phone;
    private String imageUrl;
    private Double rating;
    private Integer avgDeliveryTime;
    private Double minOrderAmount;
    private boolean open;
    private LocalDateTime createdAt;
}
