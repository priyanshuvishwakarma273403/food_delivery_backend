package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.SaleEventDTO;
import com.delivery.foodDelivery.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
public class AdminSaleController {

    private final KafkaProducerService kafkaProducerService;

    /**
     * Trigger a sale event.
     * Accessible only by Admin.
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> startSale(@RequestBody SaleEventDTO saleEvent) {
        if (saleEvent.getSaleId() == null) {
            saleEvent.setSaleId(UUID.randomUUID().toString());
        }
        
        kafkaProducerService.sendSaleNotification(saleEvent);
        
        return ResponseEntity.ok("Sale event triggered successfully. Notifications are being sent to all users.");
    }
}
