package com.campusconnect.notificationservice.kafka;

import com.campusconnect.notificationservice.dto.UserRegisteredRequest;
import com.campusconnect.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserKafkaConsumer {

    private final NotificationService notificationService;

    // ADD the containerFactory property to point to our new bean
    @KafkaListener(topics = "user-registered-topic", containerFactory = "userRegisteredListenerFactory")
    public void consumeUserRegisteredNotification(UserRegisteredRequest request) {
        log.info("Received user registered notification from Kafka for user ID: {}", request.getUserId());
        try {
            notificationService.notifyUserRegistered(request);
            log.info("Successfully processed user registered email for user: {}", request.getUserId());
        } catch (Exception e) {
            log.error("Error processing user registered notification for user {}: {}", request.getUserId(), e.getMessage());
        }
    }
}