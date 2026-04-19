package com.delivery.foodDelivery.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String vehicleNumber;

    private String vehicleType;

    @Builder.Default
    private boolean available = true;

    private String currentCity;

    private Integer totalDeliveries;

    private Double currentLatitude;

    private Double currentLongitude;
}
