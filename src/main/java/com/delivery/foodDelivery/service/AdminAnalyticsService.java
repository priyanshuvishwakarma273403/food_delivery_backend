package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.entity.Order;
import com.delivery.foodDelivery.enums.OrderStatus;
import com.delivery.foodDelivery.enums.Role;
import com.delivery.foodDelivery.repository.jpa.OrderRepository;
import com.delivery.foodDelivery.repository.jpa.UserRepository;
import com.delivery.foodDelivery.repository.mongo.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalOrders = orderRepository.count();
        Double totalRevenue = orderRepository.getTotalRevenue();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long activeRestaurants = restaurantRepository.count();
        
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        stats.put("totalCustomers", totalCustomers);
        stats.put("activeRestaurants", activeRestaurants);
        
        // Status distribution
        Map<String, Long> statusDist = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            statusDist.put(status.name(), orderRepository.countByStatus(status));
        }
        stats.put("orderStatusDistribution", statusDist);
        
        return stats;
    }
}
