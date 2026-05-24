package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.entity.Review;
import com.delivery.foodDelivery.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.addReview(review));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Review>> getRestaurantReviews(@PathVariable String restaurantId) {
        return ResponseEntity.ok(reviewService.getReviewsByRestaurant(restaurantId));
    }

    @GetMapping("/item/{menuItemId}")
    public ResponseEntity<List<Review>> getMenuItemReviews(@PathVariable String menuItemId) {
        return ResponseEntity.ok(reviewService.getReviewsByMenuItem(menuItemId));
    }
}
