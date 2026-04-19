package com.delivery.foodDelivery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemResponse {
    private String id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
    private String image; // Alias for frontend
    private boolean available;
    private String restaurantId;
    private String restaurantName;
}
