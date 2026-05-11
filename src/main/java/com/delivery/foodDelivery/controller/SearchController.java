package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.elasticsearch.RestaurantDocument;
import com.delivery.foodDelivery.service.RestaurantSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final RestaurantSearchService searchService;

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDocument>> search(@RequestParam String q) {
        return ResponseEntity.ok(searchService.searchRestaurants(q));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<RestaurantDocument>> autocomplete(@RequestParam String q) {
        return ResponseEntity.ok(searchService.autocomplete(q));
    }
}
