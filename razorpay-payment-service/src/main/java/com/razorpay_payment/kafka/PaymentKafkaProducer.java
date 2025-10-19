package com.razorpay_payment.kafka;

import com.razorpay_payment.dto.PaymentSuccessfulEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payment-successful-topic";

    public void sendPaymentSuccessEvent(PaymentSuccessfulEvent event) {
        log.info("Sending successful payment event to Kafka topic: {}", TOPIC);
        try {
            kafkaTemplate.send(TOPIC, event);
            log.info("Successfully sent message for payment ID {} to Kafka.", event.getPaymentId());
        } catch (Exception e) {
            log.error("Failed to send payment success event for payment ID {}: {}", event.getPaymentId(), e.getMessage());
        }
    }
}