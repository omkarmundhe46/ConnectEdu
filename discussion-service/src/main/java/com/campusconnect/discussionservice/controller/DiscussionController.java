package com.campusconnect.discussionservice.controller;

import com.campusconnect.discussionservice.dto.MessageRequestDto;
import com.campusconnect.discussionservice.dto.MessageResponseDto;
import com.campusconnect.discussionservice.entity.DiscussionMessage;
import com.campusconnect.discussionservice.service.DiscussionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// --- API IMPROVEMENT: A more RESTful and consistent URL path ---
@RestController
@RequestMapping("/api/clubs/{clubId}/events/{eventId}/discussions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_CLUB_MEMBER', 'ROLE_CLUB_ADMIN')")
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping("/messages")
    public ResponseEntity<List<MessageResponseDto>> getMessages(@PathVariable Long clubId, @PathVariable Long eventId) {
        Long userId = getAuthenticatedUserId();
        List<MessageResponseDto> messages = discussionService.getEventMessages(clubId, eventId, userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageResponseDto> sendTextMessage(@PathVariable Long clubId, @PathVariable Long eventId, @RequestBody MessageRequestDto request) {
        request.setUserId(getAuthenticatedUserId());
        request.setMessageType(DiscussionMessage.MessageType.TEXT);
        MessageResponseDto message = discussionService.sendTextMessage(clubId, eventId, request);
        return ResponseEntity.ok(message);
    }

    @PostMapping(value = "/messages/file", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponseDto> sendFileMessage(@PathVariable Long clubId, @PathVariable Long eventId,
                                                              @RequestParam("file") MultipartFile file,
                                                              @RequestParam("messageType") DiscussionMessage.MessageType messageType) {
        Long userId = getAuthenticatedUserId();
        MessageResponseDto message = discussionService.sendFileMessage(clubId, eventId, userId, file, messageType);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long clubId, @PathVariable Long eventId, @PathVariable Long messageId) {
        Long userId = getAuthenticatedUserId();
        discussionService.deleteMessage(clubId, eventId, messageId, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();

        Object userIdObj = jwt.getClaim("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        throw new IllegalStateException("User ID claim is missing or not a number.");
    }
}
