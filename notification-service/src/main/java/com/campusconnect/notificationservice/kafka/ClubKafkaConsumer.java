package com.campusconnect.notificationservice.kafka;

import com.campusconnect.notificationservice.dto.ClubMemberAddedRequest;
import com.campusconnect.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "club-member-added-topic", containerFactory = "clubMemberAddedListenerFactory")
    public void consumeClubMemberAddedNotification(ClubMemberAddedRequest request) {
        log.info("Received club member added notification from Kafka for user ID: {}", request.getUserId());
        try {
            // Call the existing service method to handle the business logic
            notificationService.notifyClubMemberAdded(request);
            log.info("Successfully processed club member added notification for user: {}", request.getUserId());
        } catch (Exception e) {
            log.error("Error processing club member added notification for user {}: {}", request.getUserId(), e.getMessage());
        }
    }
}