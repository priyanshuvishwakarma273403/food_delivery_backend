package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.SaleEventDTO;
import com.delivery.foodDelivery.dto.request.SaleRequest;
import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.service.KafkaProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/sales")
@RequiredArgsConstructor
public class AdminSaleController {

    private final KafkaProducerService kafkaProducerService;

    /**
     * Trigger a sale event.
     * Accessible only by Admin.
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> startSale(@Valid @RequestBody SaleRequest request) {
        SaleEventDTO saleEvent = SaleEventDTO.builder()
                .saleId("SALE-" + UUID.randomUUID().toString().substring(0, 8))
                .title(request.getTitle())
                .message(request.getMessage())
                .discountPercentage(request.getDiscountPercentage())
                .promoCode(request.getPromoCode())
                .build();
        
        kafkaProducerService.sendSaleNotification(saleEvent);
        
        return ResponseEntity.ok(ApiResponse.success("Sale event triggered successfully. Notifications are being sent to all users.", null));
    }
}

