package com.delivery.foodDelivery.repository.elasticsearch;

import com.delivery.foodDelivery.elasticsearch.RestaurantDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantSearchRepository extends ElasticsearchRepository<RestaurantDocument, String> {
    List<RestaurantDocument> findByNameContainingIgnoreCase(String name);
    List<RestaurantDocument> findByCuisineType(String cuisineType);
}
