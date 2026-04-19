package com.delivery.foodDelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponse {
    private String id;
    private String menuItemId;
    private String menuItemName;
    private String menuItemImage;
    private Double unitPrice;
    private Integer quantity;
    private Double subtotal;

}
