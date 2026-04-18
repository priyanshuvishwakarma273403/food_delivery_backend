package com.delivery.foodDelivery.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    @Builder.Default
    private Double balance = 0.0; // Tomato Coins (1 Coin = 1 Rupee)

    @Column(name = "is_gold_member")
    @Builder.Default
    private boolean goldMember = false;

    public void addCoins(Double amount) {
        this.balance += amount;
    }

    public void spendCoins(Double amount) {
        if (this.balance < amount) {
            throw new RuntimeException("Insufficient Tomato Coins");
        }
        this.balance -= amount;
    }
}
