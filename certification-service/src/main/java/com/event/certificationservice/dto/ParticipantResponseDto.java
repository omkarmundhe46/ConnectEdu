package com.event.certificationservice.dto;

import lombok.Data;

@Data
public class ParticipantResponseDto {
    private Long userId;
    private String role; // e.g. "participant"

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
