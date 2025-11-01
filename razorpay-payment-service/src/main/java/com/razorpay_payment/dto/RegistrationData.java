package com.razorpay_payment.dto;

import lombok.Data;
@Data
public class RegistrationData {
    private Long userId;
    private Long eventId;
    private String college;
    private String mobileNumber;
    private String address;
}
