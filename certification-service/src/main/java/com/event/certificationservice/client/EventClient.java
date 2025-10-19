package com.event.certificationservice.client;

import com.event.certificationservice.dto.EventResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// The base URL for the event-service is correct.
@FeignClient(name = "event-service", url = "${EVENT_SERVICE_URL:http://localhost:8083}")
public interface EventClient {

    /**
     * This is the only method this service needs. It calls the unsecured, internal
     * endpoint on the event-service to get event details for the 7 PM rule check.
     */
    @GetMapping("/internal/api/events/{eventId}/details")
    EventResponseDto getEventById(@PathVariable("eventId") Long eventId);

}