package com.campusconnect.userservice.dto;

import lombok.Data;

@Data
public class UpdateProfileRequestDto {
    private String phone;
    private String profileImageUrl;
}