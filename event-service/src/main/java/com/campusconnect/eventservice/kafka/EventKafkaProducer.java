package com.campusconnect.eventservice.kafka;

import com.campusconnect.eventservice.dto.EventParticipationDTO;
import com.campusconnect.eventservice.dto.EventResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "event-created-topic";
    private static final String EVENT_PARTICIPATION_TOPIC = "event-participation-topic"; // New topic

    public void sendEventCreatedNotification(EventResponseDto event) {
        log.info("Sending event created notification to Kafka topic: {}", TOPIC);
        try {
            kafkaTemplate.send(TOPIC, event);
            log.info("Successfully sent message for new event ID {} to Kafka.", event.getId());
        } catch (Exception e) {
            log.error("Failed to send event created notification for event {}: {}", event.getId(), e.getMessage());
        }
    }

    // ADD THIS NEW METHOD
    public void sendEventParticipationNotification(EventParticipationDTO request) {
        log.info("Sending event participation notification to Kafka topic: {}", EVENT_PARTICIPATION_TOPIC);
        try {
            kafkaTemplate.send(EVENT_PARTICIPATION_TOPIC, request);
            log.info("Successfully sent participation message for user {} in event {} to Kafka.", request.getUserId(), request.getEventId());
        } catch (Exception e) {
            log.error("Failed to send participation notification for user {}: {}", request.getUserId(), e.getMessage());
        }
    }
}