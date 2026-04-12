package com.delivery.foodDelivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants",
        indexes = { @Index(name = "idx_restaurant_name", columnList = "name") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String cuisineType;

    @Column(nullable = false)
    private String phone;

    private String imageUrl;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "avg_delivery_time_minutes")
    private Integer avgDeliveryTime;

    @Column(name = "min_order_amount")
    private Double minOrderAmount;

    @Column(name = "is_open", nullable = false)
    @Builder.Default
    private boolean open = true;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    // One restaurant has many menu items
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

    // One restaurant has many orders
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();


}
