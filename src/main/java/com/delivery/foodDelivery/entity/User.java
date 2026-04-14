package com.delivery.foodDelivery.entity;

import com.delivery.foodDelivery.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email", unique = true),
                @Index(name = "idx_user_phone", columnList = "phone")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    // Delivery address for customers
    private String address;

    // One customer can have one cart
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    // Wallet for Loyalty Coins
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Wallet wallet;

    // Order history
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

}
