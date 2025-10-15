package com.campusconnect.notificationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRegisteredRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String requestId;
}