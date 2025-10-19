//package com.campusconnect.notificationservice.kafka;
//
//import com.campusconnect.notificationservice.dto.EventParticipationDTO;
//import com.campusconnect.notificationservice.service.NotificationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EventParticipationKafkaConsumer {
//
//    private final NotificationService notificationService;
//
//    @KafkaListener(topics = "event-participation-topic", containerFactory = "eventParticipationListenerFactory")
//    public void consumeEventParticipationNotification(EventParticipationDTO request) {
//        log.info("Received event participation notification from Kafka for user ID: {} and event ID: {}", request.getUserId(), request.getEventId());
//        try {
//            // Call the existing service method to handle the business logic
//            notificationService.notifyEventParticipation(request);
//            log.info("Successfully processed event participation notification for user: {}", request.getUserId());
//        } catch (Exception e) {
//            log.error("Error processing event participation notification for user {}: {}", request.getUserId(), e.getMessage());
//        }
//    }
//}