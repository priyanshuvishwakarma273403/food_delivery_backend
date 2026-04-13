package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.request.AiChatRequest;
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

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody AiChatRequest request) {
        String aiResponse = aiService.getChatResponse(request);
        return ResponseEntity.ok(ApiResponse.success(aiResponse));
    }
}
