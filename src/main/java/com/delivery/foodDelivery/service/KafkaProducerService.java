package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.config.KafkaConfig;
import com.delivery.foodDelivery.dto.SaleEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, SaleEventDTO> kafkaTemplate;

    public void sendSaleNotification(SaleEventDTO saleEvent) {
        log.info("Producing sale event asynchronously: {}", saleEvent.getTitle());
        
        CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(KafkaConfig.SALE_TOPIC, saleEvent.getSaleId(), saleEvent)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent sale event to Kafka: {}", saleEvent.getSaleId());
                        } else {
                            log.error("Failed to send sale event to Kafka: {}", ex.getMessage());
                        }
                    });
            } catch (Exception e) {
                log.error("Error during Kafka send: {}", e.getMessage());
            }
        });
    }

}
