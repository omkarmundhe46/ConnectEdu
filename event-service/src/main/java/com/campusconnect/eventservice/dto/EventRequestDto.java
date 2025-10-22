package com.campusconnect.eventservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequestDto {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    private LocalDateTime date;

    @NotBlank(message = "Location is required")
    private String location;

    private String imageUrl; // Add imageUrl field (can be optional)

    private String meetingLink;
}
