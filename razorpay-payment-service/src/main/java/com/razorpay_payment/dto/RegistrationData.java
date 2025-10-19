package com.razorpay_payment.dto;

import lombok.Data;
// This DTO holds the user's form submission data
@Data
public class RegistrationData {
    private Long userId;
    private Long eventId;
    private String college;
    private String mobileNumber;
    private String address;
}
