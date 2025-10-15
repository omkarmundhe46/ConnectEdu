package com.campusconnect.discussionservice.dto;

import com.campusconnect.discussionservice.entity.DiscussionMessage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private String content;
    private String fileUrl;
    private DiscussionMessage.MessageType messageType;
    private LocalDateTime sentAt;
}