package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.response.PaymentResponse;
import com.delivery.foodDelivery.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Dummy payment service that simulates a payment gateway.
 * Replace processPayment() internals with Razorpay/Stripe SDK calls for production.
 *
 * Razorpay integration points are documented inline.
 */
@Service
@Slf4j
public class PaymentService {

    /**
     * Process a payment for an order.
     *
     * @param orderId       the order being paid for
     * @param amount        total amount in INR
     * @param method        CARD | UPI | COD | NET_BANKING
     * @param paymentToken  token from frontend payment widget (Razorpay checkout, etc.)
     * @return PaymentResponse with generated paymentId and status
     */
    public PaymentResponse processPayment(Long orderId, Double amount,
                                          String method, String paymentToken) {
        log.info("Processing payment for orderId={} amount={} method={}", orderId, amount, method);

        // ── COD: always succeeds immediately ──────────────────────────────
        if ("COD".equalsIgnoreCase(method)) {
            String paymentId = "COD-" + orderId + "-" + UUID.randomUUID().toString().substring(0, 8);
            log.info("COD payment created: {}", paymentId);
            return PaymentResponse.builder()
                    .paymentId(paymentId)
                    .status(PaymentStatus.PENDING.name())   // confirmed on delivery
                    .amount(amount)
                    .method(method)
                    .message("Cash on delivery. Pay ₹" + amount + " to the delivery partner.")
                    .build();
        }

        // ── Online payment (dummy simulation) ────────────────────────────
        // TODO: Replace with real Razorpay SDK call:
        //
        //   RazorpayClient client = new RazorpayClient(KEY_ID, KEY_SECRET);
        //   JSONObject options = new JSONObject();
        //   options.put("amount", (int)(amount * 100));   // paise
        //   options.put("currency", "INR");
        //   options.put("receipt", "order_" + orderId);
        //   com.razorpay.Order razorpayOrder = client.orders.create(options);
        //   // Then verify signature after frontend callback
        //
        boolean simulateSuccess = simulateGateway(paymentToken);

        if (simulateSuccess) {
            String paymentId = "PAY-" + UUID.randomUUID().toString().toUpperCase().replace("-", "");
            log.info("Payment succeeded: {} for orderId={}", paymentId, orderId);
            return PaymentResponse.builder()
                    .paymentId(paymentId)
                    .status(PaymentStatus.SUCCESS.name())
                    .amount(amount)
                    .method(method)
                    .message("Payment successful")
                    .build();
        }

        // Simulate payment failure
        log.warn("Payment failed for orderId={}", orderId);
        return PaymentResponse.builder()
                .status(PaymentStatus.FAILED.name())
                .amount(amount)
                .method(method)
                .message("Payment failed. Please retry or choose a different method.")
                .build();
    }

    /**
     * Issue a refund for a cancelled order.
     */
    public PaymentResponse processRefund(String paymentId, Double amount) {
        log.info("Processing refund for paymentId={} amount={}", paymentId, amount);

        // TODO: Razorpay refund:
        //   client.payments.refund(paymentId, refundRequest);

        return PaymentResponse.builder()
                .paymentId("REF-" + paymentId)
                .status(PaymentStatus.REFUNDED.name())
                .amount(amount)
                .message("Refund of ₹" + amount + " initiated. Credited in 5-7 business days.")
                .build();
    }

    /**
     * Simulates a payment gateway response.
     * In real integration this would verify the Razorpay signature.
     */
    private boolean simulateGateway(String token) {
        // Treat null/empty token as failure; any non-empty token = success (demo)
        return token != null && !token.isBlank();
    }
}
