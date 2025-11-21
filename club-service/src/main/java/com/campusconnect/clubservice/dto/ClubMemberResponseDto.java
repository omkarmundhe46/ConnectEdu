package com.campusconnect.clubservice.dto;

import com.campusconnect.clubservice.entity.ClubMember;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClubMemberResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long clubId;
    private ClubMember.Role role;
    private LocalDateTime joinedAt;
}