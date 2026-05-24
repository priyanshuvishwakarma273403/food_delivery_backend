package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.entity.Wallet;
import com.delivery.foodDelivery.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Wallet>> getWalletByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWalletByUserId(userId)));
    }
}
