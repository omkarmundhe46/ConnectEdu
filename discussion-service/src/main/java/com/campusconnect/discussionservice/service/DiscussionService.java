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
    
    @Value("${file.upload.dir}")
    private String uploadDir;

    public MessageResponseDto sendTextMessage(Long eventId, MessageRequestDto messageRequestDto) {
        validateMembership(eventId, messageRequestDto.getUserId());
        
        if (messageRequestDto.getMessageType() == DiscussionMessage.MessageType.TEXT && 
            (messageRequestDto.getContent() == null || messageRequestDto.getContent().trim().isEmpty())) {
            throw new IllegalArgumentException("Content cannot be empty for text messages");
        }
        
        DiscussionMessage message = new DiscussionMessage();
        message.setEventId(eventId);
        message.setUserId(messageRequestDto.getUserId());
        message.setContent(messageRequestDto.getContent());
        message.setMessageType(messageRequestDto.getMessageType());
        
        DiscussionMessage savedMessage = messageRepository.save(message);
        return mapToResponseDto(savedMessage);
    }

    public MessageResponseDto sendFileMessage(Long eventId, Long userId, MultipartFile file, DiscussionMessage.MessageType messageType) {
        validateMembership(eventId, userId);
        
        if (file.isEmpty()) {
            throw new FileUploadException("File cannot be empty");
        }
        
        String fileUrl = saveFile(file);
        
        DiscussionMessage message = new DiscussionMessage();
        message.setEventId(eventId);
        message.setUserId(userId);
        message.setFileUrl(fileUrl);
        message.setMessageType(messageType);
        
        DiscussionMessage savedMessage = messageRepository.save(message);
        return mapToResponseDto(savedMessage);
    }

    public List<MessageResponseDto> getEventMessages(Long eventId, Long userId) {
        validateMembership(eventId, userId);
        
        return messageRepository.findByEventIdOrderBySentAtAsc(eventId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<MessageResponseDto> getEventAllMessages(Long clubId, Long eventId) {
        validateMembership(eventId, clubId);
        return messageRepository.findByEventIdOrderBySentAtAsc(eventId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public void deleteMessage(Long messageId, Long userId) {
        DiscussionMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found with id: " + messageId));
        
        if (!message.getUserId().equals(userId) && !isClubAdmin(message.getEventId(), userId)) {
            throw new NotMemberException("Only sender or club admin can delete messages");
        }
        
        messageRepository.delete(message);
    }

    private void validateMembership(Long eventId, Long userId) {
        // Validate user exists
        try {
            userClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        
        // Get event details
        EventDto event;
        try {
            event = eventClient.getEventDetails(eventId);
        } catch (FeignException.NotFound e) {
            throw new EventNotFoundException("Event not found with id: " + eventId);
        }
        
        // Check if user is member of the event's club
        try {
            Boolean isMember = clubClient.checkMembership(event.getClubId(), userId);
            if (!isMember) {
                throw new NotMemberException("User is not a member of the club that owns this event");
            }
        } catch (FeignException.NotFound e) {
            throw new NotMemberException("User is not a member of the club that owns this event");
        }
    }

    private boolean isClubAdmin(Long eventId, Long userId) {
        try {
            EventDto event = eventClient.getEventDetails(eventId);
            String role = clubClient.getMemberRole(event.getClubId(), userId);
            return "PRESIDENT".equals(role) || "VICE_PRESIDENT".equals(role);
        } catch (FeignException e) {
            return false;
        }
    }

    private String saveFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload file: " + e.getMessage());
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