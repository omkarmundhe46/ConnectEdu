package com.razorpay_payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is the message that will be sent to Kafka
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessfulEvent {
    private String paymentId;
    private RegistrationData registrationData;
}