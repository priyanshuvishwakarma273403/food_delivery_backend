package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.elasticsearch.RestaurantDocument;
import com.delivery.foodDelivery.service.RestaurantSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final RestaurantSearchService searchService;

    @Autowired(required = false)
    public SearchController(RestaurantSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDocument>> search(@RequestParam String q) {
        if (searchService == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(searchService.searchRestaurants(q));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<RestaurantDocument>> autocomplete(@RequestParam String q) {
        if (searchService == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(searchService.autocomplete(q));
    }
}
