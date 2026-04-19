package com.delivery.foodDelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {

    private String id;
    private Long userId;
    private String restaurantId;
    private String restaurantName;
    private List<CartItemResponse> items;
    private Double totalPrice;
    private Integer totalItems;

}
