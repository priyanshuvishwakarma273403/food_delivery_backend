package com.delivery.foodDelivery.entity;

import com.delivery.foodDelivery.enums.FoodCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_items",
        indexes = {
                @Index(name = "idx_menuitem_restaurant", columnList = "restaurant_id"),
                @Index(name = "idx_menuitem_category", columnList = "category")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodCategory category;

    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private boolean available = true;

    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 100; // Default stock level

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

}
