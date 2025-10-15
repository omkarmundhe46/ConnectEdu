package com.campusconnect.discussionservice.controller;

import com.campusconnect.discussionservice.dto.MessageRequestDto;
import com.campusconnect.discussionservice.dto.MessageResponseDto;
import com.campusconnect.discussionservice.entity.DiscussionMessage;
import com.campusconnect.discussionservice.service.DiscussionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class DiscussionController {
    
    private final DiscussionService discussionService;

    @PostMapping("/{clubId}/events/{eventId}/discussions/messages")
    public ResponseEntity<MessageResponseDto> sendMessage(@PathVariable Long clubId,
                                                         @PathVariable Long eventId,
                                                         @Valid @RequestBody MessageRequestDto messageRequestDto) {
        MessageResponseDto response = discussionService.sendTextMessage(eventId, messageRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{clubId}/events/{eventId}/discussions/messages/file")
    public ResponseEntity<MessageResponseDto> sendFileMessage(@PathVariable Long clubId,
                                                             @PathVariable Long eventId,
                                                             @RequestParam("userId") Long userId,
                                                             @RequestParam("file") MultipartFile file,
                                                             @RequestParam("messageType") DiscussionMessage.MessageType messageType) {
        MessageResponseDto response = discussionService.sendFileMessage(eventId, userId, file, messageType);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{clubId}/events/{eventId}/discussions/messages")
    public ResponseEntity<List<MessageResponseDto>> getEventMessages(@PathVariable Long clubId,
                                                                    @PathVariable Long eventId,
                                                                    @RequestParam("userId") Long userId) {
        List<MessageResponseDto> messages = discussionService.getEventMessages(eventId, userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{clubId}/events/{eventId}/discussions/messages/all")
    public ResponseEntity<List<MessageResponseDto>> getEventAllMessages(@PathVariable Long clubId,
                                                                     @PathVariable Long eventId
                                                                     ) {
        List<MessageResponseDto> messages = discussionService.getEventAllMessages(clubId, eventId);
        return ResponseEntity.ok(messages);
    }


    @DeleteMapping("/{clubId}/events/{eventId}/discussions/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long clubId,
                                             @PathVariable Long eventId,
                                             @PathVariable Long messageId,
                                             @RequestParam("userId") Long userId) {
        discussionService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}