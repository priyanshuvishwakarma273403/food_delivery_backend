package com.delivery.foodDelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleRequest {
    @NotBlank(message = "Sale title is required")
    private String title;

    @NotBlank(message = "Sale message is required")
    private String message;

    private Double discountPercentage;
    
    private String promoCode;
}
