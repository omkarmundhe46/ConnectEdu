package com.campusconnect.eventservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AnalyticsResponseDto {
    // For Bar Charts
    private List<ChartDataDto> eventsByMonth;

    // For Pie Charts (or Row of Stats)
    private List<ChartDataDto> participationByClub;

    // For Stat Cards
    private Long totalEvents;
    private Long totalParticipants;
    private Double totalRevenue;
}