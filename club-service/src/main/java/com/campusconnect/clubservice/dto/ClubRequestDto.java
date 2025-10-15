package com.campusconnect.clubservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClubRequestDto {
    @NotBlank(message = "Club name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;
}