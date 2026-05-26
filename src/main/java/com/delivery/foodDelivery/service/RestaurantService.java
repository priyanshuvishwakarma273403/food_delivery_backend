package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.request.RestaurantRequest;
import com.delivery.foodDelivery.dto.response.RestaurantResponse;
import com.delivery.foodDelivery.entity.MenuItem;
import com.delivery.foodDelivery.entity.Restaurant;
import com.delivery.foodDelivery.exception.ResourceNotFoundException;
import com.delivery.foodDelivery.repository.mongo.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantSearchService searchService;

    @Autowired
    public RestaurantService(RestaurantRepository restaurantRepository,
                             @Autowired(required = false) RestaurantSearchService searchService) {
        this.restaurantRepository = restaurantRepository;
        this.searchService = searchService;
    }

    private void syncToElasticsearch(Restaurant restaurant) {
        if (searchService == null) {
            log.debug("Elasticsearch sync skipped - search service not available");
            return;
        }
        try {
            com.delivery.foodDelivery.elasticsearch.RestaurantDocument doc = com.delivery.foodDelivery.elasticsearch.RestaurantDocument.builder()
                    .id(restaurant.getId())
                    .name(restaurant.getName())
                    .cuisineType(restaurant.getCuisineType())
                    .city(restaurant.getCity())
                    .rating(restaurant.getRating())
                    .menuItemNames(restaurant.getMenuItems().stream().map(MenuItem::getName).collect(Collectors.toList()))
                    .build();
            searchService.save(doc);
        } catch (Exception e) {
            log.error("Failed to sync restaurant to Elasticsearch: {}", e.getMessage());
        }
    }

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
        syncToElasticsearch(restaurant);
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

        Restaurant updated = restaurantRepository.save(restaurant);
        syncToElasticsearch(updated);
        return toResponse(updated);
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

    //@org.springframework.cache.annotation.Cacheable(value = "restaurants", key = "'all'")

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
