package com.razorpay_payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.razorpay_payment.dto.OrderRequest;
import com.razorpay_payment.dto.OrderResponse;
import com.razorpay_payment.dto.PaymentSuccessfulEvent;
import com.razorpay_payment.dto.PaymentVerificationRequest;
import com.razorpay_payment.kafka.PaymentKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.key-id}")
    private String keyId;
    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final PaymentKafkaProducer kafkaProducer;

    public OrderResponse createOrder(OrderRequest request) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", request.getAmount());
        orderRequest.put("currency", request.getCurrency());
        orderRequest.put("receipt", "receipt_order_" + System.currentTimeMillis());

        Order order = razorpayClient.orders.create(orderRequest);

        return OrderResponse.builder()
                .orderId(order.get("id"))
                .amount(order.get("amount"))
                .currency(order.get("currency"))
                .build();
    }

    // real verification
//    public boolean verifyPayment(PaymentVerificationRequest request) {
//        try {
//            JSONObject options = new JSONObject();
//            options.put("razorpay_order_id", request.getRazorpayOrderId());
//            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
//            options.put("razorpay_signature", request.getRazorpaySignature());
//
//            // This is the digital handshake to confirm the payment is real
//            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);
//
//            if (isValid) {
//                log.info("Payment verification successful for order: {}", request.getRazorpayOrderId());
//                // If valid, send a confirmation message to Kafka
//                PaymentSuccessfulEvent event = PaymentSuccessfulEvent.builder()
//                        .paymentId(request.getRazorpayPaymentId())
//                        .registrationData(request.getRegistrationData())
//                        .build();
//                kafkaProducer.sendPaymentSuccessEvent(event);
//            } else {
//                log.warn("Payment verification failed for order: {}", request.getRazorpayOrderId());
//            }
//            return isValid;
//        } catch (RazorpayException e) {
//            log.error("Error verifying payment signature: {}", e.getMessage());
//            return false;
//        }
//    }


    // for testing
    // ... inside your verifyPayment method ...
    public boolean verifyPayment(PaymentVerificationRequest request) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            // --- TEMPORARY CHANGE FOR TESTING ---
            // Comment out the real verification
            // boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            // Force the verification to be true for your test
            boolean isValid = true;

            if (isValid) {
                log.info("Payment verification successful for order: {}", request.getRazorpayOrderId());
                // This will now send the Kafka message
                PaymentSuccessfulEvent event = PaymentSuccessfulEvent.builder()
                        .paymentId(request.getRazorpayPaymentId())
                        .registrationData(request.getRegistrationData())
                        .build();
                kafkaProducer.sendPaymentSuccessEvent(event);
            } else {
                // This 'else' block will be skipped during your test
                log.warn("Payment verification failed for order: {}", request.getRazorpayOrderId());
            }
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying payment signature: {}", e.getMessage());
            return false;
        }
    }
}