package com.campusconnect.notificationservice.feign;

import com.campusconnect.notificationservice.dto.EventResponseDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//
//@FeignClient(name = "event-service", url = "${EVENT_SERVICE_URL:http://localhost:8083}")
//public interface EventClient {
//    // EventController in event-service uses class-level @RequestMapping("/api/clubs")
//    // so full path is /api/clubs/events/{eventId}/details
//    @GetMapping("/api/clubs/events/{eventId}/details")
//    EventResponseDto getEventById(@PathVariable("eventId") Long eventId);
//}
@FeignClient(name = "event-service", url = "${EVENT_SERVICE_URL:http://localhost:8083}/api/clubs")
public interface EventClient {

    @GetMapping("/events/{eventId}/details")
    EventResponseDto getEventById(@PathVariable("eventId") Long eventId);
}
