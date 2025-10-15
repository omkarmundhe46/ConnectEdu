package com.campusconnect.eventservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventCreatedRequest {
    private Long eventId;
    private Long clubId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String requestId;
}