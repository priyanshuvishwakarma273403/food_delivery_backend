package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @GetMapping("/suggest/{userId}")
    public ResponseEntity<ApiResponse<String>> getSuggestion(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getSmartSuggestion(userId)));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody String message) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getChatbotResponse(message)));
    }
}
