package com.razorpay_payment.controller;

import com.razorpay.RazorpayException;
import com.razorpay_payment.dto.OrderRequest;
import com.razorpay_payment.dto.OrderResponse;
import com.razorpay_payment.dto.PaymentVerificationRequest;
import com.razorpay_payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) throws RazorpayException {
        return ResponseEntity.ok(paymentService.createOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        boolean success = paymentService.verifyPayment(request);
        if (success) {
            return ResponseEntity.ok("Payment verified and registration is being processed.");
        } else {
            return ResponseEntity.status(400).body("Payment verification failed.");
        }
    }
}