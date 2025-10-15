package com.campusconnect.notificationservice.dto;

import lombok.Data;

@Data
public class ClubMemberAddedRequest {
    private Long userId;
    private Long clubId;
    private String role; 
}
