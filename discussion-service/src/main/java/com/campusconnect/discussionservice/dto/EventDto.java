package com.campusconnect.discussionservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime date;
    private String location;
    private Long clubId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}