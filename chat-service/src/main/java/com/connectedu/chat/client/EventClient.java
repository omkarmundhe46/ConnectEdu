package com.connectedu.chat.client;

import com.connectedu.chat.dto.ChatEventDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/internal/chat/events/upcoming")
    List<ChatEventDto> getUpcomingEvents();

    @GetMapping("/internal/chat/events/past")
    List<ChatEventDto> getPastEvents();

    @GetMapping("/internal/chat/events/registrations/{userId}")
    List<ChatEventDto> getMyRegistrations(@PathVariable("userId") Long userId);
}