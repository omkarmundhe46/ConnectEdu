package com.campusconnect.eventservice.client;

import com.campusconnect.eventservice.dto.OrderRequest;
import com.campusconnect.eventservice.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {
    @PostMapping("/api/payments/create-order")
    OrderResponse createOrder(@RequestBody OrderRequest request);
}