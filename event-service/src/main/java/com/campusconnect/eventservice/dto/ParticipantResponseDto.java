package com.campusconnect.eventservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipantResponseDto {
    private Long id;
    private Long userId;
    private Long eventId;
    private LocalDateTime registeredAt;
}