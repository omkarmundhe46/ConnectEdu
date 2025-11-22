package com.campusconnect.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordResetEmailRequest {
    private String email;
    private String name;
    private String otp;
}