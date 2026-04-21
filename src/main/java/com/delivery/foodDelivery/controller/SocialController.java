package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.entity.SocialPost;
import com.delivery.foodDelivery.service.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<List<SocialPost>>> getFeed() {
        return ResponseEntity.ok(ApiResponse.success(socialService.getFeed()));
    }

    @PostMapping("/post")
    public ResponseEntity<ApiResponse<SocialPost>> createPost(
            @RequestParam Long userId,
            @RequestParam String imageUrl,
            @RequestParam String caption) {
        return ResponseEntity.ok(ApiResponse.success("Post created and 50 coins rewarded!", socialService.createPost(userId, imageUrl, caption)));
    }
}
