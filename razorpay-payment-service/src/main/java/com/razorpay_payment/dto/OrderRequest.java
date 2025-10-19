package com.razorpay_payment.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Integer amount; // Amount in paisa (e.g., 10000 for ₹100.00)
    private String currency;
}