package com.campusconnect.userservice.dto;

import java.time.LocalDateTime;


public class DiscussionResponseDto {
	private Long id;
    private Long userId;
    private String userName;
    private String content;
    private String fileUrl;
    private MessageType messageType;
    private LocalDateTime sentAt;
    public enum MessageType {
        TEXT, IMAGE, FILE
    }
}
