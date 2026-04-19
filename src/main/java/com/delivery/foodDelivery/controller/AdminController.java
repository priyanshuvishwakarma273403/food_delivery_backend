package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.SaleEventDTO;
import com.delivery.foodDelivery.dto.request.SaleRequest;
import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.service.KafkaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sales")
@RequiredArgsConstructor
public class AdminController {

    private final KafkaService kafkaService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> startSale(@Valid @RequestBody SaleRequest request) {
        SaleEventDTO saleEvent = SaleEventDTO.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .discountPercentage(request.getDiscountPercentage())
                .promoCode(request.getPromoCode())
                .saleId("SALE-" + System.currentTimeMillis())
                .build();
                
        kafkaService.sendSaleNotification(saleEvent);
        return ResponseEntity.ok(ApiResponse.success("Sale event triggered successfully! Emails will be sent to all users.", null));
    }
}
