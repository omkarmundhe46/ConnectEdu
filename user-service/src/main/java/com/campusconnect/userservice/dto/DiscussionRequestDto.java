package com.campusconnect.userservice.dto;


import jakarta.validation.constraints.NotNull;

public class DiscussionRequestDto {
	@NotNull(message = "User ID is required")
    private Long userId;

    private String content;

    @NotNull(message = "Message type is required")
    private MessageType messageType;
    public enum MessageType {
        TEXT, IMAGE, FILE
    }
}
