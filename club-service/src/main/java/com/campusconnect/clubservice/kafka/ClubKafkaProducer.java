package com.campusconnect.clubservice.kafka;

import com.campusconnect.clubservice.dto.ClubMemberAddedRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "club-member-added-topic";

    public void sendClubMemberAddedNotification(ClubMemberAddedRequest request) {
        log.info("Sending club member added notification to Kafka topic: {}", TOPIC);
        try {
            kafkaTemplate.send(TOPIC, request);
            log.info("Successfully sent message for new club member {} in club {} to Kafka.", request.getUserId(), request.getClubId());
        } catch (Exception e) {
            log.error("Failed to send notification for new club member {}: {}", request.getUserId(), e.getMessage());
        }
    }
}