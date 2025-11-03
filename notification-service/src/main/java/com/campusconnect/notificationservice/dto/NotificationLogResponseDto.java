package com.campusconnect.notificationservice.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationLogResponseDto {
    private Long id;
    private String subject;
    private String body;
    private LocalDateTime createdAt;
}