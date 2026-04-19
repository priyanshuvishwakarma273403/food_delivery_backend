package com.delivery.foodDelivery.entity;

import com.delivery.foodDelivery.enums.OrderStatus;
import com.delivery.foodDelivery.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "restaurant_id", nullable = false)
    private String restaurantId; // Reference to MongoDB Restaurant

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    private Double totalAmount;
    
    private Double coinsUsed;
    
    private Double finalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String paymentMethod;
    
    private String paymentId;

    private String specialInstructions;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Delivery delivery;
}
