package com.campusconnect.notificationservice.kafka;

import com.campusconnect.notificationservice.dto.PasswordResetEmailRequest;
import com.campusconnect.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "forgot-password-topic", containerFactory = "passwordResetListenerFactory")
    public void consumePasswordResetNotification(PasswordResetEmailRequest request) {
        log.info("Received password reset notification from Kafka for user email: {}", request.getEmail());
        try {
            notificationService.sendPasswordResetEmail(request);
            log.info("Successfully processed password reset email for user: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Error processing password reset notification for user {}: {}", request.getEmail(), e.getMessage());
        }
    }
}