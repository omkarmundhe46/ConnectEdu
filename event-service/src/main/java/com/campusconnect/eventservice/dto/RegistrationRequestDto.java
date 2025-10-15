package com.campusconnect.eventservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrationRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;
}