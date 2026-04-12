package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.request.RestaurantRequest;
import com.delivery.foodDelivery.dto.response.RestaurantResponse;
import com.delivery.foodDelivery.entity.Restaurant;
import com.delivery.foodDelivery.exception.ResourceNotFoundException;
import com.delivery.foodDelivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Transactional
    public RestaurantResponse addRestaurant(RestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .phone(request.getPhone())
                .imageUrl(request.getImageUrl())
                .avgDeliveryTime(request.getAvgDeliveryTime())
                .minOrderAmount(request.getMinOrderAmount())
                .build();

        restaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant added: {} [id={}]", restaurant.getName(), restaurant.getId());
        return toResponse(restaurant);
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = findById(id);

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setPhone(request.getPhone());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setAvgDeliveryTime(request.getAvgDeliveryTime());
        restaurant.setMinOrderAmount(request.getMinOrderAmount());

        return toResponse(restaurantRepository.save(restaurant));
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = findById(id);
        restaurant.setActive(false);    // Soft delete
        restaurantRepository.save(restaurant);
        log.info("Restaurant soft-deleted: id={}", id);
    }

    @Transactional
    public RestaurantResponse toggleOpenStatus(Long id) {
        Restaurant restaurant = findById(id);
        restaurant.setOpen(!restaurant.isOpen());
        return toResponse(restaurantRepository.save(restaurant));
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllOpenRestaurants() {
        return restaurantRepository.findByActiveTrueAndOpenTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getRestaurantsByCity(String city) {
        return restaurantRepository.findByCityAndActiveTrueAndOpenTrue(city)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchRestaurants(String keyword) {
        return restaurantRepository.searchRestaurants(keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        return toResponse(findById(id));
    }

    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));
    }

    public RestaurantResponse toResponse(Restaurant r) {
        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .address(r.getAddress())
                .city(r.getCity())
                .cuisineType(r.getCuisineType())
                .phone(r.getPhone())
                .imageUrl(r.getImageUrl())
                .rating(r.getRating())
                .avgDeliveryTime(r.getAvgDeliveryTime())
                .minOrderAmount(r.getMinOrderAmount())
                .open(r.isOpen())
                .createdAt(r.getCreatedAt())
                .build();
    }

}
