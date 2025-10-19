package com.campusconnect.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is the new message sent to the notification-service
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRegisteredEvent {
    private Long userId;
    private Long eventId;
}