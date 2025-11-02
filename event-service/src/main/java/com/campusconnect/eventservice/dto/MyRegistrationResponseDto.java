package com.campusconnect.eventservice.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MyRegistrationResponseDto {
    // From EventParticipant
    private LocalDateTime registeredAt;
    private String paymentId;

    // From Event
    private Long eventId;
    private Long clubId;
    private String eventName;
    private String eventImageUrl;
    private LocalDateTime eventDate;
    private String eventStatus;
}