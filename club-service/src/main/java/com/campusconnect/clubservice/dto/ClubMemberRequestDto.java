package com.campusconnect.clubservice.dto;

import com.campusconnect.clubservice.entity.ClubMember;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClubMemberRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Role is required")
    private ClubMember.Role role;
}