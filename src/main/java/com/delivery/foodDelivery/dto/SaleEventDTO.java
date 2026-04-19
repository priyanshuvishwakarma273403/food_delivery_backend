package com.delivery.foodDelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleEventDTO implements Serializable {
    private String saleId;
    private String title;
    private String description;
    private Double discountPercentage;
    private String promoCode;
    private String message;
}
