package com.delivery.foodDelivery.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of(
            "status", "UP",
            "message", "Tomato Server is awake!",
            "timestamp", String.valueOf(System.currentTimeMillis())
        );
    }
}
