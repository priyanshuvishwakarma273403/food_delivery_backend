package com.delivery.foodDelivery.entity;

import com.delivery.foodDelivery.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    private DeliveryPartner deliveryPartner;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private Double currentLatitude;
    
    private Double currentLongitude;

    private LocalDateTime assignedAt;
    
    private LocalDateTime acceptedAt;
    
    private LocalDateTime pickedUpAt;
    
    private LocalDateTime deliveredAt;

}
