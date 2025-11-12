package com.campusconnect.discussionservice.controller;

import com.campusconnect.discussionservice.client.ClubClient;
import com.campusconnect.discussionservice.client.EventClient;
import com.campusconnect.discussionservice.client.UserClient;
import com.campusconnect.discussionservice.dto.ChatMessageDto;
import com.campusconnect.discussionservice.dto.EventDto;
import com.campusconnect.discussionservice.dto.UserDto;
import com.campusconnect.discussionservice.entity.DiscussionMessage;
import com.campusconnect.discussionservice.repository.DiscussionMessageRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessageSendingOperations messageTemplate;
    private final DiscussionMessageRepository messageRepository;
    private final ClubClient clubClient;
    private final EventClient eventClient;


    @MessageMapping("/chat.sendMessage/{eventId}")
    public void sendMessage(
            @DestinationVariable Long eventId,
            @Payload ChatMessageDto chatMessage,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // 1. Get the user's JWT from the WebSocket session (we'll set this up in Part 2)
        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) Objects.requireNonNull(headerAccessor.getUser());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        Jwt jwt = (Jwt) authToken.getPrincipal();
        Object userIdObj = jwt.getClaim("userId");
        if (!(userIdObj instanceof Number)) {
            throw new AccessDeniedException("Invalid User ID in token.");
        }
        Long userId = ((Number) userIdObj).longValue();
        String userName = jwt.getClaim("name");

        try {
            EventDto event = eventClient.getEventDetails(eventId);
            if (event == null) {
                throw new AccessDeniedException("Event not found.");
            }
            Boolean isMember = clubClient.checkMembership(event.getClubId(), userId);
            if (isMember == null || !isMember) {
                throw new AccessDeniedException("User is not a member of this club.");
            }
        } catch (Exception e) {
            log.warn("Access denied for user {} sending to event {}: {}", userId, eventId, e.getMessage());
            // We can't throw here, as it might not be caught.
            // Instead, we just stop processing the message.
            return;
        }

        // 3. Create and save the message to the database
        DiscussionMessage message = DiscussionMessage.builder()
                .eventId(eventId)
                .userId(userId)
                .content(chatMessage.getContent())
                .fileUrl(chatMessage.getFileUrl())
                .messageType(chatMessage.getMessageType())
                .sentAt(LocalDateTime.now()) // Set server time
                .build();

        DiscussionMessage savedMessage = messageRepository.save(message);
        log.info("Saved message ID {} to event {}", savedMessage.getId(), eventId);

        // 4. Create the final response DTO
        ChatMessageDto responseMessage = ChatMessageDto.builder()
                .id(savedMessage.getId())
                .content(savedMessage.getContent())
                .fileUrl(savedMessage.getFileUrl())
                .messageType(savedMessage.getMessageType())
                .userId(userId)
                .userName(userName)
                .sentAt(savedMessage.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();

        // 5. Broadcast the message to everyone subscribed to this event's topic
        messageTemplate.convertAndSend("/topic/event/" + eventId, responseMessage);
    }
}