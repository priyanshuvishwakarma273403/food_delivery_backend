package com.delivery.foodDelivery.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String restaurantId;

    @Indexed
    private String menuItemId;

    private Long userId;

    private String userName;

    private String userImageUrl;

    private Double rating;

    private String comment;

    private LocalDateTime createdAt;

    @Builder.Default
    private boolean verifiedPurchase = false;
}
