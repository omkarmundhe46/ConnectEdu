package com.campusconnect.userservice.dto;

import lombok.Data;

@Data
public class UserRegisteredRequest {
    private Long userId;
    private String name;  // ADD THIS
    private String email; // ADD THIS
    private String requestId;
}