package com.campusconnect.eventservice.dto;

import lombok.Data;

// DTO to request order creation from the payment-service.
@Data
public class OrderRequest {
    private Integer amount;
    private String currency;
}