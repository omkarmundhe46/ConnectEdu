package com.event.certificationservice.client;

import com.event.certificationservice.dto.EventResponseDto;
import com.event.certificationservice.dto.ParticipantResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "event-service", url = "${EVENT_SERVICE_URL:http://localhost:8083}")
public interface EventClient {

    @GetMapping("/internal/api/events/{eventId}/details")
    EventResponseDto getEventById(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/api/events/by-date/{date}")
    List<EventResponseDto> getEventsByDate(@PathVariable("date") String date);

    @GetMapping("/internal/api/events/{eventId}/participants")
    List<ParticipantResponseDto> getEventParticipants(@PathVariable("eventId") Long eventId);

}