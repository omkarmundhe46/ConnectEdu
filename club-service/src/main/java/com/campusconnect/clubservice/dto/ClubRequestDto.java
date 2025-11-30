package com.campusconnect.clubservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClubRequestDto {
    @NotBlank(message = "Club name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @Email(message = "If provided, admin email must be a valid format")
    private String adminEmail;

    private String category;

    private String logoUrl;
}
