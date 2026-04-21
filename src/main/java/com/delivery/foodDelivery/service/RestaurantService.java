package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.request.RestaurantRequest;
import com.delivery.foodDelivery.dto.response.RestaurantResponse;
import com.delivery.foodDelivery.entity.Restaurant;
import com.delivery.foodDelivery.exception.ResourceNotFoundException;
import com.delivery.foodDelivery.repository.mongo.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

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

    public RestaurantResponse updateRestaurant(String id, RestaurantRequest request) {
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

    public void deleteRestaurant(String id) {
        Restaurant restaurant = findById(id);
        restaurantRepository.delete(restaurant); // Or keep active=false if using soft delete
        log.info("Restaurant deleted: id={}", id);
    }

    public RestaurantResponse toggleOpenStatus(String id) {
        Restaurant restaurant = findById(id);
        restaurant.setOpen(!restaurant.isOpen());
        return toResponse(restaurantRepository.save(restaurant));
    }

    @org.springframework.cache.annotation.Cacheable(value = "restaurants", key = "'all'")
    public List<RestaurantResponse> getAllRestaurants() {
        log.info("Fetching restaurants from database...");
        return restaurantRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }


    public List<RestaurantResponse> getRestaurantsByCity(String city) {
        return restaurantRepository.findByCity(city)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<RestaurantResponse> searchRestaurants(String keyword) {
        return restaurantRepository.findByNameContainingIgnoreCase(keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public RestaurantResponse getRestaurantById(String id) {
        return toResponse(findById(id));
    }

    public Restaurant findById(String id) {
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
                .cuisine(r.getCuisineType())
                .phone(r.getPhone())
                .imageUrl(r.getImageUrl())
                .image(r.getImageUrl())
                .rating(r.getRating())
                .avgDeliveryTime(r.getAvgDeliveryTime())
                .minOrderAmount(r.getMinOrderAmount())
                .open(r.isOpen())
                .createdAt(r.getCreatedDate())
                .build();
    }
}
