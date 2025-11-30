package com.campusconnect.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataDto {
    private String label; // e.g., "Coding Club", "Jan", "Q1"
    private Double value; // e.g., 150, 5000.0
}