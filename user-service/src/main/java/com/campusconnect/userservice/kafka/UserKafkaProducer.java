package com.campusconnect.userservice.kafka;

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

    public void sendUserRegisteredNotification(UserRegisteredRequest request) {
        log.info("Sending user registered notification to Kafka topic: {}", TOPIC);
        try {
            kafkaTemplate.send(TOPIC, request);
            log.info("Successfully sent message for new user ID {} to Kafka.", request.getUserId());
        } catch (Exception e) {
            log.error("Failed to send user registered notification for user {}: {}", request.getUserId(), e.getMessage());
        }
    }
}