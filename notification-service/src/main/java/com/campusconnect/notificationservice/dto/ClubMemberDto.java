package com.campusconnect.notificationservice.dto;

import lombok.Data;

@Data
public class ClubMemberDto {
    private Long id;
    private Long userId;
    private Long clubId;
    private String role;
}