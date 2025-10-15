package com.event.certificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sent from certificate-service to notification-service.
 * pdfBytes will be Base64-encoded in JSON automatically by Jackson.
 */
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