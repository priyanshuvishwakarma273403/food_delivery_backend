package com.delivery.foodDelivery.dto.request;

import com.delivery.foodDelivery.enums.FoodCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MenuItemRequest {
    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Category is required")
    private FoodCategory category;

    private String imageUrl;

    private String menuCategory;
}
