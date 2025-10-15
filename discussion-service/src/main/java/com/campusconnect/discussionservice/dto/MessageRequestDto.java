package com.campusconnect.discussionservice.dto;

import com.campusconnect.discussionservice.entity.DiscussionMessage;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    private String content;

    @NotNull(message = "Message type is required")
    private DiscussionMessage.MessageType messageType;
}