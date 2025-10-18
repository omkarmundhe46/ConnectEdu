package com.event.certificationservice.service;

import com.event.certificationservice.dto.CertificateNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "certificate-issued-topic";

    public void sendCertificateNotification(CertificateNotificationRequest request) {
        log.info("Sending certificate notification to Kafka topic: {}", TOPIC);
        try {
            // Send the request object to the specified Kafka topic.
            // Spring Boot will automatically serialize this object to JSON.
            kafkaTemplate.send(TOPIC, request);
            log.info("Successfully sent certificate message for user {} to Kafka.", request.getUserId());
        } catch (Exception e) {
            log.error("Failed to send certificate notification to Kafka for user {}: {}", request.getUserId(), e.getMessage());
        }
    }
}