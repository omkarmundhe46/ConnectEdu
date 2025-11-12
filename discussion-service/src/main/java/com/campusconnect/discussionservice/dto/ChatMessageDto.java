package com.campusconnect.discussionservice.dto;

import com.campusconnect.discussionservice.entity.DiscussionMessage;
import lombok.Builder;
import lombok.Data;

// This DTO will be used for both sending (request) and receiving (response)
@Data
@Builder
public class ChatMessageDto {
    private String content;
    private String fileUrl;
    private DiscussionMessage.MessageType messageType;

    // These fields will be added by the server
    private Long id;
    private Long userId;
    private String userName;
    private String sentAt;
}