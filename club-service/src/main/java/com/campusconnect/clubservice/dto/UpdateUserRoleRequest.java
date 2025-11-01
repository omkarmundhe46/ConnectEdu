package com.campusconnect.clubservice.dto;

import com.campusconnect.clubservice.entity.Role;
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
    private Long managedClubId;
}