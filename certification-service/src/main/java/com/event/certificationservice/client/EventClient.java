package com.event.certificationservice.client;

import com.event.certificationservice.dto.EventResponseDto;
import com.event.certificationservice.dto.ParticipantResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "event-service", url = "${EVENT_SERVICE_URL}")
public interface EventClient {
    @GetMapping("/api/clubs/events/{eventId}/details")
    EventResponseDto getEventById(@PathVariable Long eventId);

    @GetMapping("/api/clubs/{clubId}/events/{eventId}/participants")
    List<ParticipantResponseDto> getEventParticipants(@PathVariable Long clubId, @PathVariable Long eventId);

    @GetMapping("/api/clubs/events/date/{date}")
    List<EventResponseDto> getEventsByDate(@PathVariable String date);
}