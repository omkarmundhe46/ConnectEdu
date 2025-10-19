package com.campusconnect.eventservice.dto;

import lombok.Data;

// DTO to receive the order details from the payment-service.
@Data
public class OrderResponse {
    private String orderId;
    private Integer amount;
    private String currency;
}