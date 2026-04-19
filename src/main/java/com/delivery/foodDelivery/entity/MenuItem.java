package com.delivery.foodDelivery.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem extends BaseEntity {

    @Id
    private String id;

    private String name;
    
    private String description;
    
    private Double price;
    
    private String imageUrl;
    
    private String category;

    @Builder.Default
    private boolean available = true;

    private boolean vegetarian;

    private String restaurantId;
}
