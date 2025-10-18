package com.campusconnect.userservice.dto;

import com.campusconnect.userservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private String department;
    private Role role; // ADDED
    private Long managedClubId; // ADDED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}