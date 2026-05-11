package com.delivery.foodDelivery.repository.mongo;

import com.delivery.foodDelivery.entity.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByRestaurantId(String restaurantId);
    List<Review> findByMenuItemId(String menuItemId);
    List<Review> findByUserId(Long userId);
}
