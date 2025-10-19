package com.campusconnect.notificationservice.kafka;

import com.campusconnect.notificationservice.dto.ParticipantRegisteredEvent;
import com.campusconnect.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantKafkaConsumer {

    private final NotificationService notificationService;

    // Use the renamed method from NotificationService
    @KafkaListener(topics = "participant-registered-topic", containerFactory = "participantRegisteredListenerFactory")
    public void consumeParticipantRegisteredNotification(ParticipantRegisteredEvent request) {
        log.info("Received participant registered notification from Kafka for user ID: {} and event ID: {}", request.getUserId(), request.getEventId());
        try {
            notificationService.notifyEventParticipation(request);
        } catch (Exception e) {
            log.error("Error processing participant registered notification for user {}: {}", request.getUserId(), e.getMessage());
        }
    }
}