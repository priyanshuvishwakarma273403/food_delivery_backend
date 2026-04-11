package com.delivery.foodDelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {

    private Long id;
    private Long userId;
    private Long restaurantId;
    private String restaurantName;
    private List<CartItemResponse> items;
    private Double totalPrice;
    private Integer totalItems;

}
