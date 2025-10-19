//package com.campusconnect.userservice.client;
//
//import com.campusconnect.userservice.dto.EventRequestDto;
//import com.campusconnect.userservice.dto.EventResponseDto;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.validation.Valid;
//import java.util.List;
//
//@FeignClient(
//    name = "event-service",
//    url = "${EVENT_SERVICE_URL:http://localhost:8083}/api/events"
//)
//public interface EventClient {
//
//    // Create event for a club
//    @PostMapping("/club/{clubId}")
//    EventResponseDto createEvent(@PathVariable("clubId") Long clubId,
//                                 @Valid @RequestBody EventRequestDto request);
//
//    // Get all events of a club
//    @GetMapping("/club/{clubId}")
//    List<EventResponseDto> getEventsByClub(@PathVariable("clubId") Long clubId);
//
//    // Get single event
//    @GetMapping("/{eventId}")
//    EventResponseDto getEventById(@PathVariable("eventId") Long eventId);
//
//    // Update event
//    @PutMapping("/{eventId}")
//    EventResponseDto updateEvent(@PathVariable("eventId") Long eventId,
//                                 @Valid @RequestBody EventRequestDto request);
//
//    // Delete event
//    @DeleteMapping("/{eventId}")
//    void deleteEvent(@PathVariable("eventId") Long eventId);
//}
