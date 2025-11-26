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
    private String imageUrl;
    private Double fee;
    private String status;
    private String contactName1;
    private String contactPhone1;
    private String contactName2;
    private String contactPhone2;

}