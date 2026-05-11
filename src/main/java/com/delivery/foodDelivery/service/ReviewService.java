package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.entity.Restaurant;
import com.delivery.foodDelivery.entity.Review;
import com.delivery.foodDelivery.entity.User;
import com.delivery.foodDelivery.repository.mongo.RestaurantRepository;
import com.delivery.foodDelivery.repository.mongo.ReviewRepository;
import com.delivery.foodDelivery.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public Review addReview(Review review) {
        review.setCreatedAt(LocalDateTime.now());
        
        // Fetch user info if not provided
        if (review.getUserId() != null && review.getUserName() == null) {
            userRepository.findById(review.getUserId()).ifPresent(user -> {
                review.setUserName(user.getName());
            });
        }

        Review savedReview = reviewRepository.save(review);

        // Update restaurant average rating if it's a restaurant review
        if (review.getRestaurantId() != null) {
            updateRestaurantRating(review.getRestaurantId());
        }

        return savedReview;
    }

    public List<Review> getReviewsByRestaurant(String restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId);
    }

    public List<Review> getReviewsByMenuItem(String menuItemId) {
        return reviewRepository.findByMenuItemId(menuItemId);
    }

    private void updateRestaurantRating(String restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantId(restaurantId);
        if (reviews.isEmpty()) return;

        double avgRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        restaurantRepository.findById(restaurantId).ifPresent(restaurant -> {
            restaurant.setRating(avgRating);
            restaurantRepository.save(restaurant);
        });
    }
}
