package com.campusconnect.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubResponseDto {
    private Long id;          // Club Id
    private String name;      // Club Name
    private String description;
}
