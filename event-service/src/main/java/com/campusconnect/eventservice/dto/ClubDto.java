package com.campusconnect.eventservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClubDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}