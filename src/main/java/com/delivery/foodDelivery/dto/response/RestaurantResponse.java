package com.delivery.foodDelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantResponse {

    private Long id;
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
