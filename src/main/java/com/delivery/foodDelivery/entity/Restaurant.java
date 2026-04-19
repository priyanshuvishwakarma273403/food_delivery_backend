package com.delivery.foodDelivery.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String name;

    private String cuisineType;
    
    private String imageUrl;
    
    private Double rating;
    
    private Integer avgDeliveryTime;
    
    private String address;
    
    private String city;
    
    private String phone;
    
    private Double minOrderAmount;

    @Builder.Default
    private boolean open = true;

    @DBRef(lazy = true)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();
}
