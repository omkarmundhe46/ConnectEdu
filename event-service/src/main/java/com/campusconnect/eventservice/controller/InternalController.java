package com.campusconnect.eventservice.controller;

import com.campusconnect.eventservice.dto.EventResponseDto;
import com.campusconnect.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/internal/api/events") // A dedicated path for internal calls
@RequiredArgsConstructor
public class InternalController {

    private final EventService eventService;

    @GetMapping("/{eventId}/details")
    public ResponseEntity<EventResponseDto> getEventDetails(@PathVariable Long eventId) {
        EventResponseDto event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    // ... inside InternalController in event-service
    @GetMapping("/ended-before")
    public List<EventResponseDto> getEventsEndedBefore(@RequestParam("date") String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return eventService.findEventsEndedBefore(parsedDate);
    }
}