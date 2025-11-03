package com.campusconnect.notificationservice.dto;

import lombok.Data;

@Data
public class EmailVerificationRequest {
    private String email;
    private String name;
    private String otp;
}