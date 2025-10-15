package com.campusconnect.discussionservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClubMemberDto {
    private Long id;
    private Long userId;
    private Long clubId;
    private String role;
    private LocalDateTime joinedAt;
}