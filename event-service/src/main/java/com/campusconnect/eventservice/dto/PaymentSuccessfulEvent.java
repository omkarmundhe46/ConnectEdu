package com.campusconnect.eventservice.dto;

import lombok.Data;

// This DTO must exactly match the Kafka message sent by the payment-service.
@Data
public class PaymentSuccessfulEvent {
    private String paymentId;
    private RegistrationData registrationData;

    @Data
    public static class RegistrationData {
        private Long userId;
        private Long eventId;
        private String college;
        private String mobileNumber;
        private String address;
    }
}