package com.delivery.foodDelivery.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @Id
    private String id;

    private Long userId;

    private String restaurantId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public void clear() {
        this.items.clear();
        this.restaurantId = null;
    }

    public Double getTotalAmount() {
        return items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
    }
}
