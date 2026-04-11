package com.delivery.foodDelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryResponse {
    private Long id;
    private Long orderId;
    private Long deliveryPartnerId;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
    private String status;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

}
