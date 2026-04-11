package com.delivery.foodDelivery.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryActionRequest {

    @NotNull(message = "Action is required")
    private String action;  // ACCEPT or REJECT

    private String rejectionReason;

}
