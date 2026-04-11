package com.delivery.foodDelivery.repository;

import com.delivery.foodDelivery.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByActiveTrueAndOpenTrue();

    List<Restaurant> findByCity(String city);

    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND " +
            "(LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Restaurant> searchRestaurants(@Param("keyword") String keyword);

    List<Restaurant> findByCityAndActiveTrueAndOpenTrue(String city);


}
