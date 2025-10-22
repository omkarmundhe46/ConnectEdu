package com.campusconnect.eventservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventResponseDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime date;
    private String location;
    private Long clubId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String meetingLink;
    private String imageUrl; // Add imageUrl field (can be optional)
}