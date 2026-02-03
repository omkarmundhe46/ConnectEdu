package com.campusconnect.eventservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatClubDto {
    private Long id; // <--- ADD THIS FIELD
    private String name;
    private String description;
    private String adminEmail;
}