package com.delivery.foodDelivery.repository.mongo;

import com.delivery.foodDelivery.entity.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
    List<Restaurant> findByCity(String city);
    List<Restaurant> findByCuisineTypeContainingIgnoreCase(String cuisineType);
    List<Restaurant> findByNameContainingIgnoreCase(String name);
}
