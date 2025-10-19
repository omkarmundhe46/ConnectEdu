package com.campusconnect.notificationservice.feign;

import com.campusconnect.notificationservice.dto.EventResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Change the URL to point to the new internal path
@FeignClient(name = "event-service", url = "${EVENT_SERVICE_URL:http://localhost:8083}/internal/api/events")
public interface EventClient {

    // The path is now relative to the new base URL
    @GetMapping("/{eventId}/details")
    EventResponseDto getEventById(@PathVariable("eventId") Long eventId);
}