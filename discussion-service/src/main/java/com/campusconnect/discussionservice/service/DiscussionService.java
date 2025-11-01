package com.campusconnect.discussionservice.service;

import com.campusconnect.discussionservice.dto.MessageRequestDto;
import com.campusconnect.discussionservice.dto.MessageResponseDto;
import com.campusconnect.discussionservice.dto.UserDto;
import com.campusconnect.discussionservice.entity.DiscussionMessage;
import com.campusconnect.discussionservice.exception.FileUploadException;
import com.campusconnect.discussionservice.exception.MessageNotFoundException;
import com.campusconnect.discussionservice.exception.NotMemberException;
import com.campusconnect.discussionservice.exception.EventNotFoundException;
import com.campusconnect.discussionservice.exception.UserNotFoundException;
import com.campusconnect.discussionservice.repository.DiscussionMessageRepository;
import com.campusconnect.discussionservice.client.EventClient;
import com.campusconnect.discussionservice.client.UserClient;
import com.campusconnect.discussionservice.client.ClubClient;
import com.campusconnect.discussionservice.dto.EventDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscussionService {
    
    private final DiscussionMessageRepository messageRepository;
    private final EventClient eventClient;
    private final UserClient userClient;
    private final ClubClient clubClient;
    private final S3StorageService s3StorageService; // Inject the new service

    public MessageResponseDto sendTextMessage(Long clubId, Long eventId, MessageRequestDto messageRequestDto) {
        validateMembership(clubId, messageRequestDto.getUserId());
        if (messageRequestDto.getMessageType() != DiscussionMessage.MessageType.TEXT ||
                (messageRequestDto.getContent() == null || messageRequestDto.getContent().trim().isEmpty())) {
            throw new IllegalArgumentException("Content cannot be empty for text messages");
        }

        DiscussionMessage message = new DiscussionMessage();
        message.setEventId(eventId);
        message.setUserId(messageRequestDto.getUserId());
        message.setContent(messageRequestDto.getContent());
        message.setMessageType(DiscussionMessage.MessageType.TEXT);

        DiscussionMessage savedMessage = messageRepository.save(message);
        return mapToResponseDto(savedMessage);
    }

    public MessageResponseDto sendFileMessage(Long clubId, Long eventId, Long userId, MultipartFile file, DiscussionMessage.MessageType messageType) {
        validateMembership(clubId, userId);

        if (file.isEmpty()) {
            throw new FileUploadException("File cannot be empty");
        }

        String fileUrl = s3StorageService.uploadFile(file);

        DiscussionMessage message = new DiscussionMessage();
        message.setEventId(eventId);
        message.setUserId(userId);
        message.setFileUrl(fileUrl); // Save the S3 URL
        message.setMessageType(messageType);

        DiscussionMessage savedMessage = messageRepository.save(message);
        return mapToResponseDto(savedMessage);
    }


    public List<MessageResponseDto> getEventMessages(Long clubId, Long eventId, Long userId) {
        validateMembership(clubId, userId);

        return messageRepository.findByEventIdOrderBySentAtAsc(eventId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public void deleteMessage(Long clubId, Long eventId, Long messageId, Long userId) {
        DiscussionMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found with id: " + messageId));


        if (!message.getUserId().equals(userId) && !isClubAdmin(clubId, userId)) {
            throw new NotMemberException("You are not authorized to delete this message");
        }

        messageRepository.delete(message);
    }


    private void validateMembership(Long clubId, Long userId) {

        try {
            Boolean isMember = clubClient.checkMembership(clubId, userId);
            if (isMember == null || !isMember) {
                throw new NotMemberException("User is not a member of the club that owns this event");
            }
        } catch (FeignException e) {
            throw new NotMemberException("Could not verify club membership.");
        }
    }

    private boolean isClubAdmin(Long clubId, Long userId) {
        try {
            // This check is now more direct as well.
            String role = clubClient.getMemberRole(clubId, userId);
            return "PRESIDENT".equals(role) || "VICE_PRESIDENT".equals(role);
        } catch (FeignException e) {
            return false;
        }
    }

    private MessageResponseDto mapToResponseDto(DiscussionMessage message) {
        MessageResponseDto dto = new MessageResponseDto();
        dto.setId(message.getId());
        dto.setUserId(message.getUserId());
        try {
            UserDto user = userClient.getUserById(message.getUserId());
            dto.setUserName(user.getName());
        } catch (FeignException e) {
            dto.setUserName("User" + message.getUserId());
        }
        dto.setContent(message.getContent());
        dto.setFileUrl(message.getFileUrl());
        dto.setMessageType(message.getMessageType());
        dto.setSentAt(message.getSentAt());
        return dto;
    }
}