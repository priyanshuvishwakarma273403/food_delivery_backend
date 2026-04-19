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
        log.info("Producing sale event: {}", saleEvent.getTitle());
        
        CompletableFuture<SendResult<String, SaleEventDTO>> future = 
                kafkaTemplate.send(KafkaConfig.SALE_TOPIC, saleEvent.getSaleId(), saleEvent);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[{}] with offset=[{}]", saleEvent, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send message=[{}] due to : {}", saleEvent, ex.getMessage());
            }
        });
    }
}
