package com.campusconnect.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

// This client points to the internal API of the discussion-service
@FeignClient(name = "discussion-service")
public interface DiscussionClient {

    @DeleteMapping("/internal/api/discussions/event/{eventId}")
    void deleteChatHistory(@PathVariable("eventId") Long eventId);
}