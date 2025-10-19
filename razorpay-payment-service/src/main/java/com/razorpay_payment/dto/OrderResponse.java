package com.razorpay_payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private Integer amount;
    private String currency;
}