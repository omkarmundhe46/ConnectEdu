package com.campusconnect.discussionservice.controller;

import com.campusconnect.discussionservice.service.DiscussionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api/discussions")
@RequiredArgsConstructor
@Slf4j
public class InternalDiscussionController {

    private final DiscussionService discussionService;

    /**
     * Internal endpoint (called by event-service) to delete all messages
     * for a specific event.
     */
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<Void> deleteChatHistory(@PathVariable Long eventId) {
        log.info("Received internal request to delete chat history for eventId: {}", eventId);
        discussionService.deleteMessagesByEventId(eventId);
        return ResponseEntity.ok().build();
    }
}