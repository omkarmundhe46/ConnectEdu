package com.campusconnect.discussionservice.client;

import com.campusconnect.discussionservice.config.WebSocketFeignInterceptor;
import com.campusconnect.discussionservice.dto.EventDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "event-service", configuration = WebSocketFeignInterceptor.class)
public interface EventClient {
    @GetMapping("/internal/api/events/{eventId}/details") // Point to internal endpoint
    EventDto getEventDetails(@PathVariable("eventId") Long eventId);

    // ADD THIS
    @GetMapping("/internal/api/events/ended-before")
    List<EventDto> getEventsEndedBefore(@RequestParam("date") String date);
}