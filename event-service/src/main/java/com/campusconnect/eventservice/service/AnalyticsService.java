package com.campusconnect.eventservice.service;

import com.campusconnect.eventservice.client.ClubClient;
import com.campusconnect.eventservice.dto.AnalyticsResponseDto;
import com.campusconnect.eventservice.dto.ChartDataDto;
import com.campusconnect.eventservice.dto.ClubDto;
import com.campusconnect.eventservice.repository.EventRepository;
import com.campusconnect.eventservice.repository.EventParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final ClubClient clubClient;

    // --- COLLEGE ADMIN (GLOBAL) ANALYTICS ---
    public AnalyticsResponseDto getGlobalAnalytics() {
        // 1. Events by Month
        List<ChartDataDto> eventsByMonth = eventRepository.findEventsCountByMonth();

        // 2. Participation by Club (Need to fetch Club Names)
        List<ChartDataDto> rawClubStats = eventRepository.findTopClubsByParticipation();
        List<ChartDataDto> participationByClub = rawClubStats.stream().map(stat -> {
            try {
                Long clubId = Long.parseLong(stat.getLabel());
                ClubDto club = clubClient.getClubById(clubId);
                return new ChartDataDto(club.getName(), stat.getValue());
            } catch (Exception e) {
                return new ChartDataDto("Unknown Club " + stat.getLabel(), stat.getValue());
            }
        }).collect(Collectors.toList());

        // 3. Totals
        Long totalEvents = eventRepository.count();
        Long totalParticipants = participantRepository.count();
        Double totalRevenue = eventRepository.getTotalRevenue();

        return AnalyticsResponseDto.builder()
                .eventsByMonth(eventsByMonth)
                .participationByClub(participationByClub)
                .totalEvents(totalEvents)
                .totalParticipants(totalParticipants)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .build();
    }

    // --- CLUB ADMIN (LOCAL) ANALYTICS ---
    public AnalyticsResponseDto getClubAnalytics(Long clubId) {
        // 1. Last 5 Events Participation
        List<ChartDataDto> eventStats = eventRepository.findParticipationByEventForClub(clubId);

        // 2. Revenue for this club
        Double revenue = eventRepository.getRevenueForClub(clubId);

        // 3. Total Events for this club
        // We can reuse existing repo methods or count stream
        // Let's keep it simple and just return what we have for now

        return AnalyticsResponseDto.builder()
                .eventsByMonth(eventStats) // Reuse this field for "Events Participation"
                .totalRevenue(revenue != null ? revenue : 0.0)
                .build();
    }
}