package com.campusconnect.notificationservice.kafka;

import com.campusconnect.notificationservice.dto.CertificateNotificationRequest;
import com.campusconnect.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateKafkaConsumer {

    private final NotificationService notificationService;

    // ADD the containerFactory property to point to our new bean
    @KafkaListener(topics = "certificate-issued-topic", containerFactory = "certificateListenerFactory")
    public void consumeCertificateNotification(CertificateNotificationRequest request) {
        log.info("Received certificate notification from Kafka for user email: {}", request.getUserEmail());
        try {
            notificationService.sendCertificateEmail(request);
            log.info("Successfully processed certificate email for user: {}", request.getUserEmail());
        } catch (Exception e) {
            log.error("Error processing certificate notification for user {}: {}", request.getUserEmail(), e.getMessage());
        }
    }
}