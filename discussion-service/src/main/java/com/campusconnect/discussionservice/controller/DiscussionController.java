package com.campusconnect.discussionservice.controller;

import com.campusconnect.discussionservice.client.UserClient;
import com.campusconnect.discussionservice.dto.ChatMessageDto;
import com.campusconnect.discussionservice.dto.MessageRequestDto;
import com.campusconnect.discussionservice.dto.MessageResponseDto;
import com.campusconnect.discussionservice.dto.UserDto;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clubs/{clubId}/events/{eventId}/discussions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_CLUB_MEMBER', 'ROLE_CLUB_ADMIN')")
public class DiscussionController {

    private final DiscussionService discussionService;
    private final UserClient userClient;

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessages(@PathVariable Long clubId, @PathVariable Long eventId) {
        Long userId = getAuthenticatedUserId();

        // This service method now just validates membership and fetches messages
        List<MessageResponseDto> messages = discussionService.getEventMessages(clubId, eventId, userId);

        List<Long> userIds = messages.stream()
                // 2. Map from the correct DTO type.
                .map(MessageResponseDto::getUserId)
                .distinct()
                .toList();

        // Fetch user details in a single batch call
        Map<Long, UserDto> userMap = userClient.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        // Map to the ChatMessageDto
        List<ChatMessageDto> dtos = messages.stream().map(msg -> {
            // 1. Create a default UserDto
            UserDto defaultUser = new UserDto();
            defaultUser.setId(msg.getUserId());
            defaultUser.setName("Unknown");

            // 2. Get the real user, or use the default
            UserDto user = userMap.getOrDefault(msg.getUserId(), defaultUser);

            return ChatMessageDto.builder()
                    .id(msg.getId())
                    .content(msg.getContent())
                    .fileUrl(msg.getFileUrl())
                    .messageType(msg.getMessageType())
                    .userId(user.getId())
                    .userName(user.getName())
                    // 3. Get the 'sentAt' time from the msg DTO
                    .sentAt(msg.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageResponseDto> sendTextMessage(@PathVariable Long clubId, @PathVariable Long eventId, @RequestBody MessageRequestDto request) {
        request.setUserId(getAuthenticatedUserId());
        request.setMessageType(DiscussionMessage.MessageType.TEXT);
        MessageResponseDto message = discussionService.sendTextMessage(clubId, eventId, request);
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
