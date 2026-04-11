package com.delivery.foodDelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;      // FoodCategory enum name
    private String menuCategory;
    private String imageUrl;
    private boolean available;
    private Long restaurantId;
    private String restaurantName;

}
