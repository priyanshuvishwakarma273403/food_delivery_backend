package com.delivery.foodDelivery.entity;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    private Double balance = 0.0;
    
    @Builder.Default
    private boolean goldMember = false;

    public void addCoins(Double amount) {
        this.balance += amount;
        checkGoldStatus();
    }

    public void spendCoins(Double amount) {
        if (this.balance < amount) {
            throw new RuntimeException("Insufficient Tomato Coins");
        }
        this.balance -= amount;
    }

    private void checkGoldStatus() {
        if (this.balance > 500 && !goldMember) {
            this.goldMember = true;
        }
    }
}
