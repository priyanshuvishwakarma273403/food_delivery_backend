package com.delivery.foodDelivery.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;


    private String vehicleType;        // BIKE, BICYCLE, CAR
    private String vehicleNumber;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private boolean available = true;

    // Live location (updated via WebSocket)
    private Double currentLatitude;
    private Double currentLongitude;

    private Double rating;
    private Integer totalDeliveries;

}
