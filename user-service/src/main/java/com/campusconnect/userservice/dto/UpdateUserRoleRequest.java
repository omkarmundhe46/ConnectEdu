package com.campusconnect.userservice.dto;

import com.campusconnect.userservice.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequest {
    @NotNull
    private Role newRole;
    private Long managedClubId; // Optional: Only used when promoting to CLUB_ADMIN
}