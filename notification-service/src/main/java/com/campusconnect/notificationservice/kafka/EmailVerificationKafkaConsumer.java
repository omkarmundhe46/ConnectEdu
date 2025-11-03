package com.campusconnect.notificationservice.kafka;

import com.campusconnect.notificationservice.dto.EmailVerificationRequest;
import com.campusconnect.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "email-verification-topic", containerFactory = "emailVerificationListenerFactory")
    public void consumeEmailVerification(EmailVerificationRequest request) {
        log.info("Received email verification request from Kafka for email: {}", request.getEmail());
        try {
            notificationService.sendVerificationEmail(request);
            log.info("Successfully processed verification email for: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Error processing verification email for {}: {}", request.getEmail(), e.getMessage());
        }
    }
}