package com.campusconnect.notificationservice.feign;

import com.campusconnect.notificationservice.dto.EventResponseDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", url = "${EVENT_SERVICE_URL:http://localhost:8083}/api/clubs")
public interface EventClient {

    @GetMapping("/events/{eventId}/details")
    EventResponseDto getEventById(@PathVariable("eventId") Long eventId);
}
