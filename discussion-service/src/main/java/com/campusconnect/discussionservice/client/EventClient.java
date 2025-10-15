package com.campusconnect.discussionservice.client;

import com.campusconnect.discussionservice.dto.EventDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", url = "${EVENT_SERVICE_URL:http://localhost:8083}")
public interface EventClient {
    @GetMapping("/api/clubs/events/{eventId}/details")
    EventDto getEventDetails(@PathVariable("eventId") Long eventId);
}