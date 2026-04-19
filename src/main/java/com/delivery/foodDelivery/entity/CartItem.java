package com.delivery.foodDelivery.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    private String menuItemId;
    
    private String name;
    
    private String imageUrl;
    
    private Double price;
    
    private Integer quantity;

    public Double getSubtotal() {
        return price * quantity;
    }
}
