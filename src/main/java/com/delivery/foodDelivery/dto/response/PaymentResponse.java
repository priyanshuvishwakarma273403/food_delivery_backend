package com.delivery.foodDelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private String paymentId;
    private String status;       // SUCCESS / FAILED
    private Double amount;
    private String method;
    private String message;
    // Razorpay-specific fields (for future extension)
    private String razorpayOrderId;
    private String razorpayPaymentId;


}
