package com.delivery.foodDelivery.repository;

import com.delivery.foodDelivery.entity.Order;
import com.delivery.foodDelivery.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByCustomerIdOrderByCreatedDateDesc(Long customerId);

    List<Order> findByRestaurantIdOrderByCreatedDateDesc(Long restaurantId);

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.status = :status")
    List<Order> findByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                          @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.menuItem WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

}
