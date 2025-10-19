package com.campusconnect.eventservice.dto;

import lombok.Data;
@Data
public class RegistrationRequestDto {
    private Long userId;
    private String college;
    private String mobileNumber;
    private String address;
    private Integer amount; // The event fee in paisa
}