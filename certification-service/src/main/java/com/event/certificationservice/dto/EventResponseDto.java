package com.event.certificationservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EventResponseDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate date;
    private String location;
    private Long clubId;

}
