package com.campusconnect.clubservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubResponseDto {
    private Long id;
    private String name;
    private String description;
    private Long adminId;
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
   
}