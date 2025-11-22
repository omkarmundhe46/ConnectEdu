package com.campusconnect.userservice.kafka;

import com.campusconnect.userservice.dto.EmailVerificationRequest;
import com.campusconnect.userservice.dto.PasswordResetEmailRequest;
import com.campusconnect.userservice.dto.UserRegisteredRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "user-registered-topic";
    private static final String EMAIL_VERIFICATION_TOPIC = "email-verification-topic";
    private static final String FORGOT_PASSWORD_TOPIC = "forgot-password-topic";

    public void sendUserRegisteredNotification(UserRegisteredRequest request) {
        log.info("Sending user registered notification to Kafka topic: {}", TOPIC);
        try {
            kafkaTemplate.send(TOPIC, request);
            log.info("Successfully sent message for new user ID {} to Kafka.", request.getUserId());
        } catch (Exception e) {
            log.error("Failed to send user registered notification for user {}: {}", request.getUserId(), e.getMessage());
        }
    }

    public void sendPasswordResetEmail(PasswordResetEmailRequest request) {
        log.info("Sending password reset request to Kafka topic: {}", FORGOT_PASSWORD_TOPIC);
        try {
            kafkaTemplate.send(FORGOT_PASSWORD_TOPIC, request);
            log.info("Successfully sent password reset email for {} to Kafka.", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email for {}: {}", request.getEmail(), e.getMessage());
        }
    }

    public void sendEmailVerification(EmailVerificationRequest request) {
        log.info("Sending email verification request to Kafka topic: {}", EMAIL_VERIFICATION_TOPIC);
        try {
            kafkaTemplate.send(EMAIL_VERIFICATION_TOPIC, request);
            log.info("Successfully sent email verification for {} to Kafka.", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email verification for {}: {}", request.getEmail(), e.getMessage());
        }
    }
}