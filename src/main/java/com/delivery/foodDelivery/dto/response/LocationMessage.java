package com.delivery.foodDelivery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationMessage {

    private Long orderId;
    private Long deliveryPartnerId;
    private Double latitude;
    private Double longitude;
    private String status;           // current DeliveryStatus
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

}
