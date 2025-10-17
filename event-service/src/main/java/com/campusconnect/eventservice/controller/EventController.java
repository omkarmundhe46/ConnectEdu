
package com.campusconnect.eventservice.controller;

import com.campusconnect.eventservice.client.CertificateClient;
import com.campusconnect.eventservice.client.ClubClient;
import com.campusconnect.eventservice.dto.*;
import com.campusconnect.eventservice.service.EventService;
import com.campusconnect.eventservice.kafka.EventKafkaProducer; // Import Kafka producer
//import com.campusconnect.eventservice.client.NotificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final EventKafkaProducer eventKafkaProducer; // ADD THIS
//    private final NotificationClient notificationClient;
    private ClubClient clubClient;
    // Inject the new CertificateClient
    private final CertificateClient certificateClient;

//    @PostMapping("/{clubId}/events")
//    public ResponseEntity<EventResponseDto> createClubEvent(@PathVariable Long clubId,
//                                                           @Valid @RequestBody EventRequestDto eventRequestDto) {
//        EventResponseDto createdEvent = eventService.createClubEvent(clubId, eventRequestDto);
//
//        // Send event created notification asynchronously
//        sendEventCreatedNotificationAsync(createdEvent);
//
//        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
//    }

    @PostMapping("/{clubId}/events")
    public ResponseEntity<EventResponseDto> createClubEvent(@PathVariable Long clubId,
                                                            @Valid @RequestBody EventRequestDto eventRequestDto) {
        EventResponseDto createdEvent = eventService.createClubEvent(clubId, eventRequestDto);

        // **MODIFIED PART**: Send notification via Kafka
        log.info("🔔 Queuing event created notification for eventId={}", createdEvent.getId());
        eventKafkaProducer.sendEventCreatedNotification(createdEvent);

        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }


//    @Async
//    public CompletableFuture<Void> sendEventCreatedNotificationAsync(EventResponseDto event) {
//        try {
//            log.info("🔔 Sending event created notification for eventId={}, clubId={}", event.getId(), event.getClubId());
//
//            notificationClient.notifyEventCreated(event);
//
//            log.info("✅ Event created notification sent for event: {}", event.getId());
//        } catch (Exception e) {
//            log.error("❌ Failed to send event created notification for event: {}", event.getId(), e);
//        }
//        return CompletableFuture.completedFuture(null);
//    }

    @GetMapping("/{clubId}/events")
    public ResponseEntity<List<EventResponseDto>> getClubEvents(@PathVariable Long clubId) {
        List<EventResponseDto> events = eventService.getClubEvents(clubId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{clubId}/events/{eventId}")
    public ResponseEntity<EventResponseDto> getClubEventById(@PathVariable Long clubId, @PathVariable Long eventId) {
        EventResponseDto event = eventService.getClubEventById(clubId, eventId);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{clubId}/events/{eventId}")
    public ResponseEntity<EventResponseDto> updateClubEvent(@PathVariable Long clubId, @PathVariable Long eventId,
                                                           @Valid @RequestBody EventRequestDto eventRequestDto) {
        EventResponseDto updatedEvent = eventService.updateClubEvent(clubId, eventId, eventRequestDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{clubId}/events/{eventId}")
    public ResponseEntity<Void> deleteClubEvent(@PathVariable Long clubId, @PathVariable Long eventId) {
        eventService.deleteClubEvent(clubId, eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clubId}/events/{eventId}/participants")
    public ResponseEntity<ParticipantResponseDto> addParticipantToEvent(@PathVariable Long clubId, @PathVariable Long eventId,
                                                                       @Valid @RequestBody EventParticipationDTO participantRequestDto) {
        ParticipantResponseDto participant = eventService.addParticipantToEvent(clubId, eventId, participantRequestDto);
        return new ResponseEntity<>(participant, HttpStatus.CREATED);
    }

    @GetMapping("/{clubId}/events/{eventId}/participants")
    public ResponseEntity<List<ParticipantResponseDto>> getEventParticipants(@PathVariable Long clubId, @PathVariable Long eventId) {
        List<ParticipantResponseDto> participants = eventService.getEventParticipants(clubId, eventId);
        return ResponseEntity.ok(participants);
    }

    @DeleteMapping("/{clubId}/events/{eventId}/participants/{userId}")
    public ResponseEntity<Void> removeParticipantFromEvent(@PathVariable Long clubId, @PathVariable Long eventId, @PathVariable Long userId) {
        eventService.removeParticipantFromEvent(clubId, eventId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{eventId}/details")
    public ResponseEntity<EventResponseDto> getEventDetails(@PathVariable Long eventId) {
        EventResponseDto event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/events/{eventId}/complete")
	public ResponseEntity<Void> completeEventAndSendCertificates(@PathVariable Long eventId) {
		eventService.completeEventAndSendCertificates(eventId);
		return ResponseEntity.ok().build();
	}


    /**
     * NEW ENDPOINT:
     * Acts as a proxy to the certificate-service for downloading a certificate.
     */
    @GetMapping("/{clubId}/events/{eventId}/participants/{userId}/certificate/download")
    public ResponseEntity<byte[]> downloadParticipantCertificate(
            @PathVariable Long clubId, // The clubId is part of the path but not used in the call
            @PathVariable Long eventId,
            @PathVariable Long userId) {

        log.info("Request received to download certificate for event {} and user {}", eventId, userId);

        // This correctly calls the CertificateClient and returns the response
        return certificateClient.downloadCertificate(eventId, userId);
    }
}
