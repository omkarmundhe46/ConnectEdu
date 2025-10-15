package com.campusconnect.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipationDTO {
    private Long userId;
    private Long eventId;
    private String notificationType = "EVENT_PARTICIPATION";
}