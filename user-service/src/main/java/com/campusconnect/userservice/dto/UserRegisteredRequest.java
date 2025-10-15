package com.campusconnect.userservice.dto;

import lombok.Data;

@Data
public class UserRegisteredRequest {
    private Long userId;
    private String requestId;
}