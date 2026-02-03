package com.campusconnect.eventservice.controller;

import com.campusconnect.eventservice.dto.ChatEventDto;
import com.campusconnect.eventservice.dto.ChatClubDto; // Use the Chat DTO
import com.campusconnect.eventservice.dto.MyRegistrationResponseDto;
import com.campusconnect.eventservice.entity.Event;
import com.campusconnect.eventservice.repository.EventRepository;
import com.campusconnect.eventservice.client.ClubClient;
import com.campusconnect.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/chat/events")
@RequiredArgsConstructor
public class EventChatController {

    private final EventRepository eventRepository;
    private final ClubClient clubClient;
    private final EventService eventService;

    @GetMapping("/upcoming")
    public List<ChatEventDto> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByDateAfterOrderByDateAsc(now);
        return mapToDtoList(events);
    }

    @GetMapping("/past")
    public List<ChatEventDto> getPastEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByDateBeforeOrderByDateDesc(now);
        return mapToDtoList(events);
    }

    private List<ChatEventDto> mapToDtoList(List<Event> events) {
        if (events.isEmpty()) return List.of();

        // 1. OPTIMIZATION: Fetch ALL clubs in ONE network call
        Map<Long, String> clubMap;
        try {
            List<ChatClubDto> allClubs = clubClient.getAllClubs();
            // Convert List to Map: { 5 -> "Innovators Club", 6 -> "Coding Club" }
            clubMap = allClubs.stream()
                    .collect(Collectors.toMap(ChatClubDto::getId, ChatClubDto::getName));
        } catch (Exception e) {
            // Fallback if club-service is down
            clubMap = Map.of();
            System.err.println("Failed to fetch clubs: " + e.getMessage());
        }

        final Map<Long, String> finalClubMap = clubMap;

        // 2. Map events using the memory lookup (No loop network calls)
        return events.stream().map(event -> {
            String realClubName = finalClubMap.getOrDefault(event.getClubId(), "Club ID " + event.getClubId());

            return ChatEventDto.builder()
                    .name(event.getName())
                    .date(event.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .time(event.getDate().format(DateTimeFormatter.ofPattern("hh:mm a")))
                    .location(event.getLocation())
                    .clubName(realClubName) // Fast lookup
                    .contactName1(event.getContactName1())
                    .contactPhone1(event.getContactPhone1())
                    .contactName2(event.getContactName2())
                    .contactPhone2(event.getContactPhone2())
                    .build();
        }).collect(Collectors.toList());
    }

    @GetMapping("/registrations/{userId}")
    public List<ChatEventDto> getUserRegistrations(@PathVariable Long userId) {
        // 1. Reuse your existing service logic
        List<MyRegistrationResponseDto> registrations = eventService.getRegistrationsForUser(userId);

        // 2. Map to Chat DTO
        return registrations.stream().map(reg -> ChatEventDto.builder()
                        .name(reg.getEventName())
                        .date(reg.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .time(reg.getEventDate().format(DateTimeFormatter.ofPattern("hh:mm a")))
                        .clubName("Registered Event") // Or fetch club if available in MyRegistrationResponseDto
                        .build())
                .collect(Collectors.toList());
    }


}