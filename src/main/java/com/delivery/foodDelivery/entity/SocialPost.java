package com.delivery.foodDelivery.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "social_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String imageUrl;
    
    @Column(columnDefinition = "TEXT")
    private String caption;

    private int likes;
    
    @Builder.Default
    private boolean rewardClaimed = false;
}
