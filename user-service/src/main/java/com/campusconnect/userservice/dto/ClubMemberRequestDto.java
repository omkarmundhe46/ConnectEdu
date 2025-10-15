package com.campusconnect.userservice.dto;
import com.campusconnect.userservice.entity.Role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClubMemberRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Role is required")
    private Role role;
}