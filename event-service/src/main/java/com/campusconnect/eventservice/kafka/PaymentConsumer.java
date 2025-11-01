package com.campusconnect.eventservice.kafka;

import com.campusconnect.eventservice.dto.ParticipantRegisteredEvent;
import com.campusconnect.eventservice.dto.PaymentSuccessfulEvent;
import com.campusconnect.eventservice.entity.EventParticipant;
import com.campusconnect.eventservice.repository.EventParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentConsumer {

    private final EventParticipantRepository participantRepository;
    private final EventKafkaProducer kafkaProducer; // Inject the producer
    // You could also inject a Kafka producer here to send a final confirmation email.

    @KafkaListener(topics = "payment-successful-topic", containerFactory = "paymentSuccessListenerFactory")
    public void handlePaymentSuccess(PaymentSuccessfulEvent event) {
        log.info("Received successful payment event from Kafka: {}", event);

        // Check if this payment has already been processed to ensure idempotency.
        if (participantRepository.existsByPaymentId(event.getPaymentId())) {
            log.warn("Payment ID {} has already been processed. Skipping.", event.getPaymentId());
            return;
        }

        // Save the participant to the database with all their details.
        EventParticipant participant = new EventParticipant();
        participant.setUserId(event.getRegistrationData().getUserId());
        participant.setEventId(event.getRegistrationData().getEventId());
        participant.setCollege(event.getRegistrationData().getCollege());
        participant.setMobileNumber(event.getRegistrationData().getMobileNumber());
        participant.setAddress(event.getRegistrationData().getAddress());
        participant.setPaymentId(event.getPaymentId());

        participantRepository.save(participant);
        log.info("✅ Successfully registered participant {} for event {}.", participant.getUserId(), participant.getEventId());

        // After saving, send a message to the notification-service.
        ParticipantRegisteredEvent notificationEvent = ParticipantRegisteredEvent.builder()
                .userId(participant.getUserId())
                .eventId(participant.getEventId())
                .build();
        kafkaProducer.sendParticipantRegisteredNotification(notificationEvent);
    }
}