package com.campusconnect.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateNotificationRequest {
    private Long userId;
    private String userName;
    private String userEmail;
    private String eventName;
    private String eventDate;
    private byte[] pdfBytes;
}