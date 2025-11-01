package com.campusconnect.clubservice.dto;

import com.campusconnect.clubservice.entity.ClubMember;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClubMemberRequestDto {
    @NotBlank(message = "User email is required")
    @Email(message = "User email must be a valid email address")
    private String userEmail;

    @NotNull(message = "Role is required")
    private ClubMember.Role role;
}