package com.delivery.foodDelivery.repository;

import com.delivery.foodDelivery.entity.MenuItem;
import com.delivery.foodDelivery.enums.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem,Integer> {

    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, FoodCategory category);

    List<MenuItem> findByRestaurantIdAndMenuCategory(Long restaurantId, String menuCategory);

}
