package com.campusconnect.notificationservice.kafka;

import com.campusconnect.notificationservice.dto.EventResponseDto;
import com.campusconnect.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "event-created-topic", containerFactory = "eventCreatedListenerFactory")
    public void consumeEventCreatedNotification(EventResponseDto event) {
        log.info("Received event created notification from Kafka for event ID: {}", event.getId());
        try {
            // Call the existing service method to handle the business logic
            notificationService.notifyEventCreated(event);
            log.info("Successfully processed event created notification for event: {}", event.getId());
        } catch (Exception e) {
            log.error("Error processing event created notification for event {}: {}", event.getId(), e.getMessage());
        }
    }
}