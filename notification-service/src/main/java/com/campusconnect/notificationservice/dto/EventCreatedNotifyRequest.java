package com.campusconnect.notificationservice.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EventCreatedNotifyRequest {
    private String requestId;
    private Long eventId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private String location;
    private Long clubId;
}
