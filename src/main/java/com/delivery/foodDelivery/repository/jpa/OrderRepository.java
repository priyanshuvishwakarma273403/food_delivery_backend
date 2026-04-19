package com.delivery.foodDelivery.repository.jpa;

import com.delivery.foodDelivery.entity.Order;
import com.delivery.foodDelivery.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedDateDesc(Long customerId);
    List<Order> findByRestaurantIdOrderByCreatedDateDesc(String restaurantId); // Restaurant uses String ID
    List<Order> findByStatus(OrderStatus status);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithItems(Long id);
}
