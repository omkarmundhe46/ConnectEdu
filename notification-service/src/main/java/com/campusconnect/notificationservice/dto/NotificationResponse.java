package com.campusconnect.notificationservice.dto;

import lombok.Data;

@Data
public class NotificationResponse {
    private String details;
    private Integer enqueueCount;
}